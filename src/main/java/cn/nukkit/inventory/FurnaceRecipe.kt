package cn.nukkit.inventory

import cn.nukkit.item.Item

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class FurnaceRecipe(result: Item, ingredient: Item) : Recipe {
	private val output: Item
	private var ingredient: Item

	var input: Item
		get() = ingredient.clone()
		set(item) {
			ingredient = item.clone()
		}

	override val result: Item
		get() = output.clone()

	override fun registerToCraftingManager(manager: CraftingManager) {
		manager.registerFurnaceRecipe(this)
	}

	override val type: RecipeType?
		get() = if (ingredient.hasMeta()) RecipeType.FURNACE_DATA else RecipeType.FURNACE

	init {
		output = result.clone()
		this.ingredient = ingredient.clone()
	}
}