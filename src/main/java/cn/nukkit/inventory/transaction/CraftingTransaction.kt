package cn.nukkit.inventory.transaction

import cn.nukkit.Player
import cn.nukkit.event.inventory.CraftItemEvent
import cn.nukkit.inventory.BigCraftingGrid
import cn.nukkit.inventory.CraftingRecipe
import cn.nukkit.inventory.transaction.action.InventoryAction
import cn.nukkit.item.Item
import cn.nukkit.network.protocol.ContainerClosePacket
import cn.nukkit.network.protocol.types.ContainerIds
import cn.nukkit.scheduler.Task
import java.util.*

/**
 * @author CreeperFace
 */
class CraftingTransaction(source: Player, actions: List<InventoryAction?>) : InventoryTransaction(source, actions, false) {
	protected var gridSize: Int
	var inputMap: Array<Array<Item?>>
		protected set
	protected var secondaryOutputs: Array<Array<Item?>>
	protected var primaryOutput: Item? = null
	var recipe: CraftingRecipe? = null
		protected set

	fun setInput(index: Int, item: Item?) {
		val y = index / gridSize
		val x = index % gridSize
		if (inputMap[y][x]!!.isNull) {
			inputMap[y][x] = item!!.clone()
		} else if (inputMap[y][x] != item) {
			throw RuntimeException("Input " + index + " has already been set and does not match the current item (expected " + inputMap[y][x] + ", got " + item + ")")
		}
	}

	fun setExtraOutput(index: Int, item: Item?) {
		val y = index / gridSize
		val x = index % gridSize
		if (secondaryOutputs[y][x]!!.isNull) {
			secondaryOutputs[y][x] = item!!.clone()
		} else if (secondaryOutputs[y][x] != item) {
			throw RuntimeException("Output " + index + " has already been set and does not match the current item (expected " + secondaryOutputs[y][x] + ", got " + item + ")")
		}
	}

	fun getPrimaryOutput(): Item? {
		return primaryOutput
	}

	fun setPrimaryOutput(item: Item?) {
		if (primaryOutput == null) {
			primaryOutput = item!!.clone()
		} else if (primaryOutput != item) {
			throw RuntimeException("Primary result item has already been set and does not match the current item (expected $primaryOutput, got $item)")
		}
	}

	private fun reindexInputs(): Array<Array<Item?>> {
		var xMin = gridSize - 1
		var yMin = gridSize - 1
		var xMax = 0
		var yMax = 0
		for (y in inputMap.indices) {
			val row = inputMap[y]
			for (x in row.indices) {
				val item = row[x]
				if (!item!!.isNull) {
					xMin = Math.min(x, xMin)
					yMin = Math.min(y, yMin)
					xMax = Math.max(x, xMax)
					yMax = Math.max(y, yMax)
				}
			}
		}
		val height = yMax - yMin + 1
		val width = xMax - xMin + 1
		if (height < 1 || width < 1) {
			return arrayOfNulls(0)
		}
		val reindexed = Array(height) { arrayOfNulls<Item>(width) }
		var y = yMin
		var i = 0
		while (y <= yMax) {
			System.arraycopy(inputMap[y], xMin, reindexed[i], 0, width)
			y++
			i++
		}
		return reindexed
	}

	override fun canExecute(): Boolean {
		val inputs = reindexInputs()
		recipe = source!!.getServer().craftingManager.matchRecipe(inputs, primaryOutput, secondaryOutputs)
		return recipe != null && super.canExecute()
	}

	override fun callExecuteEvent(): Boolean {
		var ev: CraftItemEvent
		source!!.getServer().pluginManager.callEvent(CraftItemEvent(this).also { ev = it })
		return !ev.isCancelled
	}

	override fun sendInventories() {
		super.sendInventories()

		/*
         * TODO: HACK!
		 * we can't resend the contents of the crafting window, so we force the client to close it instead.
		 * So people don't whine about messy desync issues when someone cancels CraftItemEvent, or when a crafting
		 * transaction goes wrong.
		 */
		val pk = ContainerClosePacket()
		pk.windowId = ContainerIds.NONE
		source!!.getServer().scheduler.scheduleDelayedTask(object : Task() {
			override fun onRun(currentTick: Int) {
				source!!.dataPacket(pk)
			}
		}, 20)
		source!!.resetCraftingGridType()
	}

	override fun execute(): Boolean {
		if (super.execute()) {
			when (primaryOutput!!.id) {
				Item.CRAFTING_TABLE -> source!!.awardAchievement("buildWorkBench")
				Item.WOODEN_PICKAXE -> source!!.awardAchievement("buildPickaxe")
				Item.FURNACE -> source!!.awardAchievement("buildFurnace")
				Item.WOODEN_HOE -> source!!.awardAchievement("buildHoe")
				Item.BREAD -> source!!.awardAchievement("makeBread")
				Item.CAKE -> source!!.awardAchievement("bakeCake")
				Item.STONE_PICKAXE, Item.GOLDEN_PICKAXE, Item.IRON_PICKAXE, Item.DIAMOND_PICKAXE -> source!!.awardAchievement("buildBetterPickaxe")
				Item.WOODEN_SWORD -> source!!.awardAchievement("buildSword")
				Item.DIAMOND -> source!!.awardAchievement("diamond")
			}
			return true
		}
		return false
	}

	init {
		gridSize = if (source.getCraftingGrid() is BigCraftingGrid) 3 else 2
		val air = Item.get(Item.AIR, 0, 1)
		inputMap = Array(gridSize) { arrayOfNulls(gridSize) }
		for (a in inputMap) {
			Arrays.fill(a, air)
		}
		secondaryOutputs = Array(gridSize) { arrayOfNulls(gridSize) }
		for (a in secondaryOutputs) {
			Arrays.fill(a, air)
		}
		init(source, actions)
	}
}