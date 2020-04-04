package cn.nukkit.blockentity

import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.block.BlockID
import cn.nukkit.entity.item.EntityItem
import cn.nukkit.event.inventory.InventoryMoveItemEvent
import cn.nukkit.inventory.Fuel
import cn.nukkit.inventory.HopperInventory
import cn.nukkit.inventory.InventoryHolder
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.format.FullChunk
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.BlockFace
import cn.nukkit.math.SimpleAxisAlignedBB
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import java.util.*

/**
 * Created by CreeperFace on 8.5.2017.
 */
class BlockEntityHopper(chunk: FullChunk?, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt), InventoryHolder, BlockEntityContainer, BlockEntityNameable {
	protected var inventory: HopperInventory? = null
	var transferCooldown = 8
	private var pickupArea: AxisAlignedBB? = null
	override fun initBlockEntity() {
		if (namedTag.contains("TransferCooldown")) {
			transferCooldown = namedTag.getInt("TransferCooldown")
		}
		inventory = HopperInventory(this)
		if (!namedTag.contains("Items") || namedTag["Items"] !is ListTag<*>) {
			namedTag.putList(ListTag<CompoundTag>("Items"))
		}
		for (i in 0 until size) {
			inventory!!.setItem(i, getItem(i))
		}
		pickupArea = SimpleAxisAlignedBB(x, y, z, x + 1, y + 2, z + 1)
		scheduleUpdate()
		super.initBlockEntity()
	}

	override val isBlockEntityValid: Boolean
		get() = level.getBlockIdAt(this.floorX, this.floorY, this.floorZ) == Block.HOPPER_BLOCK

