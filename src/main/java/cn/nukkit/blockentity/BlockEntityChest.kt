package cn.nukkit.blockentity

import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.block.BlockID
import cn.nukkit.inventory.BaseInventory
import cn.nukkit.inventory.ChestInventory
import cn.nukkit.inventory.DoubleChestInventory
import cn.nukkit.inventory.InventoryHolder
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.format.FullChunk
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockEntityChest(chunk: FullChunk?, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt), InventoryHolder, BlockEntityContainer, BlockEntityNameable {
	var realInventory: ChestInventory? = null
		protected set
	protected var doubleInventory: DoubleChestInventory? = null
	override fun initBlockEntity() {
		realInventory = ChestInventory(this)
		if (!namedTag.contains("Items") || namedTag["Items"] !is ListTag<*>) {
			namedTag.putList(ListTag<CompoundTag>("Items"))
		}

		/* for (int i = 0; i < this.getSize(); i++) {
            this.inventory.setItem(i, this.getItem(i));
        } */
		val list = namedTag.getList("Items") as ListTag<CompoundTag>
		for (compound in list.all) {
			val item = NBTIO.getItemHelper(compound)
			realInventory!!.slots[compound.getByte("Slot")] = item
		}
		super.initBlockEntity()
	}

	override fun close() {
		if (!closed) {
			unpair()
			for (player in HashSet(this.inventory.viewers)) {
				player.removeWindow(this.inventory)
			}
			for (player in HashSet(this.inventory.viewers)) {
				player.removeWindow(realInventory)
			}
			super.close()
		}
	}

	override fun onBreak() {
		for (content in realInventory!!.contents.values) {
			level.dropItem(this, content)
		}
		realInventory!!.clearAll() // Stop items from being moved around by another player in the inventory
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
			return blockID == Block.CHEST || blockID == Block.TRAPPED_CHEST
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

		// If item is air or count less than 0, remove the item from the "Items" list
		if (item.id == Item.AIR || item.getCount() <= 0) {
			if (i >= 0) {
				namedTag.getList("Items").remove(i)
			}
		} else if (i < 0) {
			// If it is less than i, then it is a new item, so we are going to add it at the end of the list
			namedTag.getList("Items", CompoundTag::class.java).add(d)
		} else {
			// If it is more than i, then it is an update on a inventorySlot, so we are going to overwrite the item in the list
			namedTag.getList("Items", CompoundTag::class.java).add(i, d)
		}
	}

	override fun getInventory(): BaseInventory {
		if (doubleInventory == null && isPaired) {
			checkPairing()
		}
		return (if (doubleInventory != null) doubleInventory else realInventory)!!
	}

	protected fun checkPairing() {
		val pair = pair
		if (pair != null) {
			if (!pair.isPaired) {
				pair.createPair(this)
				pair.checkPairing()
			}
			if (pair.doubleInventory != null) {
				doubleInventory = pair.doubleInventory
			} else if (doubleInventory == null) {
				if (pair.x + (pair.z.toInt() shl 15) > x + (z.toInt() shl 15)) { //Order them correctly
					doubleInventory = DoubleChestInventory(pair, this)
				} else {
					doubleInventory = DoubleChestInventory(this, pair)
				}
			}
		} else {
			if (level.isChunkLoaded(namedTag.getInt("pairx") shr 4, namedTag.getInt("pairz") shr 4)) {
				doubleInventory = null
				namedTag.remove("pairx")
				namedTag.remove("pairz")
			}
		}
	}

	override fun getName(): String? {
		return if (hasName()) namedTag.getString("CustomName") else "Chest"
	}

	override fun hasName(): Boolean {
		return namedTag.contains("CustomName")
	}

	override fun setName(name: String?) {
		if (name == null || name == "") {
			namedTag.remove("CustomName")
			return
		}
		namedTag.putString("CustomName", name)
	}

	val isPaired: Boolean
		get() = namedTag.contains("pairx") && namedTag.contains("pairz")

	val pair: BlockEntityChest?
		get() {
			if (isPaired) {
				val blockEntity = getLevel().getBlockEntityIfLoaded(Vector3(namedTag.getInt("pairx").toDouble(), y, namedTag.getInt("pairz").toDouble()))
				if (blockEntity is BlockEntityChest) {
					return blockEntity
				}
			}
			return null
		}

	fun pairWith(chest: BlockEntityChest): Boolean {
		if (isPaired || chest.isPaired || this.block.id != chest.block.id) {
			return false
		}
		createPair(chest)
		chest.spawnToAll()
		spawnToAll()
		checkPairing()
		return true
	}

	fun createPair(chest: BlockEntityChest) {
		namedTag.putInt("pairx", chest.x.toInt())
		namedTag.putInt("pairz", chest.z.toInt())
		chest.namedTag.putInt("pairx", x.toInt())
		chest.namedTag.putInt("pairz", z.toInt())
	}

	fun unpair(): Boolean {
		if (!isPaired) {
			return false
		}
		val chest = pair
		doubleInventory = null
		namedTag.remove("pairx")
		namedTag.remove("pairz")
		spawnToAll()
		if (chest != null) {
			chest.namedTag.remove("pairx")
			chest.namedTag.remove("pairz")
			chest.doubleInventory = null
			chest.checkPairing()
			chest.spawnToAll()
		}
		checkPairing()
		return true
	}

	override val spawnCompound: CompoundTag?
		get() {
			val c: CompoundTag
			c = if (isPaired) {
				CompoundTag()
						.putString("id", BlockEntity.Companion.CHEST)
						.putInt("x", x.toInt())
						.putInt("y", y.toInt())
						.putInt("z", z.toInt())
						.putInt("pairx", namedTag.getInt("pairx"))
						.putInt("pairz", namedTag.getInt("pairz"))
			} else {
				CompoundTag()
						.putString("id", BlockEntity.Companion.CHEST)
						.putInt("x", x.toInt())
						.putInt("y", y.toInt())
						.putInt("z", z.toInt())
			}
			if (hasName()) {
				c.put("CustomName", namedTag["CustomName"])
			}
			return c
		}
}