package cn.nukkit.blockentity

import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.block.BlockID
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag

/**
 * Created by Pub4Game on 03.07.2016.
 */
class BlockEntityItemFrame(chunk: FullChunk?, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
	override fun initBlockEntity() {
		if (!namedTag.contains("Item")) {
			namedTag.putCompound("Item", NBTIO.putItemHelper(ItemBlock(get(BlockID.AIR))))
		}
		if (!namedTag.contains("ItemRotation")) {
			namedTag.putByte("ItemRotation", 0)
		}
		if (!namedTag.contains("ItemDropChance")) {
			namedTag.putFloat("ItemDropChance", 1.0f)
		}
		level.updateComparatorOutputLevel(this)
		super.initBlockEntity()
	}

	override fun getName(): String? {
		return "Item Frame"
	}

	override val isBlockEntityValid: Boolean
		get() = this.block.id == Block.ITEM_FRAME_BLOCK

	var itemRotation: Int
		get() = namedTag.getByte("ItemRotation")
		set(itemRotation) {
			namedTag.putByte("ItemRotation", itemRotation)
			level.updateComparatorOutputLevel(this)
			setDirty()
		}

	var item: Item?
		get() {
			val NBTTag = namedTag.getCompound("Item")
			return NBTIO.getItemHelper(NBTTag)
		}
		set(item) {
			setItem(item, true)
		}

	fun setItem(item: Item?, setChanged: Boolean) {
		namedTag.putCompound("Item", NBTIO.putItemHelper(item))
		if (setChanged) {
			setDirty()
		}
		level.updateComparatorOutputLevel(this)
	}

	var itemDropChance: Float
		get() = namedTag.getFloat("ItemDropChance")
		set(chance) {
			namedTag.putFloat("ItemDropChance", chance)
		}

	override fun setDirty() {
		spawnToAll()
		super.setDirty()
	}

	override val spawnCompound: CompoundTag?
		get() {
			if (!namedTag.contains("Item")) {
				setItem(ItemBlock(get(BlockID.AIR)), false)
			}
			val item = namedTag.getCompound("Item").copy()
			item.name = "Item"
			val tag = CompoundTag()
					.putString("id", BlockEntity.Companion.ITEM_FRAME)
					.putInt("x", x.toInt())
					.putInt("y", y.toInt())
					.putInt("z", z.toInt())
			if (item.getShort("id") != Item.AIR) {
				tag.putCompound("Item", item)
						.putByte("ItemRotation", itemRotation)
			}
			return tag
		}

	val analogOutput: Int
		get() = if (item == null || item!!.id == 0) 0 else itemRotation % 8 + 1
}