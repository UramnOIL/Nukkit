package cn.nukkit.block

import cn.nukkit.entity.EntityLiving
import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * @author Nukkit Project Team
 */
class BlockPressurePlateStone @JvmOverloads constructor(meta: Int = 0) : BlockPressurePlateBase(meta) {
	override val name: String
		get() = "Stone Pressure Plate"

	override val id: Int
		get() = BlockID.Companion.STONE_PRESSURE_PLATE

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

	override val color: BlockColor
		get() = BlockColor.STONE_BLOCK_COLOR

	override fun computeRedstoneStrength(): Int {
		val bb = collisionBoundingBox
		for (entity in level.getCollidingEntities(bb)) {
			if (entity is EntityLiving && entity.doesTriggerPressurePlate()) {
				return 15
			}
		}
		return 0
	}

	init {
		onPitch = 0.6f
		offPitch = 0.5f
	}
}