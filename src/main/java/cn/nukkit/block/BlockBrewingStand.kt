package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityBrewingStand
import cn.nukkit.inventory.ContainerInventory
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBrewingStand
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.nbt.tag.StringTag
import cn.nukkit.utils.BlockColor

class BlockBrewingStand @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta) {
	override val name: String
		get() = "Brewing Stand"

	override fun canBeActivated(): Boolean {
		return true
	}

	override val hardness: Double
		get() = 0.5

	override val resistance: Double
		get() = 2.5

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val id: Int
		get() = BlockID.Companion.BREWING_STAND_BLOCK

	override val lightLevel: Int
		get() = 1

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (!block.down().isTransparent) {
			getLevel().setBlock(block, this, true, true)
			val nbt = CompoundTag()
					.putList(ListTag("Items"))
					.putString("id", BlockEntity.BREWING_STAND)
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
			val brewing = BlockEntity.createBlockEntity(BlockEntity.BREWING_STAND, getLevel().getChunk(x.toInt() shr 4, z.toInt() shr 4), nbt) as BlockEntityBrewingStand
			return brewing != null
		}
		return false
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (player != null) {
			val t = getLevel().getBlockEntity(this)
			val brewing: BlockEntityBrewingStand
			if (t is BlockEntityBrewingStand) {
				brewing = t
			} else {
				val nbt = CompoundTag()
						.putList(ListTag("Items"))
						.putString("id", BlockEntity.BREWING_STAND)
						.putInt("x", x.toInt())
						.putInt("y", y.toInt())
						.putInt("z", z.toInt())
				brewing = BlockEntity.createBlockEntity(BlockEntity.BREWING_STAND, getLevel().getChunk(x.toInt() shr 4, z.toInt() shr 4), nbt) as BlockEntityBrewingStand
				if (brewing == null) {
					return false
				}
			}
			if (brewing.namedTag.contains("Lock") && brewing.namedTag["Lock"] is StringTag) {
				if (brewing.namedTag.getString("Lock") != item.customName) {
					return false
				}
			}
			player.addWindow(brewing.inventory)
		}
		return true
	}

	override fun toItem(): Item? {
		return ItemBrewingStand()
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

	override val color: BlockColor
		get() = BlockColor.IRON_BLOCK_COLOR

	override fun hasComparatorInputOverride(): Boolean {
		return true
	}

	override val comparatorInputOverride: Int
		get() {
			val blockEntity = level.getBlockEntity(this)
			return if (blockEntity is BlockEntityBrewingStand) {
				ContainerInventory.calculateRedstone(blockEntity.inventory)
			} else super.getComparatorInputOverride()
		}

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}