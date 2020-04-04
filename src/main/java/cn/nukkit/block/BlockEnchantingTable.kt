package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityEnchantTable
import cn.nukkit.inventory.EnchantInventory
import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.nbt.tag.StringTag
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/11/22 by CreeperFace.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockEnchantingTable : BlockTransparent() {
	override val id: Int
		get() = BlockID.Companion.ENCHANTING_TABLE

	override val name: String
		get() = "Enchanting Table"

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val hardness: Double
		get() = 5

	override val resistance: Double
		get() = 6000

	override val lightLevel: Int
		get() = 12

	override fun canBeActivated(): Boolean {
		return true
	}

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(
					toItem()
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		getLevel().setBlock(block, this, true, true)
		val nbt = CompoundTag()
				.putString("id", BlockEntity.ENCHANT_TABLE)
				.putInt("x", x.toInt())
				.putInt("y", y.toInt())
				.putInt("z", z.toInt())
		if (item.hasCustomName()) {
			nbt.putString("CustomName", item.customName)
		}
		if (item.hasCustomBlockData()) {
			val customData = item.customBlockData.tags
			for ((key, value) in customData) {
				nbt.put(key, value)
			}
		}
		val enchantTable = BlockEntity.createBlockEntity(BlockEntity.ENCHANT_TABLE, getLevel().getChunk(x.toInt() shr 4, z.toInt() shr 4), nbt) as BlockEntityEnchantTable
		return enchantTable != null
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (player != null) {
			val t = getLevel().getBlockEntity(this)
			val enchantTable: BlockEntityEnchantTable
			if (t is BlockEntityEnchantTable) {
				enchantTable = t
			} else {
				val nbt = CompoundTag()
						.putList(ListTag("Items"))
						.putString("id", BlockEntity.ENCHANT_TABLE)
						.putInt("x", x.toInt())
						.putInt("y", y.toInt())
						.putInt("z", z.toInt())
				enchantTable = BlockEntity.createBlockEntity(BlockEntity.ENCHANT_TABLE, getLevel().getChunk(x.toInt() shr 4, z.toInt() shr 4), nbt) as BlockEntityEnchantTable
				if (enchantTable == null) {
					return false
				}
			}
			if (enchantTable.namedTag.contains("Lock") && enchantTable.namedTag["Lock"] is StringTag) {
				if (enchantTable.namedTag.getString("Lock") != item.customName) {
					return true
				}
			}
			player.addWindow(EnchantInventory(player.uIInventory, this.location), Player.ENCHANT_WINDOW_ID)
		}
		return true
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override val color: BlockColor
		get() = BlockColor.RED_BLOCK_COLOR
}