package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.math.NukkitMath
import cn.nukkit.utils.BlockColor

/**
 * @author CreeperFace
 */
class BlockWeightedPressurePlateLight @JvmOverloads constructor(meta: Int = 0) : BlockPressurePlateBase(meta) {
	override val id: Int
		get() = BlockID.Companion.LIGHT_WEIGHTED_PRESSURE_PLATE

	override val name: String
		get() = "Weighted Pressure Plate (Light)"

	override val hardness: Double
		get() = 0.5

	override val resistance: Double
		get() = 2.5

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(
					toItem()
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override fun toItem(): Item? {
		return ItemBlock(this, 0)
	}

	override val color: BlockColor
		get() = BlockColor.GOLD_BLOCK_COLOR

	override fun computeRedstoneStrength(): Int {
		val count = Math.min(level.getCollidingEntities(collisionBoundingBox).size, maxWeight)
		return if (count > 0) {
			val f = Math.min(maxWeight, count).toFloat() / maxWeight.toFloat()
			NukkitMath.ceilFloat(f * 15.0f)
		} else {
			0
		}
	}

	val maxWeight: Int
		get() = 15

	init {
		onPitch = 0.90000004f
		offPitch = 0.75f
	}
}