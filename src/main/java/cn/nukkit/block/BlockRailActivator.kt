package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.level.Level
import cn.nukkit.math.Vector3
import cn.nukkit.utils.Rail

/**
 * @author Nukkit Project Team
 */
class BlockRailActivator(meta: Int) : BlockRail(meta) {
	constructor() : this(0) {
		canBePowered = true
	}

	override val name: String
		get() = "Activator Rail"

	override val id: Int
		get() = BlockID.Companion.ACTIVATOR_RAIL

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL || type == Level.BLOCK_UPDATE_REDSTONE || type == Level.BLOCK_UPDATE_SCHEDULED) {
			super.onUpdate(type)
			val wasPowered = isActive
			val isPowered = (level.isBlockPowered(this.location)
					|| checkSurrounding(this, true, 0)
					|| checkSurrounding(this, false, 0))
			var hasUpdate = false
			if (wasPowered != isPowered) {
				isActive = isPowered
				hasUpdate = true
			}
			if (hasUpdate) {
				level.updateAround(down())
				if (orientation.isAscending) {
					level.updateAround(up())
				}
			}
			return type
		}
		return 0
	}

	/**
	 * Check the surrounding of the rail
	 *
	 * @param pos      The rail position
	 * @param relative The relative of the rail that will be checked
	 * @param power    The count of the rail that had been counted
	 * @return Boolean of the surrounding area. Where the powered rail on!
	 */
	protected fun checkSurrounding(pos: Vector3, relative: Boolean, power: Int): Boolean {
		if (power >= 8) {
			return false
		}
		var dx = pos.floorX
		var dy = pos.floorY
		var dz = pos.floorZ
		val block: BlockRail
		val block2 = level.getBlock(Vector3(dx.toDouble(), dy.toDouble(), dz.toDouble()))
		block = if (Rail.isRailBlock(block2)) {
			block2 as BlockRail
		} else {
			return false
		}
		var base: Rail.Orientation? = null
		var onStraight = true
		when (block.orientation) {
			Rail.Orientation.STRAIGHT_NORTH_SOUTH -> if (relative) {
				dz++
			} else {
				dz--
			}
			Rail.Orientation.STRAIGHT_EAST_WEST -> if (relative) {
				dx--
			} else {
				dx++
			}
			Rail.Orientation.ASCENDING_EAST -> {
				if (relative) {
					dx--
				} else {
					dx++
					dy++
					onStraight = false
				}
				base = Rail.Orientation.STRAIGHT_EAST_WEST
			}
			Rail.Orientation.ASCENDING_WEST -> {
				if (relative) {
					dx--
					dy++
					onStraight = false
				} else {
					dx++
				}
				base = Rail.Orientation.STRAIGHT_EAST_WEST
			}
			Rail.Orientation.ASCENDING_NORTH -> {
				if (relative) {
					dz++
				} else {
					dz--
					dy++
					onStraight = false
				}
				base = Rail.Orientation.STRAIGHT_NORTH_SOUTH
			}
			Rail.Orientation.ASCENDING_SOUTH -> {
				if (relative) {
					dz++
					dy++
					onStraight = false
				} else {
					dz--
				}
				base = Rail.Orientation.STRAIGHT_NORTH_SOUTH
			}
			else -> return false
		}
		return (canPowered(Vector3(dx.toDouble(), dy.toDouble(), dz.toDouble()), base, power, relative)
				|| onStraight && canPowered(Vector3(dx.toDouble(), (dy - 1).toDouble(), dz.toDouble()), base, power, relative))
	}

	protected fun canPowered(pos: Vector3, state: Rail.Orientation?, power: Int, relative: Boolean): Boolean {
		val block = level.getBlock(pos) as? BlockRailActivator ?: return false
		val base = block.orientation
		return ((state != Rail.Orientation.STRAIGHT_EAST_WEST
				|| base != Rail.Orientation.STRAIGHT_NORTH_SOUTH && base != Rail.Orientation.ASCENDING_NORTH && base != Rail.Orientation.ASCENDING_SOUTH)
				&& (state != Rail.Orientation.STRAIGHT_NORTH_SOUTH
				|| base != Rail.Orientation.STRAIGHT_EAST_WEST && base != Rail.Orientation.ASCENDING_EAST && base != Rail.Orientation.ASCENDING_WEST)
				&& (level.isBlockPowered(pos) || checkSurrounding(pos, relative, power + 1)))
	}

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOf(
				Item.get(Item.ACTIVATOR_RAIL, 0, 1)
		)
	}
}