package cn.nukkit.blockentity

import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.block.BlockID
import cn.nukkit.inventory.BaseInventory
import cn.nukkit.inventory.InventoryHolder
import cn.nukkit.inventory.ShulkerBoxInventory
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import java.util.*

/**
 * Created by PetteriM1
 */
class BlockEntityShulkerBox(chunk: FullChunk?, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt), InventoryHolder, BlockEntityContainer, BlockEntityNameable {
	var realInventory: ShulkerBoxInventory? = null
		protected set

	override fun initBlockEntity() {
		realInventory = ShulkerBoxInventory(this)
		if (!namedTag.contains("Items") || namedTag["Items"] !is ListTag<*>) {
			namedTag.putList(ListTag<CompoundTag>("Items"))
		}
		val list = namedTag.getList("Items") as ListTag<CompoundTag>
		for (compound in list.all) {
			val item = NBTIO.getItemHelper(compound)
			realInventory!!.slots[compound.getByte("Slot")] = item
		}
		if (!namedTag.contains("facing")) {
			namedTag.putByte("facing", 0)
		}
		super.initBlockEntity()
	}

	override fun close() {
		if (!closed) {
			for (player in HashSet(this.inventory.viewers)) {
				player.removeWindow(this.inventory)
			}
			for (player in HashSet(this.inventory.viewers)) {
				player.removeWindow(realInventory)
			}
			super.close()
		}
	}

	override fun saveNBT() {
		namedTag.putList(ListTag<CompoundTag>("Items"))
		for (index in 0 until size) {
			setItem(index, realInventory!!.getItem(index))
		}
	}

	override val isBlockEntityValid: Boolean
		get() {
			val blockID = this.block.id
			return blockID == Block.SHULKER_BOX || blockID == Block.UNDYED_SHULKER_BOX
		}

	override val size: Int
		get() = 27

	protected fun getSlotIndex(index: Int): Int {
		val list = namedTag.getList("Items", CompoundTag::class.java)
		for (i in 0 until list.size()) {
			if (list[i].getByte("Slot") == index) {
				return i
			}
		}
		return -1
	}

	override fun getItem(index: Int): Item {
		val i = getSlotIndex(index)
		return if (i < 0) {
			ItemBlock(get(BlockID.AIR), 0, 0)
		} else {
			val data = namedTag.getList("Items")[i] as CompoundTag
			NBTIO.getItemHelper(data)
		}
	}

	override fun setItem(index: Int, item: Item) {
		val i = getSlotIndex(index)
		val d = NBTIO.putItemHelper(item, index)
		if (item.id == Item.AIR || item.getCount() <= 0) {
			if (i >= 0) {
				namedTag.getList("Items").remove(i)
			}
		} else if (i < 0) {
			namedTag.getList("Items", CompoundTag::class.java).add(d)
		} else {
			namedTag.getList("Items", CompoundTag::class.java).add(i, d)
		}
	}

	override fun getInventory(): BaseInventory {
		return realInventory!!
	}

	override fun getName(): String? {
		return if (hasName()) namedTag.getString("CustomName") else "Shulker Box"
	}

	override fun hasName(): Boolean {
		return namedTag.contains("CustomName")
	}

	override fun setName(name: String?) {
		if (name == null || name.isEmpty()) {
			namedTag.remove("CustomName")
			return
		}
		namedTag.putString("CustomName", name)
	}

	override val spawnCompound: CompoundTag?
		get() {
			val c: CompoundTag = BlockEntity.Companion.getDefaultCompound(this, BlockEntity.Companion.SHULKER_BOX)
					.putByte("facing", namedTag.getByte("facing"))
			if (hasName()) {
				c.put("CustomName", namedTag["CustomName"])
			}
			return c
		}
}