package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.event.Event
import cn.nukkit.event.block.BlockRedstoneEvent
import cn.nukkit.event.entity.EntityInteractEvent
import cn.nukkit.event.player.PlayerInteractEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.Level
import cn.nukkit.level.Sound
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.BlockFace
import cn.nukkit.math.SimpleAxisAlignedBB

/**
 * Created by Snake1999 on 2016/1/11.
 * Package cn.nukkit.block in project nukkit
 */
abstract class BlockPressurePlateBase protected constructor(meta: Int = 0) : BlockFlowable(meta) {
	protected var onPitch = 0f
	protected var offPitch = 0f
	override fun canPassThrough(): Boolean {
		return true
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override fun getMinX(): Double {
		return x + 0.625
	}

	override fun getMinZ(): Double {
		return z + 0.625
	}

	override fun getMinY(): Double {
		return y + 0
	}

	override fun getMaxX(): Double {
		return x + 0.9375
	}

	override fun getMaxZ(): Double {
		return z + 0.9375
	}

	override fun getMaxY(): Double {
		return if (isActivated) y + 0.03125 else y + 0.0625
	}

	override val isPowerSource: Boolean
		get() = true

	val isActivated: Boolean
		get() = this.damage == 0

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			if (this.down().isTransparent) {
				level.useBreakOn(this)
			}
		} else if (type == Level.BLOCK_UPDATE_SCHEDULED) {
			val power = redstonePower
			if (power > 0) {
				updateState(power)
			}
		}
		return 0
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (block.down().isTransparent) {
			return false
		}
		level.setBlock(block, this, true, true)
		return true
	}

	override fun recalculateCollisionBoundingBox(): AxisAlignedBB? {
		return SimpleAxisAlignedBB(x + 0.125, y, z + 0.125, x + 0.875, y + 0.25, z + 0.875)
	}

	override fun onEntityCollide(entity: Entity) {
		val power = redstonePower
		if (power == 0) {
			val ev: Event
			ev = if (entity is Player) {
				PlayerInteractEvent(entity, null, this, null, PlayerInteractEvent.Action.PHYSICAL)
			} else {
				EntityInteractEvent(entity, this)
			}
			level.server.pluginManager.callEvent(ev)
			if (!ev.isCancelled) {
				updateState(power)
			}
		}
	}

	protected fun updateState(oldStrength: Int) {
		val strength = computeRedstoneStrength()
		val wasPowered = oldStrength > 0
		val isPowered = strength > 0
		if (oldStrength != strength) {
			redstonePower = strength
			level.setBlock(this, this, false, false)
			level.updateAroundRedstone(this, null)
			level.updateAroundRedstone(this.location.down(), null)
			if (!isPowered && wasPowered) {
				playOffSound()
				level.server.pluginManager.callEvent(BlockRedstoneEvent(this, 15, 0))
			} else if (isPowered && !wasPowered) {
				playOnSound()
				level.server.pluginManager.callEvent(BlockRedstoneEvent(this, 0, 15))
			}
		}
		if (isPowered) {
			level.scheduleUpdate(this, 20)
		}
	}

	override fun onBreak(item: Item): Boolean {
		level.setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true, true)
		if (redstonePower > 0) {
			level.updateAroundRedstone(this, null)
			level.updateAroundRedstone(this.location.down(), null)
		}
		return true
	}

	override fun getWeakPower(side: BlockFace): Int {
		return redstonePower
	}

	override fun getStrongPower(side: BlockFace): Int {
		return if (side == BlockFace.UP) redstonePower else 0
	}

	var redstonePower: Int
		get() = this.damage
		set(power) {
			this.setDamage(power)
		}

	protected fun playOnSound() {
		level.addSound(this, Sound.RANDOM_CLICK, 0.6f, onPitch)
	}

	protected fun playOffSound() {
		level.addSound(this, Sound.RANDOM_CLICK, 0.6f, offPitch)
	}

	protected abstract fun computeRedstoneStrength(): Int
	override fun toItem(): Item? {
		return ItemBlock(this, 0, 1)
	}
}