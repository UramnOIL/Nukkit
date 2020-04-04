package cn.nukkit.block

import cn.nukkit.inventory.ShulkerBoxInventoryimport
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

cn.nukkit.item.*
import cn.nukkit.nbt.NBTIO

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class BlockDoorWood @JvmOverloads constructor(meta: Int = 0) : BlockDoor(meta) {
	override val name: String
		get() = "Wood Door Block"

	override val id: Int
		get() = BlockID.Companion.WOOD_DOOR_BLOCK

	override val hardness: Double
		get() = 3

	override val resistance: Double
		get() = 15

	override val toolType: Int
		get() = ItemTool.TYPE_AXE

	override fun toItem(): Item? {
		return ItemDoorWood()
	}

	override val color: BlockColor
		get() = BlockColor.WOOD_BLOCK_COLOR
}