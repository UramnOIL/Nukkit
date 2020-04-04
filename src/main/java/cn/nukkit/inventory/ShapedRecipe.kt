package cn.nukkit.inventory

import cn.nukkit.item.Item
import cn.nukkit.utils.Utils
import io.netty.util.collection.CharObjectHashMap
import java.util.*
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.indices
import kotlin.collections.iterator
import kotlin.collections.set

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ShapedRecipe(override var recipeId: String?, override val priority: Int, primaryResult: Item, shape: Array<String>, ingredients: Map<Char, Item>, extraResults: List<Item>?) : CraftingRecipe {
	override val result: Item
	override val extraResults: MutableList<Item> = ArrayList()
	private var least: Long = 0
	private var most: Long = 0
	val shape: Array<String>
	private val ingredients = CharObjectHashMap<Item>()

	constructor(primaryResult: Item, shape: Array<String>, ingredients: Map<Char, Item>, extraResults: List<Item>?) : this(null, 1, primaryResult, shape, ingredients, extraResults) {}

	val width: Int
		get() = shape[0].length

	val height: Int
		get() = shape.size

	override var id: UUID
		get() = UUID(least, most)
		set(uuid) {
			least = uuid.leastSignificantBits
			most = uuid.mostSignificantBits
			if (recipeId == null) {
				recipeId = id.toString()
			}
		}

	fun setIngredient(key: String, item: Item): ShapedRecipe {
		return this.setIngredient(key[0], item)
	}

	fun setIngredient(key: Char, item: Item): ShapedRecipe {
		if (java.lang.String.join("", *shape).indexOf(key) < 0) {
			throw RuntimeException("Symbol does not appear in the shape: $key")
		}
		ingredients[key] = item
		return this
	}

	val ingredientList: List<Item>
		get() {
			val items: MutableList<Item> = ArrayList()
			var y = 0
			val y2 = height
			while (y < y2) {
				var x = 0
				val x2 = width
				while (x < x2) {
					items.add(getIngredient(x, y))
					++x
				}
				++y
			}
			return items
		}

	val ingredientMap: Map<Int, Map<Int, Item>>
		get() {
			val ingredients: MutableMap<Int, Map<Int, Item>> = LinkedHashMap()
			var y = 0
			val y2 = height
			while (y < y2) {
				val m: MutableMap<Int, Item> = LinkedHashMap()
				var x = 0
				val x2 = width
				while (x < x2) {
					m[x] = getIngredient(x, y)
					++x
				}
				ingredients[y] = m
				++y
			}
			return ingredients
		}

	fun getIngredient(x: Int, y: Int): Item {
		val item = ingredients[shape[y][x]]
		return item?.clone() ?: Item.get(Item.AIR)
	}

	override fun registerToCraftingManager(manager: CraftingManager) {
		manager.registerShapedRecipe(this)
	}

	override val type: RecipeType?
		get() = RecipeType.SHAPED

	override val allResults: List<Item>?
		get() {
			val list: MutableList<Item> = ArrayList(extraResults)
			list.add(result)
			return list
		}

	override fun matchItems(input: Array<Array<Item?>>, output: Array<Array<Item?>>): Boolean {
		if (!matchInputMap(Utils.clone2dArray(input))) {
			val reverse = Utils.clone2dArray(input)
			for (y in reverse.indices) {
				reverse[y] = Utils.reverseArray(reverse[y], false)
			}
			if (!matchInputMap(reverse)) {
				return false
			}
		}

		//and then, finally, check that the output items are good:
		val haveItems: MutableList<Item> = ArrayList()
		for (items in output) {
			haveItems.addAll(Arrays.asList(*items))
		}
		val needItems = extraResults
		for (haveItem in ArrayList(haveItems)) {
			if (haveItem.isNull) {
				haveItems.remove(haveItem)
				continue
			}
			for (needItem in ArrayList(needItems)) {
				if (needItem.equals(haveItem, needItem.hasMeta(), needItem.hasCompoundTag()) && needItem.getCount() == haveItem.getCount()) {
					haveItems.remove(haveItem)
					needItems.remove(needItem)
					break
				}
			}
		}
		return haveItems.isEmpty() && needItems.isEmpty()
	}

	private fun matchInputMap(input: Array<Array<Item?>>): Boolean {
		val map = ingredientMap

		//match the given items to the requested items
		var y = 0
		val y2 = height
		while (y < y2) {
			var x = 0
			val x2 = width
			while (x < x2) {
				val given = input[y][x]
				val required = map[y]!![x]
				if (given == null || !required!!.equals(given, required.hasMeta(), required.hasCompoundTag()) || required.getCount() != given.getCount()) {
					return false
				}
				input[y][x] = null
				++x
			}
			++y
		}

		//check if there are any items left in the grid outside of the recipe
		for (items in input) {
			for (item in items) {
				if (item != null && !item.isNull) {
					return false
				}
			}
		}
		return true
	}

	override fun toString(): String {
		val joiner = StringJoiner(", ")
		ingredients.forEach { (character: Char?, item: Item) -> joiner.add(item.name + ":" + item.damage) }
		return joiner.toString()
	}

	override fun requiresCraftingTable(): Boolean {
		return height > 2 || width > 2
	}

	class Entry(val x: Int, val y: Int)

	/**
	 * Constructs a ShapedRecipe instance.
	 *
	 * @param primaryResult    Primary result of the recipe
	 * @param shape<br></br>        Array of 1, 2, or 3 strings representing the rows of the recipe.
	 * This accepts an array of 1, 2 or 3 strings. Each string should be of the same length and must be at most 3
	 * characters long. Each character represents a unique type of ingredient. Spaces are interpreted as air.
	 * @param ingredients<br></br>  Char =&gt; Item map of items to be set into the shape.
	 * This accepts an array of Items, indexed by character. Every unique character (except space) in the shape
	 * array MUST have a corresponding item in this list. Space character is automatically treated as air.
	 * @param extraResults<br></br> List of additional result items to leave in the crafting grid afterwards. Used for things like cake recipe
	 * empty buckets.
	 *
	 *
	 * Note: Recipes **do not** need to be square. Do NOT add padding for empty rows/columns.
	 */
	init {
		val rowCount = shape.size
		if (rowCount > 3 || rowCount <= 0) {
			throw RuntimeException("Shaped recipes may only have 1, 2 or 3 rows, not $rowCount")
		}
		val columnCount = shape[0].length
		if (columnCount > 3 || rowCount <= 0) {
			throw RuntimeException("Shaped recipes may only have 1, 2 or 3 columns, not $columnCount")
		}


		//for($shape as $y => $row) {
		for (row in shape) {
			if (row.length != columnCount) {
				throw RuntimeException("Shaped recipe rows must all have the same length (expected " + columnCount + ", got " + row.length + ")")
			}
			for (x in 0 until columnCount) {
				val c = row[x]
				if (c != ' ' && !ingredients.containsKey(c)) {
					throw RuntimeException("No item specified for symbol '$c'")
				}
			}
		}
		result = primaryResult.clone()
		this.extraResults.addAll(extraResults!!)
		this.shape = shape
		for ((key, value) in ingredients) {
			this.setIngredient(key, value)
		}
	}
}