package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityFurnace
import cn.nukkit.inventory.ContainerInventory
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.nbt.tag.StringTag
import cn.nukkit.utils.Faceable

/**
 * author: Angelic47
 * Nukkit Project
 */
open class BlockFurnaceBurning @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta), Faceable {
	override val id: Int
		get() = BlockID.Companion.BURNING_FURNACE

	override val name: String
		get() = "Burning Furnace"

	override fun canBeActivated(): Boolean {
		return true
	}

	override val hardness: Double
		get() = 3.5

	override val resistance: Double
		get() = 17.5

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val lightLevel: Int
		get() = 13

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val faces = intArrayOf(2, 5, 3, 4)
		this.setDamage(faces[player?.direction?.horizontalIndex ?: 0])
		getLevel().setBlock(block, this, true, true)
		val nbt = CompoundTag()
				.putList(ListTag("Items"))
				.putString("id", BlockEntity.FURNACE)
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
		val furnace = BlockEntity.createBlockEntity(BlockEntity.FURNACE, getLevel().getChunk(x.toInt() shr 4, z.toInt() shr 4), nbt) as BlockEntityFurnace
		return furnace != null
	}

	override fun onBreak(item: Item): Boolean {
		getLevel().setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true, true)
		return true
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (player != null) {
			val t = getLevel().getBlockEntity(this)
			val furnace: BlockEntityFurnace
			if (t is BlockEntityFurnace) {
				furnace = t
			} else {
				val nbt = CompoundTag()
						.putList(ListTag("Items"))
						.putString("id", BlockEntity.FURNACE)
						.putInt("x", x.toInt())
						.putInt("y", y.toInt())
						.putInt("z", z.toInt())
				furnace = BlockEntity.createBlockEntity(BlockEntity.FURNACE, getLevel().getChunk(x.toInt() shr 4, z.toInt() shr 4), nbt) as BlockEntityFurnace
				if (furnace == null) {
					return false
				}
			}
			if (furnace.namedTag.contains("Lock") && furnace.namedTag["Lock"] is StringTag) {
				if (furnace.namedTag.getString("Lock") != item.customName) {
					return true
				}
			}
			player.addWindow(furnace.inventory)
		}
		return true
	}

	override fun toItem(): Item? {
		return ItemBlock(Block.Companion.get(BlockID.Companion.FURNACE))
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

	override fun hasComparatorInputOverride(): Boolean {
		return true
	}

	override val comparatorInputOverride: Int
		get() {
			val blockEntity = level.getBlockEntity(this)
			return if (blockEntity is BlockEntityFurnace) {
				ContainerInventory.calculateRedstone(blockEntity.inventory)
			} else super.getComparatorInputOverride()
		}

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x7)
	}
}