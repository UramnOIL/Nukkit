package cn.nukkit.inventory

import cn.nukkit.item.Item
import java.util.*

/**
 * @author CreeperFace
 */
interface CraftingRecipe : Recipe {
	val recipeId: String?
	var id: UUID
	fun requiresCraftingTable(): Boolean
	val extraResults: MutableList<Item>
	val allResults: List<Item>?
	val priority: Int

	/**
	 * Returns whether the specified list of crafting grid inputs and outputs matches this recipe. Outputs DO NOT
	 * include the primary result item.
	 *
	 * @param input  2D array of items taken from the crafting grid
	 * @param output 2D array of items put back into the crafting grid (secondary results)
	 * @return bool
	 */
	fun matchItems(input: Array<Array<Item?>>, output: Array<Array<Item?>>): Boolean
}