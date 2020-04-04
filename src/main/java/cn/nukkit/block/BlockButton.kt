package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.event.block.BlockRedstoneEvent
import cn.nukkit.item.Item
import cn.nukkit.level.Level
import cn.nukkit.level.Sound
import cn.nukkit.math.BlockFace
import cn.nukkit.math.Vector3
import cn.nukkit.utils.Faceable

/**
 * Created by CreeperFace on 27. 11. 2016.
 */
abstract class BlockButton @JvmOverloads constructor(meta: Int = 0) : BlockFlowable(meta), Faceable {
	override val resistance: Double
		get() = 2.5

	override val hardness: Double
		get() = 0.5

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (target.isTransparent) {
			return false
		}
		this.setDamage(face.index)
		level.setBlock(block, this, true, true)
		return true
	}

	override fun canBeActivated(): Boolean {
		return true
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (isActivated) {
			return false
		}
		level.server.pluginManager.callEvent(BlockRedstoneEvent(this, 0, 15))
		this.setDamage(this.damage xor 0x08)
		level.setBlock(this, this, true, false)
		level.addSound(this.add(0.5, 0.5, 0.5), Sound.RANDOM_CLICK)
		level.scheduleUpdate(this, 30)
		val pos: Vector3 = location
		level.updateAroundRedstone(pos, null)
		level.updateAroundRedstone(pos.getSide(facing.opposite), null)
		return true
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			if (this.getSide(facing.opposite).isTransparent) {
				level.useBreakOn(this)
				return Level.BLOCK_UPDATE_NORMAL
			}
		} else if (type == Level.BLOCK_UPDATE_SCHEDULED) {
			if (isActivated) {
				level.server.pluginManager.callEvent(BlockRedstoneEvent(this, 15, 0))
				this.setDamage(this.damage xor 0x08)
				level.setBlock(this, this, true, false)
				level.addSound(this.add(0.5, 0.5, 0.5), Sound.RANDOM_CLICK)
				val pos: Vector3 = location
				level.updateAroundRedstone(pos, null)
				level.updateAroundRedstone(pos.getSide(facing.opposite), null)
			}
			return Level.BLOCK_UPDATE_SCHEDULED
		}
		return 0
	}

	val isActivated: Boolean
		get() = this.damage and 0x08 == 0x08

	override val isPowerSource: Boolean
		get() = true

	override fun getWeakPower(side: BlockFace): Int {
		return if (isActivated) 15 else 0
	}

	override fun getStrongPower(side: BlockFace): Int {
		return if (!isActivated) 0 else if (facing == side) 15 else 0
	}

	val facing: BlockFace
		get() {
			val side = if (isActivated) damage xor 0x08 else damage
			return BlockFace.fromIndex(side)
		}

	override fun onBreak(item: Item): Boolean {
		if (isActivated) {
			level.server.pluginManager.callEvent(BlockRedstoneEvent(this, 15, 0))
		}
		return super.onBreak(item)
	}

	override fun toItem(): Item? {
		return Item.get(this.id, 5)
	}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x7)
	}
}