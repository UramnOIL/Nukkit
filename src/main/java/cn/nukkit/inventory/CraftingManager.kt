package cn.nukkit.inventory

import cn.nukkit.Server
import cn.nukkit.item.Item
import cn.nukkit.item.ItemID
import cn.nukkit.network.protocol.BatchPacket
import cn.nukkit.network.protocol.CraftingDataPacket
import cn.nukkit.utils.BinaryStream
import cn.nukkit.utils.Config
import cn.nukkit.utils.MainLogger
import cn.nukkit.utils.Utils
import io.netty.util.collection.CharObjectHashMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import lombok.extern.log4j.Log4j2
import java.io.File
import java.util.*
import java.util.zip.Deflater
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableCollection
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.indices
import kotlin.collections.iterator
import kotlin.collections.set
import kotlin.collections.sort
import kotlin.collections.toTypedArray

/**
 * author: MagicDroidX
 * Nukkit Project
 */
@Log4j2
class CraftingManager {
	val recipes: MutableCollection<Recipe> = ArrayDeque()
	protected val shapedRecipes: MutableMap<Int, MutableMap<UUID?, ShapedRecipe>> = Int2ObjectOpenHashMap<Map<UUID, ShapedRecipe>>()
	val furnaceRecipes: MutableMap<Int, FurnaceRecipe> = Int2ObjectOpenHashMap()
	val brewingRecipes: MutableMap<Int, BrewingRecipe> = Int2ObjectOpenHashMap()
	val containerRecipes: MutableMap<Int, ContainerRecipe> = Int2ObjectOpenHashMap()
	protected val shapelessRecipes: MutableMap<Int, MutableMap<UUID?, ShapelessRecipe>> = Int2ObjectOpenHashMap<Map<UUID, ShapelessRecipe>>()
	private fun loadRecipes(config: Config) {
		val recipes = config.getMapList("recipes")
		MainLogger.getLogger().info("Loading recipes...")
		for (recipe in recipes) {
			try {
				when (Utils.toInt(recipe["type"])) {
					0 -> {
						val craftingBlock = recipe["block"] as String?
						if ("crafting_table" != craftingBlock) {
							// Ignore other recipes than crafting table ones
							continue
						}
						// TODO: handle multiple result items
						val outputs = recipe["output"] as List<Map<*, *>>?
						if (outputs!!.size > 1) {
							continue
						}
						val first: Map<String, Any> = outputs[0]
						val sorted: MutableList<Item> = ArrayList()
						for (ingredient in (recipe["input"] as List<Map<*, *>>?)!!) {
							sorted.add(Item.fromJson(ingredient))
						}
						// Bake sorted list
						sorted.sort(recipeComparator)
						val recipeId = recipe["id"] as String?
						val priority = Utils.toInt(recipe["priority"])
						val result = ShapelessRecipe(recipeId, priority, Item.fromJson(first), sorted)
						registerRecipe(result)
					}
					1 -> {
						craftingBlock = recipe["block"] as String?
						if ("crafting_table" != craftingBlock) {
							// Ignore other recipes than crafting table ones
							continue
						}
						outputs = recipe["output"] as List<Map<*, *>?>?
						first = outputs.removeAt(0)
						val shape = (recipe["shape"] as List<String>?)!!.toTypedArray()
						val ingredients: MutableMap<Char, Item> = CharObjectHashMap()
						val extraResults: MutableList<Item> = ArrayList()
						val input: Map<String, Map<String, Any>>? = recipe["input"] as Map<*, *>?
						for ((key, value) in input!!) {
							val ingredientChar = key[0]
							val ingredient = Item.fromJson(value)
							ingredients[ingredientChar] = ingredient
						}
						for (data in outputs) {
							extraResults.add(Item.fromJson(data))
						}
						recipeId = recipe["id"] as String?
						priority = Utils.toInt(recipe["priority"])
						registerRecipe(ShapedRecipe(recipeId, priority, Item.fromJson(first), shape, ingredients, extraResults))
					}
					2, 3 -> {
						craftingBlock = recipe["block"] as String?
						if ("furnace" != craftingBlock) {
							// Ignore other recipes than furnaces
							continue
						}
						val resultMap: Map<String, Any>? = recipe["output"] as Map<*, *>?
						val resultItem = Item.fromJson(resultMap)
						var inputItem: Item
						inputItem = try {
							val inputMap: Map<String, Any>? = recipe["input"] as Map<*, *>?
							Item.fromJson(inputMap)
						} catch (old: Exception) {
							Item.get(Utils.toInt(recipe["inputId"]), if (recipe.containsKey("inputDamage")) Utils.toInt(recipe["inputDamage"]) else -1, 1)
						}
						registerRecipe(FurnaceRecipe(resultItem, inputItem))
					}
					else -> {
					}
				}
			} catch (e: Exception) {
				MainLogger.getLogger().error("Exception during registering recipe", e)
			}
		}

		// Load brewing recipes
		val potionMixes = config.getMapList("potionMixes")
		for (potionMix in potionMixes) {
			val fromPotionId: Int = (potionMix["fromPotionId"] as Number?).intValue() // gson returns doubles...
			val ingredient: Int = (potionMix["ingredient"] as Number?).intValue()
			val toPotionId: Int = (potionMix["toPotionId"] as Number?).intValue()
			registerBrewingRecipe(BrewingRecipe(Item.get(ItemID.POTION, fromPotionId), Item.get(ingredient), Item.get(ItemID.POTION, toPotionId)))
		}
		val containerMixes = config.getMapList("containerMixes")
		for (containerMix in containerMixes) {
			val fromItemId: Int = (containerMix["fromItemId"] as Number?).intValue()
			val ingredient: Int = (containerMix["ingredient"] as Number?).intValue()
			val toItemId: Int = (containerMix["toItemId"] as Number?).intValue()
			registerContainerRecipe(ContainerRecipe(Item.get(fromItemId), Item.get(ingredient), Item.get(toItemId)))
		}
	}