	override fun getName(): String? {
		return if (hasName()) namedTag.getString("CustomName") else "Hopper"
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

	val isOnTransferCooldown: Boolean
		get() = transferCooldown > 0

	fun setTransferCooldown(transferCooldown: Int) {
		this.transferCooldown = transferCooldown
	}

	override val size: Int
		get() = 5

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
				namedTag.getList("Items").all.removeAt(i)
			}
		} else if (i < 0) {
			namedTag.getList("Items", CompoundTag::class.java).add(d)
		} else {
			namedTag.getList("Items", CompoundTag::class.java).add(i, d)
		}
	}

	override fun saveNBT() {
		namedTag.putList(ListTag<CompoundTag>("Items"))
		for (index in 0 until size) {
			setItem(index, inventory!!.getItem(index))
		}
		namedTag.putInt("TransferCooldown", transferCooldown)
	}

	override fun getInventory(): HopperInventory {
		return inventory!!
	}

	override fun onUpdate(): Boolean {
		if (closed) {
			return false
		}
		transferCooldown--
		if (!isOnTransferCooldown) {
			val blockEntity = level.getBlockEntity(this.up())
			var changed = pushItems()
			if (!changed) {
				changed = if (blockEntity !is BlockEntityContainer) {
					pickupItems()
				} else {
					pullItems()
				}
			}
			if (changed) {
				setTransferCooldown(8)
				setDirty()
			}
		}
		return true
	}

	fun pullItems(): Boolean {
		if (inventory!!.isFull) {
			return false
		}
		val blockEntity = level.getBlockEntity(this.up())
		//Fix for furnace outputs
		if (blockEntity is BlockEntityFurnace) {
			val inv = blockEntity.getInventory()
			val item = inv!!.result
			if (!item.isNull) {
				val itemToAdd = item.clone()
				itemToAdd.count = 1
				if (!inventory!!.canAddItem(itemToAdd)) {
					return false
				}
				val ev = InventoryMoveItemEvent(inv, inventory, this, itemToAdd, InventoryMoveItemEvent.Action.SLOT_CHANGE)
				server.pluginManager.callEvent(ev)
				if (ev.isCancelled) {
					return false
				}
				val items = inventory!!.addItem(itemToAdd)
				if (items.size <= 0) {
					item.count--
					inv.result = item
					return true
				}
			}
		} else if (blockEntity is InventoryHolder) {
			val inv = (blockEntity as InventoryHolder).inventory
			for (i in 0 until inv.size) {
				val item = inv.getItem(i)
				if (!item.isNull) {
					val itemToAdd = item.clone()
					itemToAdd.count = 1
					if (!inventory!!.canAddItem(itemToAdd)) {
						continue
					}
					val ev = InventoryMoveItemEvent(inv, inventory, this, itemToAdd, InventoryMoveItemEvent.Action.SLOT_CHANGE)
					server.pluginManager.callEvent(ev)
					if (ev.isCancelled) {
						continue
					}
					val items = inventory!!.addItem(itemToAdd)
					if (items.size >= 1) {
						continue
					}
					item.count--
					inv.setItem(i, item)
					return true
				}
			}
		}
		return false
	}

	fun pickupItems(): Boolean {
		if (inventory!!.isFull) {
			return false
		}
		var pickedUpItem = false
		for (entity in level.getCollidingEntities(pickupArea)) {
			if (entity.isClosed || entity !is EntityItem) {
				continue
			}
			val item = entity.item
			if (item.isNull) {
				continue
			}
			val originalCount = item.getCount()
			if (!inventory!!.canAddItem(item)) {
				continue
			}
			val ev = InventoryMoveItemEvent(null, inventory, this, item, InventoryMoveItemEvent.Action.PICKUP)
			server.pluginManager.callEvent(ev)
			if (ev.isCancelled) {
				continue
			}
			val items = inventory!!.addItem(item)
			if (items.size == 0) {
				entity.close()
				pickedUpItem = true
				continue
			}
			if (items[0].getCount() != originalCount) {
				pickedUpItem = true
				item.setCount(items[0].getCount())
			}
		}

		//TODO: check for minecart
		return pickedUpItem
	}

	override fun close() {
		if (!closed) {
			for (player in HashSet(getInventory().viewers)) {
				player.removeWindow(getInventory())
			}
			super.close()
		}
	}

	override fun onBreak() {
		for (content in inventory!!.contents.values) {
			level.dropItem(this, content)
		}
	}

	fun pushItems(): Boolean {
		if (inventory!!.isEmpty) {
			return false
		}
		val be = level.getBlockEntity(this.getSide(BlockFace.fromIndex(level.getBlockDataAt(this.floorX, this.floorY, this.floorZ))))
		if (be is BlockEntityHopper && this.block.damage == 0 || be !is InventoryHolder) return false
		var event: InventoryMoveItemEvent

		//Fix for furnace inputs
		if (be is BlockEntityFurnace) {
			val inventory = be.getInventory()
			if (inventory!!.isFull) {
				return false
			}
			var pushedItem = false
			for (i in 0 until this.inventory!!.size) {
				val item = this.inventory!!.getItem(i)
				if (!item.isNull) {
					val itemToAdd = item.clone()
					itemToAdd.setCount(1)

					//Check direction of hopper
					if (this.block.damage == 0) {
						val smelting = inventory.smelting
						if (smelting.isNull) {
							event = InventoryMoveItemEvent(this.inventory, inventory, this, itemToAdd, InventoryMoveItemEvent.Action.SLOT_CHANGE)
							server.pluginManager.callEvent(event)
							if (!event.isCancelled) {
								inventory.smelting = itemToAdd
								item.count--
								pushedItem = true
							}
						} else if (inventory.smelting.id == itemToAdd.id && inventory.smelting.damage == itemToAdd.damage && smelting.count < smelting.maxStackSize) {
							event = InventoryMoveItemEvent(this.inventory, inventory, this, itemToAdd, InventoryMoveItemEvent.Action.SLOT_CHANGE)
							server.pluginManager.callEvent(event)
							if (!event.isCancelled) {
								smelting.count++
								inventory.smelting = smelting
								item.count--
								pushedItem = true
							}
						}
					} else if (Fuel.duration.containsKey(itemToAdd.id)) {
						val fuel = inventory.fuel
						if (fuel.isNull) {
							event = InventoryMoveItemEvent(this.inventory, inventory, this, itemToAdd, InventoryMoveItemEvent.Action.SLOT_CHANGE)
							server.pluginManager.callEvent(event)
							if (!event.isCancelled) {
								inventory.fuel = itemToAdd
								item.count--
								pushedItem = true
							}
						} else if (fuel.id == itemToAdd.id && fuel.damage == itemToAdd.damage && fuel.count < fuel.maxStackSize) {
							event = InventoryMoveItemEvent(this.inventory, inventory, this, itemToAdd, InventoryMoveItemEvent.Action.SLOT_CHANGE)
							server.pluginManager.callEvent(event)
							if (!event.isCancelled) {
								fuel.count++
								inventory.fuel = fuel
								item.count--
								pushedItem = true
							}
						}
					}
					if (pushedItem) {
						this.inventory!!.setItem(i, item)
					}
				}
			}
			return pushedItem
		} else {
			val inventory = (be as InventoryHolder).inventory
			if (inventory.isFull) {
				return false
			}
			for (i in 0 until this.inventory!!.size) {
				val item = this.inventory!!.getItem(i)
				if (!item.isNull) {
					val itemToAdd = item.clone()
					itemToAdd.setCount(1)
					if (!inventory.canAddItem(itemToAdd)) {
						continue
					}
					val ev = InventoryMoveItemEvent(this.inventory, inventory, this, itemToAdd, InventoryMoveItemEvent.Action.SLOT_CHANGE)
					server.pluginManager.callEvent(ev)
					if (ev.isCancelled) {
						continue
					}
					val items = inventory.addItem(itemToAdd)
					if (items.size > 0) {
						continue
					}
					item.count--
					this.inventory!!.setItem(i, item)
					return true
				}
			}
		}

		//TODO: check for minecart
		return false
	}

	override val spawnCompound: CompoundTag?
		get() {
			val c = CompoundTag()
					.putString("id", BlockEntity.Companion.HOPPER)
					.putInt("x", x.toInt())
					.putInt("y", y.toInt())
					.putInt("z", z.toInt())
			if (hasName()) {
				c.put("CustomName", namedTag["CustomName"])
			}
			return c
		}
}