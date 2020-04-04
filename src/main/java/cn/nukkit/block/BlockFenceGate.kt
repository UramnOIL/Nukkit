package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.event.block.DoorToggleEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.Level
import cn.nukkit.level.Sound
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.Faceable

/**
 * Created on 2015/11/23 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
open class BlockFenceGate @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta), Faceable {
	override val id: Int
		get() = BlockID.Companion.FENCE_GATE_OAK

	override val name: String
		get() = "Oak Fence Gate"

	override val hardness: Double
		get() = 2

	override val resistance: Double
		get() = 15

	override fun canBeActivated(): Boolean {
		return true
	}

	override val toolType: Int
		get() = ItemTool.TYPE_AXE

	companion object {
		private val offMinX = DoubleArray(2)
		private val offMinZ = DoubleArray(2)
		private val offMaxX = DoubleArray(2)
		private val offMaxZ = DoubleArray(2)

		init {
			offMinX[0] = 0
			offMinZ[0] = 0.375
			offMaxX[0] = 1
			offMaxZ[0] = 0.625
			offMinX[1] = 0.375
			offMinZ[1] = 0
			offMaxX[1] = 0.625
			offMaxZ[1] = 1
		}
	}

	private val offsetIndex: Int
		private get() = when (this.damage and 0x03) {
			0, 2 -> 0
			else -> 1
		}

	override fun getMinX(): Double {
		return x + offMinX[offsetIndex]
	}

	override fun getMinZ(): Double {
		return z + offMinZ[offsetIndex]
	}

	override fun getMaxX(): Double {
		return x + offMaxX[offsetIndex]
	}

	override fun getMaxZ(): Double {
		return z + offMaxZ[offsetIndex]
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		this.setDamage(player?.direction?.horizontalIndex ?: 0)
		getLevel().setBlock(block, this, true, true)
		return true
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (player == null) {
			return false
		}
		if (!toggle(player)) {
			return false
		}
		level.addSound(this, if (isOpen) Sound.RANDOM_DOOR_OPEN else Sound.RANDOM_DOOR_CLOSE)
		return true
	}

	override val color: BlockColor
		get() = BlockColor.WOOD_BLOCK_COLOR

	fun toggle(player: Player?): Boolean {
		var player = player
		val event = DoorToggleEvent(this, player)
		getLevel().server.pluginManager.callEvent(event)
		if (event.isCancelled) {
			return false
		}
		player = event.player
		val direction: Int
		if (player != null) {
			val yaw = player.yaw
			var rotation = (yaw - 90) % 360
			if (rotation < 0) {
				rotation += 360.0
			}
			val originDirection = this.damage and 0x01
			direction = if (originDirection == 0) {
				if (rotation >= 0 && rotation < 180) {
					2
				} else {
					0
				}
			} else {
				if (rotation >= 90 && rotation < 270) {
					3
				} else {
					1
				}
			}
		} else {
			val originDirection = this.damage and 0x01
			direction = if (originDirection == 0) {
				0
			} else {
				1
			}
		}
		this.setDamage(direction or (this.damage.inv() and 0x04))
		level.setBlock(this, this, false, false)
		return true
	}

	val isOpen: Boolean
		get() = this.damage and 0x04 > 0

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_REDSTONE) {
			if (!isOpen && level.isBlockPowered(this.location) || isOpen && !level.isBlockPowered(this.location)) {
				toggle(null)
				return type
			}
		}
		return 0
	}

	override fun toItem(): Item? {
		return Item.get(Item.FENCE_GATE, 0, 1)
	}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x07)
	}
}