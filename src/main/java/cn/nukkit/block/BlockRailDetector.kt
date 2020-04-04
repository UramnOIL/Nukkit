package cn.nukkit.block

import cn.nukkit.entity.Entity
import cn.nukkit.entity.item.EntityMinecartAbstract
import cn.nukkit.item.Item
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.math.SimpleAxisAlignedBB

/**
 * Created on 2015/11/22 by CreeperFace.
 * Contributed by: larryTheCoder on 2017/7/8.
 *
 *
 * Nukkit Project,
 * Minecart and Riding Project,
 * Package cn.nukkit.block in project Nukkit.
 */
class BlockRailDetector(meta: Int) : BlockRail(meta) {
	constructor() : this(0) {
		canBePowered = true
	}

	override val id: Int
		get() = BlockID.Companion.DETECTOR_RAIL

	override val name: String
		get() = "Detector Rail"

	override val isPowerSource: Boolean
		get() = true

	override fun getWeakPower(side: BlockFace): Int {
		return if (isActive) 15 else 0
	}

	override fun getStrongPower(side: BlockFace): Int {
		return if (isActive) 0 else if (side == BlockFace.UP) 15 else 0
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_SCHEDULED) {
			updateState()
			return type
		}
		return super.onUpdate(type)
	}

	override fun onEntityCollide(entity: Entity) {
		updateState()
	}

	protected fun updateState() {
		val wasPowered = isActive
		var isPowered = false
		for (entity in level.getNearbyEntities(SimpleAxisAlignedBB(
				floorX + 0.125,
				floorY.toDouble(),
				floorZ + 0.125,
				floorX + 0.875,
				floorY + 0.525,
				floorZ + 0.875))) {
			if (entity is EntityMinecartAbstract) {
				isPowered = true
			}
		}
		if (isPowered && !wasPowered) {
			isActive = true
			level.scheduleUpdate(this, this, 0)
			level.scheduleUpdate(this, this.down(), 0)
		}
		if (!isPowered && wasPowered) {
			isActive = false
			level.scheduleUpdate(this, this, 0)
			level.scheduleUpdate(this, this.down(), 0)
		}
		level.updateComparatorOutputLevel(this)
	}

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOf(
				Item.get(Item.DETECTOR_RAIL, 0, 1)
		)
	}
}