package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * @author Nukkit Project Team
 */
class BlockPressurePlateWood @JvmOverloads constructor(meta: Int = 0) : BlockPressurePlateBase(meta) {
	override val name: String
		get() = "Wooden Pressure Plate"

	override val id: Int
		get() = BlockID.Companion.WOODEN_PRESSURE_PLATE

	override val toolType: Int
		get() = ItemTool.TYPE_AXE

	override val hardness: Double
		get() = 0.5

	override val resistance: Double
		get() = 2.5

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOf(
				toItem()
		)
	}

	override val color: BlockColor
		get() = BlockColor.WOOD_BLOCK_COLOR

	override fun computeRedstoneStrength(): Int {
		val bb = collisionBoundingBox
		for (entity in level.getCollidingEntities(bb)) {
			if (entity.doesTriggerPressurePlate()) {
				return 15
			}
		}
		return 0
	}

	init {
		onPitch = 0.8f
		offPitch = 0.7f
	}
}