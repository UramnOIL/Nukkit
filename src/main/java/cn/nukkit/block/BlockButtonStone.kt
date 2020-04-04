package cn.nukkit.block

import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator

/**
 * Created by CreeperFace on 27. 11. 2016.
 */
class BlockButtonStone @JvmOverloads constructor(meta: Int = 0) : BlockButton(meta) {
	override val id: Int
		get() = BlockID.Companion.STONE_BUTTON

	override val name: String
		get() = "Stone Button"

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE
}