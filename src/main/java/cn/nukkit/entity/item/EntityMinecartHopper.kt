package cn.nukkit.entity.item

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.entity.Entity
import cn.nukkit.inventory.InventoryHolder
import cn.nukkit.inventory.MinecartHopperInventory
import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.utils.MinecartType

class EntityMinecartHopper(chunk: FullChunk?, nbt: CompoundTag?) : EntityMinecartAbstract(chunk, nbt), InventoryHolder {
	protected var inventory: MinecartHopperInventory? = null
	override val type: MinecartType
		get() = MinecartType.valueOf(5)

	override val isRideable: Boolean
		get() = false

	override fun dropItem() {
		super.dropItem()
		level.dropItem(this, Item.get(Item.HOPPER))
		for (item in inventory!!.contents.values) {
			level.dropItem(this, item)
		}
	}

	override fun mountEntity(entity: Entity, mode: Byte): Boolean {
		return false
	}

	override fun onInteract(player: Player, item: Item, clickedPos: Vector3?): Boolean {
		player.addWindow(inventory)
		return true
	}

	override fun getInventory(): MinecartHopperInventory {
		return inventory!!
	}

	override fun initEntity() {
		super.initEntity()
		inventory = MinecartHopperInventory(this)
		if (namedTag!!.contains("Items") && namedTag!!["Items"] is ListTag<*>) {
			val inventoryList = namedTag!!.getList("Items", CompoundTag::class.java)
			for (item in inventoryList.all) {
				inventory!!.setItem(item.getByte("Slot"), NBTIO.getItemHelper(item))
			}
		}
		dataProperties
				.putByte(Entity.Companion.DATA_CONTAINER_TYPE, 11)
				.putInt(Entity.Companion.DATA_CONTAINER_BASE_SIZE, inventory!!.size)
				.putInt(Entity.Companion.DATA_CONTAINER_EXTRA_SLOTS_PER_STRENGTH, 0)
	}

	override fun saveNBT() {
		super.saveNBT()
		namedTag!!.putList(ListTag<CompoundTag>("Items"))
		if (inventory != null) {
			for (slot in 0..4) {
				val item = inventory!!.getItem(slot)
				if (item != null && item.id != Item.AIR) {
					namedTag!!.getList("Items", CompoundTag::class.java)
							.add(NBTIO.putItemHelper(item, slot))
				}
			}
		}
	}

	companion object {
		const val networkId = 96
	}

	init {
		setDisplayBlock(get(Block.HOPPER_BLOCK), false)
	}
}