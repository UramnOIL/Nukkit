package cn.nukkit.block

import cn.nukkit.inventory.ShulkerBoxInventoryimport
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

cn.nukkit.item.*
import cn.nukkit.nbt.NBTIO

class BlockDoorDarkOak @JvmOverloads constructor(meta: Int = 0) : BlockDoorWood(meta) {
	override val name: String
		get() = "Dark Oak Door Block"

	override val id: Int
		get() = BlockID.Companion.DARK_OAK_DOOR_BLOCK

	override fun toItem(): Item? {
		return ItemDoorDarkOak()
	}

	override val color: BlockColor
		get() = BlockColor.BROWN_BLOCK_COLOR
}