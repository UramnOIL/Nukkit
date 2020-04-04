package cn.nukkit.inventory

import cn.nukkit.item.Item

class BrewingRecipe(input: Item, ingredient: Item, output: Item) : MixRecipe(input, ingredient, output) {
	override fun registerToCraftingManager(manager: CraftingManager) {
		manager.registerBrewingRecipe(this)
	}

	override val type: RecipeType?
		get() {
			throw UnsupportedOperationException()
		}
}