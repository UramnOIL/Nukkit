package cn.nukkit.block

import cn.nukkit.inventory.ShulkerBoxInventoryimport
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

cn.nukkit.item.*
import cn.nukkit.nbt.NBTIO

class BlockDoorAcacia @JvmOverloads constructor(meta: Int = 0) : BlockDoorWood(meta) {
	override val name: String
		get() = "Acacia Door Block"

	override val id: Int
		get() = BlockID.Companion.ACACIA_DOOR_BLOCK

	override fun toItem(): Item? {
		return ItemDoorAcacia()
	}

	override val color: BlockColor
		get() = BlockColor.ORANGE_BLOCK_COLOR
}