package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/12/5 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockCraftingTable : BlockSolid() {
	override val name: String
		get() = "Crafting Table"

	override val id: Int
		get() = BlockID.Companion.WORKBENCH

	override fun canBeActivated(): Boolean {
		return true
	}

	override val hardness: Double
		get() = 2.5

	override val resistance: Double
		get() = 15

	override val toolType: Int
		get() = ItemTool.TYPE_AXE

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (player != null) {
			player.craftingType = Player.CRAFTING_BIG
			player.setCraftingGrid(player.uIInventory!!.bigCraftingGrid)
		}
		return true
	}

	override val color: BlockColor
		get() = BlockColor.WOOD_BLOCK_COLOR
}