package cn.nukkit.inventory

import cn.nukkit.item.Item
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ShapelessRecipe(override var recipeId: String?, override val priority: Int, result: Item, ingredients: Collection<Item>) : CraftingRecipe {
	private val output: Item
	private var least: Long = 0
	private var most: Long = 0
	private val ingredients: MutableList<Item>

	constructor(result: Item, ingredients: Collection<Item>) : this(null, 10, result, ingredients) {}

	override val result: Item
		get() = output.clone()

	override var id: UUID
		get() = UUID(least, most)
		set(uuid) {
			least = uuid.leastSignificantBits
			most = uuid.mostSignificantBits
			if (recipeId == null) {
				recipeId = id.toString()
			}
		}

	val ingredientList: List<Item>
		get() {
			val ingredients: MutableList<Item> = ArrayList()
			for (ingredient in this.ingredients) {
				ingredients.add(ingredient.clone())
			}
			return ingredients
		}

	val ingredientCount: Int
		get() = ingredients.size

	override fun registerToCraftingManager(manager: CraftingManager) {
		manager.registerShapelessRecipe(this)
	}

	override val type: RecipeType?
		get() = RecipeType.SHAPELESS

	override fun requiresCraftingTable(): Boolean {
		return ingredients.size > 4
	}

	override val extraResults: MutableList<Item>
		get() = ArrayList()

	override val allResults: List<Item>?
		get() = null

	override fun matchItems(input: Array<Array<Item?>>, output: Array<Array<Item?>>): Boolean {
		val haveInputs: MutableList<Item> = ArrayList()
		for (items in input) {
			haveInputs.addAll(Arrays.asList(*items))
		}
		haveInputs.sort(CraftingManager.Companion.recipeComparator)
		val needInputs = ingredientList
		if (!matchItemList(haveInputs, needInputs)) {
			return false
		}
		val haveOutputs: MutableList<Item> = ArrayList()
		for (items in output) {
			haveOutputs.addAll(Arrays.asList(*items))
		}
		haveOutputs.sort(CraftingManager.Companion.recipeComparator)
		val needOutputs: List<Item> = extraResults
		return matchItemList(haveOutputs, needOutputs)
	}

	private fun matchItemList(haveItems: MutableList<Item>, needItems: List<Item>): Boolean {
		// Remove any air blocks that may have gotten through.
		haveItems.removeIf { obj: Item -> obj.isNull }
		if (haveItems.size != needItems.size) {
			return false
		}
		val size = needItems.size
		var completed = 0
		for (i in 0 until size) {
			val haveItem = haveItems[i]
			val needItem = needItems[i]
			if (needItem.equals(haveItem, needItem.hasMeta(), needItem.hasCompoundTag())) {
				completed++
			}
		}
		return completed == size
	}

	init {
		output = result.clone()
		require(ingredients.size <= 9) { "Shapeless recipes cannot have more than 9 ingredients" }
		this.ingredients = ArrayList()
		for (item in ingredients) {
			require(item.getCount() >= 1) { "Recipe '" + recipeId + "' Ingredient amount was not 1 (value: " + item.getCount() + ")" }
			this.ingredients.add(item.clone())
		}
	}
}