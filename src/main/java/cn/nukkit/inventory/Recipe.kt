package cn.nukkit.inventory

import cn.nukkit.item.Item

/**
 * author: MagicDroidX
 * Nukkit Project
 */
interface Recipe {
	val result: Item
	fun registerToCraftingManager(manager: CraftingManager)
	val type: RecipeType?
}