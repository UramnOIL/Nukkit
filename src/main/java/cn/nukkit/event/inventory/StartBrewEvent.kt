package cn.nukkit.event.inventory

import cn.nukkit.blockentity.BlockEntityBrewingStand
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.item.Item

/**
 * @author CreeperFace
 */
class StartBrewEvent(val brewingStand: BlockEntityBrewingStand) : InventoryEvent(brewingStand.inventory), Cancellable {
	val ingredient: Item
	val potions: Array<Item?>

	/**
	 * @param index Potion index in range 0 - 2
	 * @return potion
	 */
	fun getPotion(index: Int): Item? {
		return potions[index]
	}

	companion object {
		val handlers = HandlerList()
	}

	init {
		ingredient = brewingStand.inventory.ingredient
		potions = arrayOfNulls(3)
		for (i in 0..2) {
			potions[i] = brewingStand.inventory.getItem(i)
		}
	}
}