	fun rebuildPacket() {
		val pk = CraftingDataPacket()
		pk.cleanRecipes = true
		for (recipe in getRecipes()) {
			if (recipe is ShapedRecipe) {
				pk.addShapedRecipe(recipe)
			} else if (recipe is ShapelessRecipe) {
				pk.addShapelessRecipe(recipe)
			}
		}
		for (recipe in getFurnaceRecipes().values) {
			pk.addFurnaceRecipe(recipe)
		}
		for (recipe in brewingRecipes.values) {
			pk.addBrewingRecipe(recipe)
		}
		for (recipe in containerRecipes.values) {
			pk.addContainerRecipe(recipe)
		}
		pk.encode()
		packet = pk.compress(Deflater.BEST_COMPRESSION)
	}

	fun getRecipes(): Collection<Recipe> {
		return recipes
	}

	fun getFurnaceRecipes(): Map<Int, FurnaceRecipe> {
		return furnaceRecipes
	}

	fun matchFurnaceRecipe(input: Item): FurnaceRecipe? {
		var recipe = furnaceRecipes[getItemHash(input)]
		if (recipe == null) recipe = furnaceRecipes[getItemHash(input.id, 0)]
		return recipe
	}

	fun registerFurnaceRecipe(recipe: FurnaceRecipe) {
		val input = recipe.input
		furnaceRecipes[getItemHash(input)] = recipe
	}

	fun registerShapedRecipe(recipe: ShapedRecipe) {
		val resultHash = getItemHash(recipe.result)
		val map = shapedRecipes.computeIfAbsent(resultHash) { k: Int? -> HashMap() }
		map[getMultiItemHash(recipe.ingredientList)] = recipe
	}

	private fun cloneItemMap(map: Array<Array<Item?>>): Array<Array<Item?>> {
		val newMap: Array<Array<Item?>> = arrayOfNulls(map.size)
		for (i in newMap.indices) {
			val old = map[i]
			val n = arrayOfNulls<Item>(old.size)
			System.arraycopy(old, 0, n, 0, n.size)
			newMap[i] = n
		}
		for (y in newMap.indices) {
			val row = newMap[y]
			for (x in row.indices) {
				val item = newMap[y][x]
				newMap[y][x] = item!!.clone()
			}
		}
		return newMap
	}

	fun registerRecipe(recipe: Recipe) {
		if (recipe is CraftingRecipe) {
			val id = Utils.dataToUUID(++RECIPE_COUNT.toString(), recipe.getResult().id.toString(), recipe.getResult().damage.toString(), recipe.getResult().getCount().toString(), Arrays.toString(recipe.getResult().compoundTag))
			recipe.id = id
			recipes.add(recipe)
		}
		recipe.registerToCraftingManager(this)
	}

	fun registerShapelessRecipe(recipe: ShapelessRecipe) {
		val list = recipe.ingredientList
		list.sort(recipeComparator)
		val hash = getMultiItemHash(list)
		val resultHash = getItemHash(recipe.result)
		val map = shapelessRecipes.computeIfAbsent(resultHash) { k: Int? -> HashMap() }
		map[hash] = recipe
	}

	fun registerBrewingRecipe(recipe: BrewingRecipe) {
		val input = recipe.ingredient
		val potion = recipe.input
		brewingRecipes[getPotionHash(input!!.id, potion!!.damage)] = recipe
	}

