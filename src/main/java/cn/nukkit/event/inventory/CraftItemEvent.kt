package cn.nukkit.event.inventory

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.Event
import cn.nukkit.event.HandlerList
import cn.nukkit.inventory.Recipe
import cn.nukkit.inventory.transaction.CraftingTransaction
import cn.nukkit.item.Item
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class CraftItemEvent : Event, Cancellable {
	var input = arrayOfNulls<Item>(0)
		private set
	val recipe: Recipe
	val player: Player
	var transaction: CraftingTransaction? = null
		private set

	constructor(transaction: CraftingTransaction) {
		this.transaction = transaction
		val merged: MutableList<Item> = ArrayList()
		val input = transaction.inputMap
		for (items in input) {
			merged.addAll(Arrays.asList(*items))
		}
		player = transaction.source
		this.input = merged.toTypedArray()
		recipe = transaction.recipe
	}

	constructor(player: Player, input: Array<Item?>, recipe: Recipe) {
		this.player = player
		this.input = input
		this.recipe = recipe
	}

	companion object {
		val handlers = HandlerList()
	}
}