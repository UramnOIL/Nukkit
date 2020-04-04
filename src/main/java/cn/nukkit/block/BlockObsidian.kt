package cn.nukkit.block

import cn.nukkit.inventory.ShulkerBoxInventoryimport
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

cn.nukkit.item.*
import cn.nukkit.nbt.NBTIO

/**
 * Created on 2015/12/2 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockObsidian : BlockSolid() {
	override val name: String
		get() = "Obsidian"

	override val id: Int
		get() = BlockID.Companion.OBSIDIAN

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	//50 in PC
	override val hardness: Double
		get() = 35 //50 in PC

	override val resistance: Double
		get() = 6000

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe() && item.getTier() >= ItemTool.TIER_DIAMOND) {
			arrayOf(
					toItem()
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override fun onBreak(item: Item): Boolean {
		//destroy the nether portal
		val nearby = arrayOf(
				this.up(), this.down(),
				this.north(), south(),
				this.west(), this.east())
		for (aNearby in nearby) {
			if (aNearby != null) if (aNearby.id == BlockID.Companion.NETHER_PORTAL) {
				aNearby.onBreak(item)
			}
		}
		return super.onBreak(item)
	}

	override val color: BlockColor
		get() = BlockColor.OBSIDIAN_BLOCK_COLOR

	override fun canBePushed(): Boolean {
		return false
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}