	fun registerContainerRecipe(recipe: ContainerRecipe) {
		val input = recipe.ingredient
		val potion = recipe.input
		containerRecipes[getContainerHash(input!!.id, potion!!.id)] = recipe
	}

	fun matchBrewingRecipe(input: Item, potion: Item): BrewingRecipe? {
		val id = potion.id
		return if (id == Item.POTION || id == Item.SPLASH_POTION || id == Item.LINGERING_POTION) {
			brewingRecipes[getPotionHash(input.id, potion.damage)]
		} else null
	}

	fun matchContainerRecipe(input: Item, potion: Item): ContainerRecipe? {
		return containerRecipes[getContainerHash(input.id, potion.id)]
	}

	fun matchRecipe(inputMap: Array<Array<Item?>>, primaryOutput: Item?, extraOutputMap: Array<Array<Item?>>): CraftingRecipe? {
		//TODO: try to match special recipes before anything else (first they need to be implemented!)
		val outputHash = getItemHash(primaryOutput)
		if (shapedRecipes.containsKey(outputHash)) {
			val itemCol: MutableList<Item?> = ArrayList()
			for (items in inputMap) itemCol.addAll(Arrays.asList(*items))
			val inputHash = getMultiItemHash(itemCol)
			val recipeMap: Map<UUID?, ShapedRecipe>? = shapedRecipes[outputHash]
			if (recipeMap != null) {
				val recipe = recipeMap[inputHash]
				if (recipe != null && recipe.matchItems(cloneItemMap(inputMap), cloneItemMap(extraOutputMap))) { //matched a recipe by hash
					return recipe
				}
				for (shapedRecipe in recipeMap.values) {
					if (shapedRecipe.matchItems(cloneItemMap(inputMap), cloneItemMap(extraOutputMap))) {
						return shapedRecipe
					}
				}
			}
		}
		if (shapelessRecipes.containsKey(outputHash)) {
			val list: MutableList<Item?> = ArrayList()
			for (a in inputMap) {
				list.addAll(Arrays.asList(*a))
			}
			list.sort(recipeComparator)
			val inputHash = getMultiItemHash(list)
			val recipes = shapelessRecipes[outputHash] ?: return null
			val recipe = recipes[inputHash]
			if (recipe != null && recipe.matchItems(cloneItemMap(inputMap), cloneItemMap(extraOutputMap))) {
				return recipe
			}
			for (shapelessRecipe in recipes.values) {
				if (shapelessRecipe.matchItems(cloneItemMap(inputMap), cloneItemMap(extraOutputMap))) {
					return shapelessRecipe
				}
			}
		}
		return null
	}

	class Entry(val resultItemId: Int, val resultMeta: Int, val ingredientItemId: Int, val ingredientMeta: Int, val recipeShape: String, val resultAmount: Int)

	companion object {
		var packet: BatchPacket? = null
		private const val RECIPE_COUNT = 0
		val recipeComparator = label@ Comparator { i1: Item, i2: Item ->
			if (i1.id > i2.id) {
				return@label 1
			} else if (i1.id < i2.id) {
				return@label -1
			} else if (i1.damage > i2.damage) {
				return@label 1
			} else if (i1.damage < i2.damage) {
				return@label -1
			} else return@label Integer.compare(i1.count, i2.count)
		}

		private fun getMultiItemHash(items: Collection<Item?>?): UUID {
			val stream = BinaryStream()
			for (item in items!!) {
				stream.putVarInt(getFullItemHash(item))
			}
			return UUID.nameUUIDFromBytes(stream.buffer)
		}

		private fun getFullItemHash(item: Item?): Int {
			return 31 * getItemHash(item) + item!!.getCount()
		}

		private fun getItemHash(item: Item?): Int {
			return getItemHash(item!!.id, item.damage)
		}

		private fun getItemHash(id: Int, meta: Int): Int {
			return id shl 4 or (meta and 0xf)
		}

		private fun getPotionHash(ingredientId: Int, potionType: Int): Int {
			return ingredientId shl 6 or potionType
		}

		private fun getContainerHash(ingredientId: Int, containerId: Int): Int {
			return ingredientId shl 9 or containerId
		}
	}

	init {
		val recipesStream = Server::class.java.classLoader.getResourceAsStream("recipes.json")
				?: throw AssertionError("Unable to find recipes.json")
		val recipesConfig = Config(Config.JSON)
		recipesConfig.load(recipesStream)
		loadRecipes(recipesConfig)
		val path = Server.instance!!.dataPath + "custom_recipes.json"
		val filePath = File(path)
		if (filePath.exists()) {
			val customRecipes = Config(filePath, Config.JSON)
			loadRecipes(customRecipes)
		}
		rebuildPacket()
		MainLogger.getLogger().info("Loaded " + recipes.size + " recipes.")
	}
}