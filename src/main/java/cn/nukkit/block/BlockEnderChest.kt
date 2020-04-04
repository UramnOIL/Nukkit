package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityEnderChest
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.StringTag
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.Faceable
import java.util.*

class BlockEnderChest @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta), Faceable {
	val viewers: Set<Player> = HashSet()
	override fun canBeActivated(): Boolean {
		return true
	}

	override val id: Int
		get() = BlockID.Companion.ENDER_CHEST

	override val lightLevel: Int
		get() = 7

	override val name: String
		get() = "Chest"

	override val hardness: Double
		get() = 22.5

	override val resistance: Double
		get() = 3000

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override fun getMinX(): Double {
		return x + 0.0625
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
		val faces = intArrayOf(2, 5, 3, 4)
		this.setDamage(faces[player?.direction?.horizontalIndex ?: 0])
		getLevel().setBlock(block, this, true, true)
		val nbt = CompoundTag("")
				.putString("id", BlockEntity.ENDER_CHEST)
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
		val ender = BlockEntity.createBlockEntity(BlockEntity.ENDER_CHEST, getLevel().getChunk(x.toInt() shr 4, z.toInt() shr 4), nbt) as BlockEntityEnderChest
		return ender != null
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (player != null) {
			val top = this.up()
			if (!top!!.isTransparent) {
				return true
			}
			val t = getLevel().getBlockEntity(this)
			val chest: BlockEntityEnderChest
			if (t is BlockEntityEnderChest) {
				chest = t
			} else {
				val nbt = CompoundTag("")
						.putString("id", BlockEntity.ENDER_CHEST)
						.putInt("x", x.toInt())
						.putInt("y", y.toInt())
						.putInt("z", z.toInt())
				chest = BlockEntity.createBlockEntity(BlockEntity.ENDER_CHEST, getLevel().getChunk(x.toInt() shr 4, z.toInt() shr 4), nbt) as BlockEntityEnderChest
				if (chest == null) {
					return false
				}
			}
			if (chest.namedTag.contains("Lock") && chest.namedTag["Lock"] is StringTag) {
				if (chest.namedTag.getString("Lock") != item.customName) {
					return true
				}
			}
			player.viewingEnderChest = this
			player.addWindow(player.enderChestInventory)
		}
		return true
	}

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(
					Item.get(Item.OBSIDIAN, 0, 8)
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override val color: BlockColor
		get() = BlockColor.OBSIDIAN_BLOCK_COLOR

	override fun canBePushed(): Boolean {
		return false
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override fun canSilkTouch(): Boolean {
		return true
	}

	override fun toItem(): Item? {
		return ItemBlock(this, 0)
	}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x07)
	}
}