package cn.nukkit.inventory

import cn.nukkit.item.Item

class ContainerRecipe(input: Item, ingredient: Item, output: Item) : MixRecipe(input, ingredient, output) {
	override fun registerToCraftingManager(manager: CraftingManager) {
		manager.registerContainerRecipe(this)
	}

	override val type: RecipeType?
		get() {
			throw UnsupportedOperationException()
		}
}