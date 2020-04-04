package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.event.block.BlockRedstoneEvent
import cn.nukkit.event.block.DoorToggleEvent
import cn.nukkit.item.Item
import cn.nukkit.level.Level
import cn.nukkit.level.Sound
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.BlockFace
import cn.nukkit.math.SimpleAxisAlignedBB
import cn.nukkit.utils.Faceable

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class BlockDoor protected constructor(meta: Int) : BlockTransparentMeta(meta), Faceable {
	override fun canBeActivated(): Boolean {
		return true
	}

	override val isSolid: Boolean
		get() = false

	val fullDamage: Int
		get() {
			val meta: Int
			meta = if (isTop) {
				this.down().damage
			} else {
				this.damage
			}
			return (this.id shl 5) + (meta and 0x07 or (if (isTop) 0x08 else 0) or if (isRightHinged) 0x10 else 0)
		}

	override fun recalculateBoundingBox(): AxisAlignedBB? {
		val f = 0.1875
		val bb: AxisAlignedBB = SimpleAxisAlignedBB(
				x,
				y,
				z,
				x + 1,
				y + 2,
				z + 1
		)
		val j = if (isTop) this.down().damage and 0x03 else damage and 0x03
		val isOpen = isOpen
		val isRight = isRightHinged
		if (j == 0) {
			if (isOpen) {
				if (!isRight) {
					bb.setBounds(
							x,
							y,
							z,
							x + 1,
							y + 1,
							z + f
					)
				} else {
					bb.setBounds(
							x,
							y,
							z + 1 - f,
							x + 1,
							y + 1,
							z + 1
					)
				}
			} else {
				bb.setBounds(
						x,
						y,
						z,
						x + f,
						y + 1,
						z + 1
				)
			}
		} else if (j == 1) {
			if (isOpen) {
				if (!isRight) {
					bb.setBounds(
							x + 1 - f,
							y,
							z,
							x + 1,
							y + 1,
							z + 1
					)
				} else {
					bb.setBounds(
							x,
							y,
							z,
							x + f,
							y + 1,
							z + 1
					)
				}
			} else {
				bb.setBounds(
						x,
						y,
						z,
						x + 1,
						y + 1,
						z + f
				)
			}
		} else if (j == 2) {
			if (isOpen) {
				if (!isRight) {
					bb.setBounds(
							x,
							y,
							z + 1 - f,
							x + 1,
							y + 1,
							z + 1
					)
				} else {
					bb.setBounds(
							x,
							y,
							z,
							x + 1,
							y + 1,
							z + f
					)
				}
			} else {
				bb.setBounds(
						x + 1 - f,
						y,
						z,
						x + 1,
						y + 1,
						z + 1
				)
			}
		} else if (j == 3) {
			if (isOpen) {
				if (!isRight) {
					bb.setBounds(
							x,
							y,
							z,
							x + f,
							y + 1,
							z + 1
					)
				} else {
					bb.setBounds(
							x + 1 - f,
							y,
							z,
							x + 1,
							y + 1,
							z + 1
					)
				}
			} else {
				bb.setBounds(
						x,
						y,
						z + 1 - f,
						x + 1,
						y + 1,
						z + 1
				)
			}
		}
		return bb
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			if (this.down().id == BlockID.Companion.AIR) {
				val up = this.up()
				if (up is BlockDoor) {
					getLevel().setBlock(up, Block.Companion.get(BlockID.Companion.AIR), false)
					getLevel().useBreakOn(this)
				}
				return Level.BLOCK_UPDATE_NORMAL
			}
		}
		if (type == Level.BLOCK_UPDATE_REDSTONE) {
			if (!isOpen && level.isBlockPowered(this.location) || isOpen && !level.isBlockPowered(this.location)) {
				level.server.pluginManager.callEvent(BlockRedstoneEvent(this, if (isOpen) 15 else 0, if (isOpen) 0 else 15))
				toggle(null)
			}
		}
		return 0
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (y > 254) return false
		if (face == BlockFace.UP) {
			val blockUp = this.up()
			val blockDown = this.down()
			if (!blockUp!!.canBeReplaced() || blockDown!!.isTransparent) {
				return false
			}
			val faces = intArrayOf(1, 2, 3, 0)
			val direction = faces[player?.direction?.horizontalIndex ?: 0]
			val left = this.getSide(player!!.direction.rotateYCCW())
			val right = this.getSide(player.direction.rotateY())
			var metaUp = DOOR_TOP_BIT
			if (left.id == this.id || !right!!.isTransparent && left!!.isTransparent) { //Door hinge
				metaUp = metaUp or DOOR_HINGE_BIT
			}
			this.setDamage(direction)
			getLevel().setBlock(block, this, true, false) //Bottom
			getLevel().setBlock(blockUp, Block.Companion.get(this.id, metaUp), true, true) //Top
			if (!isOpen && level.isBlockPowered(this.location)) {
				toggle(null)
				metaUp = metaUp or DOOR_POWERED_BIT
				getLevel().setBlockDataAt(blockUp.floorX, blockUp.floorY, blockUp.floorZ, metaUp)
			}
			return true
		}
		return false
	}

	override fun onBreak(item: Item): Boolean {
		if (isTop(this.damage)) {
			val down = this.down()
			if (down.id == this.id) {
				getLevel().setBlock(down, Block.Companion.get(BlockID.Companion.AIR), true)
			}
		} else {
			val up = this.up()
			if (up.id == this.id) {
				getLevel().setBlock(up, Block.Companion.get(BlockID.Companion.AIR), true)
			}
		}
		getLevel().setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true)
		return true
	}

	override fun onActivate(item: Item): Boolean {
		return this.onActivate(item, null)
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (!toggle(player)) {
			return false
		}
		level.addSound(this, if (isOpen) Sound.RANDOM_DOOR_OPEN else Sound.RANDOM_DOOR_CLOSE)
		return true
	}

	fun toggle(player: Player?): Boolean {
		val event = DoorToggleEvent(this, player)
		getLevel().server.pluginManager.callEvent(event)
		if (event.isCancelled) {
			return false
		}
		val down: Block?
		down = if (isTop) {
			this.down()
		} else {
			this
		}
		if (down.up().id != down.id) {
			return false
		}
		down.setDamage(down.damage xor DOOR_OPEN_BIT)
		getLevel().setBlock(down, down, true, true)
		return true
	}

	val isOpen: Boolean
		get() = if (isTop(this.damage)) {
			this.down().damage and DOOR_OPEN_BIT > 0
		} else {
			this.damage and DOOR_OPEN_BIT > 0
		}

	val isTop: Boolean
		get() = isTop(this.damage)

	fun isTop(meta: Int): Boolean {
		return meta and DOOR_TOP_BIT != 0
	}

	val isRightHinged: Boolean
		get() = if (isTop) {
			this.damage and DOOR_HINGE_BIT > 0
		} else this.up().damage and DOOR_HINGE_BIT > 0

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x07)
	}

	companion object {
		var DOOR_OPEN_BIT = 0x04
		var DOOR_TOP_BIT = 0x08
		var DOOR_HINGE_BIT = 0x01
		var DOOR_POWERED_BIT = 0x02
	}
}