package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.event.block.BlockRedstoneEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.math.Vector3
import cn.nukkit.network.protocol.LevelSoundEventPacket

/**
 * @author CreeperFace
 */
class BlockTripWireHook @JvmOverloads constructor(meta: Int = 0) : BlockFlowable(meta) {
	override val name: String
		get() = "Tripwire Hook"

	override val id: Int
		get() = BlockID.Companion.TRIPWIRE_HOOK

	val facing: BlockFace
		get() = BlockFace.fromHorizontalIndex(damage and 3)

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			if (!this.getSide(facing.opposite).isNormalBlock) {
				level.useBreakOn(this)
			}
			return type
		} else if (type == Level.BLOCK_UPDATE_SCHEDULED) {
			calculateState(false, true, -1, null)
			return type
		}
		return 0
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (!this.getSide(face.opposite).isNormalBlock || face == BlockFace.DOWN || face == BlockFace.UP) {
			return false
		}
		if (face.axis.isHorizontal) {
			setFace(face)
		}
		level.setBlock(this, this)
		if (player != null) {
			calculateState(false, false, -1, null)
		}
		return true
	}

	override fun onBreak(item: Item): Boolean {
		super.onBreak(item)
		val attached = isAttached
		val powered = isPowered
		if (attached || powered) {
			calculateState(true, false, -1, null)
		}
		if (powered) {
			level.updateAroundRedstone(this, null)
			level.updateAroundRedstone(this.location.getSide(facing.opposite), null)
		}
		return true
	}

	fun calculateState(onBreak: Boolean, updateAround: Boolean, pos: Int, block: Block?) {
		var block = block
		val facing = facing
		val v: Vector3 = this.location
		val attached = isAttached
		val powered = isPowered
		var canConnect = !onBreak
		var nextPowered = false
		var distance = 0
		val blocks = arrayOfNulls<Block>(42)
		for (i in 1..41) {
			val vector = v.getSide(facing, i)
			var b = level.getBlock(vector)
			if (b is BlockTripWireHook) {
				if (b.facing == facing.opposite) {
					distance = i
				}
				break
			}
			if (b.id != BlockID.Companion.TRIPWIRE && i != pos) {
				blocks[i] = null
				canConnect = false
			} else {
				if (i == pos) {
					b = block ?: b
				}
				if (b is BlockTripWire) {
					val disarmed = !b.isDisarmed
					val wirePowered = b.isPowered
					nextPowered = nextPowered or (disarmed && wirePowered)
					if (i == pos) {
						level.scheduleUpdate(this, 10)
						canConnect = canConnect and disarmed
					}
				}
				blocks[i] = b
			}
		}
		canConnect = canConnect and distance > 1
		nextPowered = nextPowered and canConnect
		val hook = Block.Companion.get(BlockID.Companion.TRIPWIRE_HOOK) as BlockTripWireHook
		hook.attached = canConnect
		hook.powered = nextPowered
		if (distance > 0) {
			val vec = v.getSide(facing, distance)
			val face = facing.opposite
			hook.setFace(face)
			level.setBlock(vec, hook, true, false)
			level.updateAroundRedstone(vec, null)
			level.updateAroundRedstone(vec.getSide(face.opposite), null)
			addSound(vec, canConnect, nextPowered, attached, powered)
		}
		addSound(v, canConnect, nextPowered, attached, powered)
		if (!onBreak) {
			hook.setFace(facing)
			level.setBlock(v, hook, true, false)
			if (updateAround) {
				level.updateAroundRedstone(v, null)
				level.updateAroundRedstone(v.getSide(facing.opposite), null)
			}
		}
		if (attached != canConnect) {
			for (i in 1 until distance) {
				val vc = v.getSide(facing, i)
				block = blocks[i]
				if (block != null && level.getBlockIdAt(vc.floorX, vc.floorY, vc.floorZ) != BlockID.Companion.AIR) {
					if (canConnect xor (block.damage and 0x04 > 0)) {
						block.setDamage(block.damage xor 0x04)
					}
					level.setBlock(vc, block, true, false)
				}
			}
		}
	}

	private fun addSound(pos: Vector3, canConnect: Boolean, nextPowered: Boolean, attached: Boolean, powered: Boolean) {
		if (nextPowered && !powered) {
			level.addLevelSoundEvent(pos, LevelSoundEventPacket.SOUND_POWER_ON)
			level.server.pluginManager.callEvent(BlockRedstoneEvent(this, 0, 15))
		} else if (!nextPowered && powered) {
			level.addLevelSoundEvent(pos, LevelSoundEventPacket.SOUND_POWER_OFF)
			level.server.pluginManager.callEvent(BlockRedstoneEvent(this, 15, 0))
		} else if (canConnect && !attached) {
			level.addLevelSoundEvent(pos, LevelSoundEventPacket.SOUND_ATTACH)
		} else if (!canConnect && attached) {
			level.addLevelSoundEvent(pos, LevelSoundEventPacket.SOUND_DETACH)
		}
	}

	var isAttached: Boolean
		get() = damage and 0x04 > 0
		set(value) {
			if (value xor isAttached) {
				this.setDamage(this.damage xor 0x04)
			}
		}

	var isPowered: Boolean
		get() = this.damage and 0x08 > 0
		set(value) {
			if (value xor isPowered) {
				this.setDamage(this.damage xor 0x08)
			}
		}

	fun setFace(face: BlockFace) {
		this.setDamage(this.damage - this.damage % 4)
		this.setDamage(this.damage or face.horizontalIndex)
	}

	override val isPowerSource: Boolean
		get() = true

	override fun getWeakPower(face: BlockFace): Int {
		return if (isPowered) 15 else 0
	}

	override fun getStrongPower(side: BlockFace): Int {
		return if (!isPowered) 0 else if (facing == side) 15 else 0
	}

	override fun toItem(): Item? {
		return ItemBlock(this, 0)
	}
}