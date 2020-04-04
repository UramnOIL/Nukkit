package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.inventory.ShulkerBoxInventoryimport
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

cn.nukkit.item.*
import cn.nukkit.nbt.NBTIO

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockDoorIron @JvmOverloads constructor(meta: Int = 0) : BlockDoor(meta) {
	override val name: String
		get() = "Iron Door Block"

	override val id: Int
		get() = BlockID.Companion.IRON_DOOR_BLOCK

	override fun canBeActivated(): Boolean {
		return true
	}

	override val hardness: Double
		get() = 5

	override val resistance: Double
		get() = 25

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe() && item.getTier() >= ItemTool.TIER_WOODEN) {
			arrayOf<Item?>(
					toItem()
			)
		} else {
			arrayOfNulls<Item>(0)
		}
	}

	override fun toItem(): Item? {
		return ItemDoorIron()
	}

	override val color: BlockColor
		get() = BlockColor.IRON_BLOCK_COLOR

	override fun onActivate(item: Item, player: Player?): Boolean {
		return false
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}