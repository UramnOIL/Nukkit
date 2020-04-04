package cn.nukkit.block

import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator

/**
 * Created by CreeperFace on 27. 11. 2016.
 */
class BlockButtonWooden @JvmOverloads constructor(meta: Int = 0) : BlockButton(meta) {
	override val id: Int
		get() = BlockID.Companion.WOODEN_BUTTON

	override val name: String
		get() = "Wooden Button"

	override val toolType: Int
		get() = ItemTool.TYPE_AXE
}