package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityItemFrame
import cn.nukkit.item.Item
import cn.nukkit.item.ItemItemFrame
import cn.nukkit.level.Level
import cn.nukkit.level.Sound
import cn.nukkit.math.BlockFace
import cn.nukkit.nbt.tag.CompoundTag
import java.util.*

/**
 * Created by Pub4Game on 03.07.2016.
 */
class BlockItemFrame @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta) {
	override val id: Int
		get() = BlockID.Companion.ITEM_FRAME_BLOCK

	override val name: String
		get() = "Item Frame"

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			if (this.getSide(facing!!).isTransparent) {
				level.useBreakOn(this)
				return type
			}
		}
		return 0
	}

	override fun canBeActivated(): Boolean {
		return true
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		val blockEntity = getLevel().getBlockEntity(this)
		val itemFrame = blockEntity as BlockEntityItemFrame
		if (itemFrame.item.id == Item.AIR) {
			val itemOnFrame = item.clone()
			if (player != null && player.isSurvival) {
				itemOnFrame.setCount(itemOnFrame.getCount() - 1)
				player.inventory.itemInHand = itemOnFrame
			}
			itemOnFrame.setCount(1)
			itemFrame.item = itemOnFrame
			getLevel().addSound(this, Sound.BLOCK_ITEMFRAME_ADD_ITEM)
		} else {
			itemFrame.itemRotation = (itemFrame.itemRotation + 1) % 8
			getLevel().addSound(this, Sound.BLOCK_ITEMFRAME_ROTATE_ITEM)
		}
		return true
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (!target.isTransparent && face.index > 1 && !block.isSolid) {
			when (face) {
				BlockFace.NORTH -> this.setDamage(3)
				BlockFace.SOUTH -> this.setDamage(2)
				BlockFace.WEST -> this.setDamage(1)
				BlockFace.EAST -> this.setDamage(0)
				else -> return false
			}
			getLevel().setBlock(block, this, true, true)
			val nbt = CompoundTag()
					.putString("id", BlockEntity.ITEM_FRAME)
					.putInt("x", block.x.toInt())
					.putInt("y", block.y.toInt())
					.putInt("z", block.z.toInt())
					.putByte("ItemRotation", 0)
					.putFloat("ItemDropChance", 1.0f)
			if (item.hasCustomBlockData()) {
				for (aTag in item.customBlockData.allTags) {
					nbt.put(aTag.name, aTag)
				}
			}
			val frame = BlockEntity.createBlockEntity(BlockEntity.ITEM_FRAME, getLevel().getChunk(x.toInt() shr 4, z.toInt() shr 4), nbt) as BlockEntityItemFrame
					?: return false
			getLevel().addSound(this, Sound.BLOCK_ITEMFRAME_PLACE)
			return true
		}
		return false
	}

	override fun onBreak(item: Item): Boolean {
		getLevel().setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true, true)
		getLevel().addSound(this, Sound.BLOCK_ITEMFRAME_REMOVE_ITEM)
		return true
	}

	override fun getDrops(item: Item): Array<Item?> {
		val blockEntity = getLevel().getBlockEntity(this)
		val itemFrame = blockEntity as BlockEntityItemFrame
		val chance = Random().nextInt(100) + 1
		return if (itemFrame != null && chance <= itemFrame.itemDropChance * 100) {
			arrayOf(
					toItem(), itemFrame.item.clone()
			)
		} else {
			arrayOf(
					toItem()
			)
		}
	}

	override fun toItem(): Item? {
		return ItemItemFrame()
	}

	override fun canPassThrough(): Boolean {
		return true
	}

	override fun hasComparatorInputOverride(): Boolean {
		return true
	}

	override val comparatorInputOverride: Int
		get() {
			val blockEntity = level.getBlockEntity(this)
			return if (blockEntity is BlockEntityItemFrame) {
				blockEntity.analogOutput
			} else super.getComparatorInputOverride()
		}

	val facing: BlockFace?
		get() {
			when (this.damage and 3) {
				0 -> return BlockFace.WEST
				1 -> return BlockFace.EAST
				2 -> return BlockFace.NORTH
				3 -> return BlockFace.SOUTH
			}
			return null
		}

	override val hardness: Double
		get() = 0.25
}