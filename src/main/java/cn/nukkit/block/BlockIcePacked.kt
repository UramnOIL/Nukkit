package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockIcePacked : BlockIce() {
	override val id: Int
		get() = BlockID.Companion.PACKED_ICE

	override val name: String
		get() = "Packed Ice"

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override fun onUpdate(type: Int): Int {
		return 0 //not being melted
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override fun onBreak(item: Item): Boolean {
		getLevel().setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true) //no water
		return true
	}

	override fun canSilkTouch(): Boolean {
		return true
	}
}