package cn.nukkit.blockentity

import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.block.BlockID
import cn.nukkit.event.inventory.FurnaceBurnEvent
import cn.nukkit.event.inventory.FurnaceSmeltEvent
import cn.nukkit.inventory.FurnaceInventory
import cn.nukkit.inventory.InventoryHolder
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.network.protocol.ContainerSetDataPacket
import java.util.*

/**
 * @author MagicDroidX
 */
class BlockEntityFurnace(chunk: FullChunk?, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt), InventoryHolder, BlockEntityContainer, BlockEntityNameable {
	protected var inventory: FurnaceInventory? = null
	var burnTime = 0
	var burnDuration = 0
	var cookTime = 0
	var maxTime = 0
	override fun initBlockEntity() {
		inventory = FurnaceInventory(this)
		if (!namedTag.contains("Items") || namedTag["Items"] !is ListTag<*>) {
			namedTag.putList(ListTag<CompoundTag>("Items"))
		}
		for (i in 0 until size) {
			inventory!!.setItem(i, getItem(i))
		}
		burnTime = if (!namedTag.contains("BurnTime") || namedTag.getShort("BurnTime") < 0) {
			0
		} else {
			namedTag.getShort("BurnTime")
		}
		cookTime = if (!namedTag.contains("CookTime") || namedTag.getShort("CookTime") < 0 || namedTag.getShort("BurnTime") == 0 && namedTag.getShort("CookTime") > 0) {
			0
		} else {
			namedTag.getShort("CookTime")
		}
		if (!namedTag.contains("MaxTime")) {
			maxTime = burnTime
			burnDuration = 0
		} else {
			maxTime = namedTag.getShort("MaxTime")
		}
		if (namedTag.contains("BurnTicks")) {
			burnDuration = namedTag.getShort("BurnTicks")
			namedTag.remove("BurnTicks")
		}
		if (burnTime > 0) {
			scheduleUpdate()
		}
		super.initBlockEntity()
	}

	override fun getName(): String? {
		return if (hasName()) namedTag.getString("CustomName") else "Furnace"
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

	override fun saveNBT() {
		namedTag.putList(ListTag<CompoundTag>("Items"))
		for (index in 0 until size) {
			setItem(index, inventory!!.getItem(index))
		}
		namedTag.putShort("CookTime", cookTime)
		namedTag.putShort("BurnTime", burnTime)
		namedTag.putShort("BurnDuration", burnDuration)
		namedTag.putShort("MaxTime", maxTime)
	}

	override val isBlockEntityValid: Boolean
		get() {
			val blockID = block.id
			return blockID == Block.FURNACE || blockID == Block.BURNING_FURNACE
		}

	override val size: Int
		get() = 3

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

	override fun getInventory(): FurnaceInventory {
		return inventory!!
	}

	protected fun checkFuel(fuel: Item) {
		var fuel = fuel
		val ev = FurnaceBurnEvent(this, fuel, if (fuel.fuelTime == null) 0 else fuel.fuelTime)
		server.pluginManager.callEvent(ev)
		if (ev.isCancelled) {
			return
		}
		maxTime = ev.burnTime.toInt()
		burnTime = ev.burnTime.toInt()
		burnDuration = 0
		if (this.block.id == Item.FURNACE) {
			getLevel().setBlock(this, get(BlockID.BURNING_FURNACE, this.block.damage), true)
		}
		if (burnTime > 0 && ev.isBurning) {
			fuel.setCount(fuel.getCount() - 1)
			if (fuel.getCount() == 0) {
				if (fuel.id == Item.BUCKET && fuel.damage == 10) {
					fuel.setDamage(0)
					fuel.setCount(1)
				} else {
					fuel = ItemBlock(get(BlockID.AIR), 0, 0)
				}
			}
			inventory!!.fuel = fuel
		}
	}

	override fun onUpdate(): Boolean {
		if (closed) {
			return false
		}
		timing.startTiming()
		var ret = false
		val fuel = inventory!!.fuel
		var raw = inventory!!.smelting
		var product = inventory!!.result
		val smelt = server.craftingManager.matchFurnaceRecipe(raw)
		val canSmelt = smelt != null && raw.getCount() > 0 && (smelt.result.equals(product, true) && product.getCount() < product.maxStackSize || product.id == Item.AIR)
		if (burnTime <= 0 && canSmelt && fuel.fuelTime != null && fuel.getCount() > 0) {
			checkFuel(fuel)
		}
		if (burnTime > 0) {
			burnTime--
			burnDuration = Math.ceil(burnTime.toFloat() / maxTime * 200.toDouble()).toInt()
			if (smelt != null && canSmelt) {
				cookTime++
				if (cookTime >= 200) {
					product = Item.get(smelt.result.id, smelt.result.damage, product.getCount() + 1)
					val ev = FurnaceSmeltEvent(this, raw, product)
					server.pluginManager.callEvent(ev)
					if (!ev.isCancelled) {
						inventory!!.result = ev.result
						raw.setCount(raw.count - 1)
						if (raw.getCount() == 0) {
							raw = ItemBlock(get(BlockID.AIR), 0, 0)
						}
						inventory!!.smelting = raw
					}
					cookTime -= 200
				}
			} else if (burnTime <= 0) {
				burnTime = 0
				cookTime = 0
				burnDuration = 0
			} else {
				cookTime = 0
			}
			ret = true
		} else {
			if (this.block.id == Item.BURNING_FURNACE) {
				getLevel().setBlock(this, get(BlockID.FURNACE, this.block.damage), true)
			}
			burnTime = 0
			cookTime = 0
			burnDuration = 0
		}
		for (player in getInventory().viewers) {
			val windowId = player.getWindowId(getInventory())
			if (windowId > 0) {
				var pk = ContainerSetDataPacket()
				pk.windowId = windowId
				pk.property = ContainerSetDataPacket.PROPERTY_FURNACE_TICK_COUNT
				pk.value = cookTime
				player.dataPacket(pk)
				pk = ContainerSetDataPacket()
				pk.windowId = windowId
				pk.property = ContainerSetDataPacket.PROPERTY_FURNACE_LIT_TIME
				pk.value = burnDuration
				player.dataPacket(pk)
			}
		}
		lastUpdate = System.currentTimeMillis()
		timing.stopTiming()
		return ret
	}

	override val spawnCompound: CompoundTag?
		get() {
			val c = CompoundTag()
					.putString("id", BlockEntity.Companion.FURNACE)
					.putInt("x", x.toInt())
					.putInt("y", y.toInt())
					.putInt("z", z.toInt())
					.putShort("BurnDuration", burnDuration)
					.putShort("BurnTime", burnTime)
					.putShort("CookTime", cookTime)
			if (hasName()) {
				c.put("CustomName", namedTag["CustomName"])
			}
			return c
		}

}