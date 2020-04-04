package cn.nukkit.blockentity

import cn.nukkit.Server
import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.block.BlockBrewingStand
import cn.nukkit.block.BlockID
import cn.nukkit.event.inventory.BrewEvent
import cn.nukkit.event.inventory.StartBrewEvent
import cn.nukkit.inventory.BrewingInventory
import cn.nukkit.inventory.InventoryHolder
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.network.protocol.ContainerSetDataPacket
import cn.nukkit.network.protocol.LevelSoundEventPacket
import java.util.*

class BlockEntityBrewingStand(chunk: FullChunk?, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt), InventoryHolder, BlockEntityContainer, BlockEntityNameable {
	protected var inventory: BrewingInventory? = null
	var brewTime = MAX_BREW_TIME
	var fuelTotal = 0
	var fuel = 0
	override fun initBlockEntity() {
		inventory = BrewingInventory(this)
		if (!namedTag.contains("Items") || namedTag["Items"] !is ListTag<*>) {
			namedTag.putList(ListTag<CompoundTag>("Items"))
		}
		for (i in 0 until size) {
			inventory!!.setItem(i, getItem(i))
		}
		if (!namedTag.contains("CookTime") || namedTag.getShort("CookTime") > MAX_BREW_TIME) {
			brewTime = MAX_BREW_TIME
		} else {
			brewTime = namedTag.getShort("CookTime")
		}
		fuel = namedTag.getShort("FuelAmount")
		fuelTotal = namedTag.getShort("FuelTotal")
		if (brewTime < MAX_BREW_TIME) {
			scheduleUpdate()
		}
		super.initBlockEntity()
	}

	override fun getName(): String? {
		return if (hasName()) namedTag.getString("CustomName") else "Brewing Stand"
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
		namedTag.putShort("CookTime", brewTime)
		namedTag.putShort("FuelAmount", fuel)
		namedTag.putShort("FuelTotal", fuelTotal)
	}

	override val isBlockEntityValid: Boolean
		get() = block.id == Block.BREWING_STAND_BLOCK

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

	override fun getInventory(): BrewingInventory {
		return inventory!!
	}

	protected fun checkIngredient(ingredient: Item): Boolean {
		return ingredients.contains(ingredient.id)
	}

	override fun onUpdate(): Boolean {
		if (closed) {
			return false
		}
		var ret = false
		val ingredient = inventory!!.ingredient
		var canBrew = false
		val fuel = getInventory().fuel
		if (this.fuel <= 0 && fuel.id == Item.BLAZE_POWDER && fuel.getCount() > 0) {
			fuel.count--
			this.fuel = 20
			fuelTotal = 20
			inventory!!.fuel = fuel
			sendFuel()
		}
		if (this.fuel > 0) {
			for (i in 1..3) {
				if (inventory!!.getItem(i).id == Item.POTION) {
					canBrew = true
				}
			}
			if (brewTime <= MAX_BREW_TIME && canBrew && ingredient.getCount() > 0) {
				if (!checkIngredient(ingredient)) {
					canBrew = false
				}
			} else {
				canBrew = false
			}
		}
		if (canBrew) {
			if (brewTime == MAX_BREW_TIME) {
				sendBrewTime()
				val e = StartBrewEvent(this)
				server.pluginManager.callEvent(e)
				if (e.isCancelled) {
					return false
				}
			}
			brewTime--
			if (brewTime <= 0) { //20 seconds
				val e = BrewEvent(this)
				server.pluginManager.callEvent(e)
				if (!e.isCancelled) {
					for (i in 1..3) {
						val potion = inventory!!.getItem(i)
						val containerRecipe = Server.instance!!.craftingManager.matchContainerRecipe(ingredient, potion)
						if (containerRecipe != null) {
							val result = containerRecipe.result
							result.damage = potion.damage
							inventory!!.setItem(i, result)
						} else {
							val recipe = Server.instance!!.craftingManager.matchBrewingRecipe(ingredient, potion)
							if (recipe != null) {
								inventory!!.setItem(i, recipe.result)
							}
						}
					}
					getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_POTION_BREWED)
					ingredient.count--
					inventory!!.ingredient = ingredient
					this.fuel--
					sendFuel()
				}
				brewTime = MAX_BREW_TIME
			}
			ret = true
		} else {
			brewTime = MAX_BREW_TIME
		}

		//this.sendBrewTime();
		lastUpdate = System.currentTimeMillis()
		return ret
	}

	protected fun sendFuel() {
		val pk = ContainerSetDataPacket()
		for (p in inventory!!.viewers) {
			val windowId = p.getWindowId(inventory)
			if (windowId > 0) {
				pk.windowId = windowId
				pk.property = ContainerSetDataPacket.PROPERTY_BREWING_STAND_FUEL_AMOUNT
				pk.value = fuel
				p.dataPacket(pk)
				pk.property = ContainerSetDataPacket.PROPERTY_BREWING_STAND_FUEL_TOTAL
				pk.value = fuelTotal
				p.dataPacket(pk)
			}
		}
	}

	protected fun sendBrewTime() {
		val pk = ContainerSetDataPacket()
		pk.property = ContainerSetDataPacket.PROPERTY_BREWING_STAND_BREW_TIME
		pk.value = brewTime
		for (p in inventory!!.viewers) {
			val windowId = p.getWindowId(inventory)
			if (windowId > 0) {
				pk.windowId = windowId
				p.dataPacket(pk)
			}
		}
	}

	fun updateBlock() {
		val block = this.levelBlock as? BlockBrewingStand ?: return
		var meta = 0
		for (i in 1..3) {
			val potion = inventory!!.getItem(i)
			val id = potion.id
			if ((id == Item.POTION || id == Item.SPLASH_POTION || id == Item.LINGERING_POTION) && potion.getCount() > 0) {
				meta = meta or (1 shl i - 1)
			}
		}
		block.damage = meta
		level.setBlock(block, block, false, false)
	}

	override val spawnCompound: CompoundTag?
		get() {
			val nbt = CompoundTag()
					.putString("id", BlockEntity.Companion.BREWING_STAND)
					.putInt("x", x.toInt())
					.putInt("y", y.toInt())
					.putInt("z", z.toInt())
					.putShort("FuelTotal", fuelTotal)
					.putShort("FuelAmount", fuel)
			if (brewTime < MAX_BREW_TIME) {
				nbt.putShort("CookTime", brewTime)
			}
			if (hasName()) {
				nbt.put("CustomName", namedTag["CustomName"])
			}
			return nbt
		}

	companion object {
		const val MAX_BREW_TIME = 400
		val ingredients: List<Int> = object : ArrayList<Int?>() {
			init {
				addAll(Arrays.asList(Item.NETHER_WART, Item.GHAST_TEAR, Item.GLOWSTONE_DUST, Item.REDSTONE_DUST, Item.GUNPOWDER, Item.MAGMA_CREAM, Item.BLAZE_POWDER, Item.GOLDEN_CARROT, Item.SPIDER_EYE, Item.FERMENTED_SPIDER_EYE, Item.GLISTERING_MELON, Item.SUGAR, Item.RABBIT_FOOT, Item.PUFFERFISH, Item.TURTLE_SHELL, Item.PHANTOM_MEMBRANE, 437))
			}
		}
	}
}