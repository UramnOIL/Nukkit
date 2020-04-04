package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityChest
import cn.nukkit.inventory.ContainerInventory
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.nbt.tag.StringTag
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.Faceable

/**
 * author: Angelic47
 * Nukkit Project
 */
open class BlockChest @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta), Faceable {
	override fun canBeActivated(): Boolean {
		return true
	}

	override val id: Int
		get() = BlockID.Companion.CHEST

	override val name: String
		get() = "Chest"

	override val hardness: Double
		get() = 2.5

	override val resistance: Double
		get() = 12.5

	override val toolType: Int
		get() = ItemTool.TYPE_AXE

	override fun getMinX(): Double {
		return x + 0.0625
	}

	override fun getMinY(): Double {
		return y
	}

	override fun getMinZ(): Double {
		return z + 0.0625
	}

	override fun getMaxX(): Double {
		return x + 0.9375
	}

	override fun getMaxY(): Double {
		return y + 0.9475
	}

	override fun getMaxZ(): Double {
		return z + 0.9375
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		var chest: BlockEntityChest? = null
		val faces = intArrayOf(2, 5, 3, 4)
		this.setDamage(faces[player?.direction?.horizontalIndex ?: 0])
		for (side in 2..5) {
			if ((this.damage == 4 || this.damage == 5) && (side == 4 || side == 5)) {
				continue
			} else if ((this.damage == 3 || this.damage == 2) && (side == 2 || side == 3)) {
				continue
			}
			val c = this.getSide(BlockFace.fromIndex(side))
			if (c is BlockChest && c.getDamage() == this.damage) {
				val blockEntity = getLevel().getBlockEntity(c)
				if (blockEntity is BlockEntityChest && !blockEntity.isPaired) {
					chest = blockEntity
					break
				}
			}
		}
		getLevel().setBlock(block, this, true, true)
		val nbt = CompoundTag("")
				.putList(ListTag("Items"))
				.putString("id", BlockEntity.CHEST)
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
		val blockEntity = BlockEntity.createBlockEntity(BlockEntity.CHEST, getLevel().getChunk(x.toInt() shr 4, z.toInt() shr 4), nbt) as BlockEntityChest
				?: return false
		if (chest != null) {
			chest.pairWith(blockEntity)
			blockEntity.pairWith(chest)
		}
		return true
	}

	override fun onBreak(item: Item): Boolean {
		val t = getLevel().getBlockEntity(this)
		if (t is BlockEntityChest) {
			t.unpair()
		}
		getLevel().setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true, true)
		return true
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (player != null) {
			val top = up()
			if (!top!!.isTransparent) {
				return true
			}
			val t = getLevel().getBlockEntity(this)
			val chest: BlockEntityChest
			if (t is BlockEntityChest) {
				chest = t
			} else {
				val nbt = CompoundTag("")
						.putList(ListTag("Items"))
						.putString("id", BlockEntity.CHEST)
						.putInt("x", x.toInt())
						.putInt("y", y.toInt())
						.putInt("z", z.toInt())
				chest = BlockEntity.createBlockEntity(BlockEntity.CHEST, getLevel().getChunk(x.toInt() shr 4, z.toInt() shr 4), nbt) as BlockEntityChest
				if (chest == null) {
					return false
				}
			}
			if (chest.namedTag.contains("Lock") && chest.namedTag["Lock"] is StringTag) {
				if (chest.namedTag.getString("Lock") != item.customName) {
					return true
				}
			}
			player.addWindow(chest.inventory)
		}
		return true
	}

	override val color: BlockColor
		get() = BlockColor.WOOD_BLOCK_COLOR

	override fun hasComparatorInputOverride(): Boolean {
		return true
	}

	override val comparatorInputOverride: Int
		get() {
			val blockEntity = level.getBlockEntity(this)
			return if (blockEntity is BlockEntityChest) {
				ContainerInventory.calculateRedstone(blockEntity.inventory)
			} else super.getComparatorInputOverride()
		}

	override fun toItem(): Item? {
		return ItemBlock(this, 0)
	}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x7)
	}
}