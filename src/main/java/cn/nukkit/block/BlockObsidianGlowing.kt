package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator

/**
 * Created on 2015/11/22 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockObsidianGlowing : BlockSolid() {
	override val id: Int
		get() = BlockID.Companion.GLOWING_OBSIDIAN

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val name: String
		get() = "Glowing Obsidian"

	override val hardness: Double
		get() = 50

	override val resistance: Double
		get() = 6000

	override val lightLevel: Int
		get() = 12

	override fun toItem(): Item? {
		return ItemBlock(Block.Companion.get(BlockID.Companion.OBSIDIAN))
	}

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier > ItemTool.DIAMOND_PICKAXE) {
			arrayOf(
					toItem()
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override fun canBePushed(): Boolean {
		return false
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}