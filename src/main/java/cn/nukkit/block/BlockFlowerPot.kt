package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityFlowerPot
import cn.nukkit.item.Item
import cn.nukkit.item.ItemFlowerPot
import cn.nukkit.level.generator
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.BlockFace
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author Nukkit Project Team
 */
class BlockFlowerPot @JvmOverloads constructor(meta: Int = 0) : BlockFlowable(meta) {
	override val name: String
		get() = "Flower Pot"

	override val id: Int
		get() = BlockID.Companion.FLOWER_POT_BLOCK

	override val hardness: Double
		get() = 0

	override val resistance: Double
		get() = 0

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (face != BlockFace.UP) return false
		val nbt = CompoundTag()
				.putString("id", BlockEntity.FLOWER_POT)
				.putInt("x", x.toInt())
				.putInt("y", y.toInt())
				.putInt("z", z.toInt())
				.putShort("item", 0)
				.putInt("data", 0)
		if (item.hasCustomBlockData()) {
			for (aTag in item.customBlockData.allTags) {
				nbt.put(aTag.name, aTag)
			}
		}
		val flowerPot = BlockEntity.createBlockEntity(BlockEntity.FLOWER_POT, getLevel().getChunk(block.x.toInt() shr 4, block.z.toInt() shr 4), nbt) as BlockEntityFlowerPot
				?: return false
		getLevel().setBlock(block, this, true, true)
		return true
	}

	override fun canBeActivated(): Boolean {
		return true
	}

	override fun onActivate(item: Item): Boolean {
		return this.onActivate(item, null)
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		val blockEntity = getLevel().getBlockEntity(this) as? BlockEntityFlowerPot ?: return false
		if (blockEntity.namedTag.getShort("item") != 0 || blockEntity.namedTag.getInt("mData") != 0) return false
		val itemID: Int
		val itemMeta: Int
		if (!canPlaceIntoFlowerPot(item.id)) {
			if (!canPlaceIntoFlowerPot(item.block.id)) {
				return true
			}
			itemID = item.block.id
			itemMeta = item.damage
		} else {
			itemID = item.id
			itemMeta = item.damage
		}
		blockEntity.namedTag.putShort("item", itemID)
		blockEntity.namedTag.putInt("data", itemMeta)
		this.setDamage(1)
		getLevel().setBlock(this, this, true)
		blockEntity.spawnToAll()
		if (player!!.isSurvival) {
			item.setCount(item.getCount() - 1)
			player.inventory.itemInHand = if (item.getCount() > 0) item else Item.get(Item.AIR)
		}
		return true
	}

	override fun getDrops(item: Item): Array<Item?> {
		var dropInside = false
		var insideID = 0
		var insideMeta = 0
		val blockEntity = getLevel().getBlockEntity(this)
		if (blockEntity is BlockEntityFlowerPot) {
			dropInside = true
			insideID = blockEntity.namedTag.getShort("item")
			insideMeta = blockEntity.namedTag.getInt("data")
		}
		return if (dropInside) {
			arrayOf(
					ItemFlowerPot(),
					Item.get(insideID, insideMeta, 1)
			)
		} else {
			arrayOf(
					ItemFlowerPot()
			)
		}
	}

	override fun recalculateBoundingBox(): AxisAlignedBB? {
		return this
	}

	override fun getMinX(): Double {
		return x + 0.3125
	}

	override fun getMinZ(): Double {
		return z + 0.3125
	}

	override fun getMaxX(): Double {
		return x + 0.6875
	}

	override fun getMaxY(): Double {
		return y + 0.375
	}

	override fun getMaxZ(): Double {
		return z + 0.6875
	}

	override fun canPassThrough(): Boolean {
		return false
	}

	override fun toItem(): Item? {
		return ItemFlowerPot()
	}

	companion object {
		protected fun canPlaceIntoFlowerPot(id: Int): Boolean {
			return when (id) {
				BlockID.Companion.SAPLING, BlockID.Companion.DEAD_BUSH, BlockID.Companion.DANDELION, BlockID.Companion.ROSE, BlockID.Companion.RED_MUSHROOM, BlockID.Companion.BROWN_MUSHROOM, BlockID.Companion.CACTUS ->                 // TODO: 2016/2/4 case NETHER_WART:
					true
				else -> false
			}
		}
	}
}