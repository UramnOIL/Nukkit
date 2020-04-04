package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.event.block.BlockRedstoneEvent
import cn.nukkit.event.block.DoorToggleEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.Level
import cn.nukkit.level.Sound
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.BlockFace
import cn.nukkit.math.SimpleAxisAlignedBB
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.Faceable

/**
 * Created by Pub4Game on 26.12.2015.
 */
open class BlockTrapdoor @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta), Faceable {
	override val id: Int
		get() = BlockID.Companion.TRAPDOOR

	override val name: String
		get() = "Wooden Trapdoor"

	override val hardness: Double
		get() = 3

	override val resistance: Double
		get() = 15

	override fun canBeActivated(): Boolean {
		return true
	}

	override val toolType: Int
		get() = ItemTool.TYPE_AXE

	companion object {
		const val TRAPDOOR_OPEN_BIT = 0x08
		const val TRAPDOOR_TOP_BIT = 0x04
		private val boundingBoxDamage = arrayOfNulls<AxisAlignedBB>(16)

		init {
			for (damage in 0..15) {
				var bb: AxisAlignedBB
				val f = 0.1875
				if (damage and TRAPDOOR_TOP_BIT > 0) {
					cn.nukkit.block.bb = SimpleAxisAlignedBB(
							0,
							1 - cn.nukkit.block.f,
							0,
							1,
							1,
							1
					)
				} else {
					cn.nukkit.block.bb = SimpleAxisAlignedBB(
							0,
							0,
							0,
							1,
							0 + cn.nukkit.block.f,
							1
					)
				}
				if (damage and TRAPDOOR_OPEN_BIT > 0) {
					if (damage and 0x03 == 0) {
						cn.nukkit.block.bb.setBounds(0.0, 0.0,
								1 - cn.nukkit.block.f, 1.0, 1.0, 1.0)
					} else if (damage and 0x03 == 1) {
						cn.nukkit.block.bb.setBounds(0.0, 0.0, 0.0, 1.0, 1.0,
								0 + cn.nukkit.block.f
						)
					}
					if (damage and 0x03 == 2) {
						cn.nukkit.block.bb.setBounds(
								1 - cn.nukkit.block.f, 0.0, 0.0, 1.0, 1.0, 1.0)
					}
					if (damage and 0x03 == 3) {
						cn.nukkit.block.bb.setBounds(0.0, 0.0, 0.0,
								0 + cn.nukkit.block.f, 1.0, 1.0)
					}
				}
				boundingBoxDamage[damage] = cn.nukkit.block.bb
			}
		}
	}

	private val relativeBoundingBox: AxisAlignedBB?
		private get() = boundingBoxDamage[this.damage]

	override fun getMinX(): Double {
		return x + relativeBoundingBox!!.minX
	}

	override fun getMaxX(): Double {
		return x + relativeBoundingBox!!.maxX
	}

	override fun getMinY(): Double {
		return y + relativeBoundingBox!!.minY
	}

	override fun getMaxY(): Double {
		return y + relativeBoundingBox!!.maxY
	}

	override fun getMinZ(): Double {
		return z + relativeBoundingBox!!.minZ
	}

	override fun getMaxZ(): Double {
		return z + relativeBoundingBox!!.maxZ
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_REDSTONE) {
			if (!isOpen && level.isBlockPowered(this.location) || isOpen && !level.isBlockPowered(this.location)) {
				level.server.pluginManager.callEvent(BlockRedstoneEvent(this, if (isOpen) 15 else 0, if (isOpen) 0 else 15))
				this.setDamage(this.damage xor 0x04)
				level.setBlock(this, this, true)
				level.addSound(this, if (isOpen) Sound.RANDOM_DOOR_OPEN else Sound.RANDOM_DOOR_CLOSE)
				return type
			}
		}
		return 0
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val facing: BlockFace
		val top: Boolean
		var meta = 0
		if (face.axis.isHorizontal || player == null) {
			facing = face
			top = fy > 0.5
		} else {
			facing = player.direction.opposite
			top = face != BlockFace.UP
		}
		val faces = intArrayOf(2, 1, 3, 0)
		val faceBit = faces[facing.horizontalIndex]
		meta = meta or faceBit
		if (top) {
			meta = meta or TRAPDOOR_TOP_BIT
		}
		this.setDamage(meta)
		getLevel().setBlock(block, this, true, true)
		return true
	}

	override fun toItem(): Item? {
		return ItemBlock(this, 0)
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (toggle(player)) {
			level.addSound(this, if (isOpen) Sound.RANDOM_DOOR_OPEN else Sound.RANDOM_DOOR_CLOSE)
			return true
		}
		return false
	}

	fun toggle(player: Player?): Boolean {
		val ev = DoorToggleEvent(this, player)
		getLevel().server.pluginManager.callEvent(ev)
		if (ev.isCancelled) {
			return false
		}
		this.setDamage(this.damage xor TRAPDOOR_OPEN_BIT)
		getLevel().setBlock(this, this, true)
		return true
	}

	override val color: BlockColor
		get() = BlockColor.WOOD_BLOCK_COLOR

	val isOpen: Boolean
		get() = this.damage and TRAPDOOR_OPEN_BIT != 0

	val isTop: Boolean
		get() = this.damage and TRAPDOOR_TOP_BIT != 0

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x07)
	}
}