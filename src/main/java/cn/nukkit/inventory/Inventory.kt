package cn.nukkit.inventory

import cn.nukkit.Player
import cn.nukkit.item.Item

/**
 * author: MagicDroidX
 * Nukkit Project
 */
interface Inventory {
	val size: Int
	var maxStackSize: Int
	val name: String?
	val title: String?
	fun getItem(index: Int): Item?
	fun setItem(index: Int, item: Item?): Boolean {
		return setItem(index, item, true)
	}

	fun setItem(index: Int, item: Item?, send: Boolean): Boolean
	fun addItem(vararg slots: Item): Array<Item?>
	fun canAddItem(item: Item): Boolean
	fun removeItem(vararg slots: Item): Array<Item?>
	fun getContents(): Map<Int?, Item?>?
	fun setContents(items: Map<Int?, Item?>)
	fun sendContents(player: Player?)
	fun sendContents(vararg players: Player)
	fun sendContents(players: Collection<Player?>?)
	fun sendSlot(index: Int, player: Player?)
	fun sendSlot(index: Int, vararg players: Player)
	fun sendSlot(index: Int, players: Collection<Player?>?)
	operator fun contains(item: Item): Boolean
	fun all(item: Item): Map<Int?, Item?>
	fun first(item: Item): Int {
		return first(item, false)
	}

	fun first(item: Item, exact: Boolean): Int
	fun firstEmpty(item: Item?): Int
	fun decreaseCount(slot: Int)
	fun remove(item: Item)
	fun clear(index: Int): Boolean {
		return clear(index, true)
	}

	fun clear(index: Int, send: Boolean): Boolean
	fun clearAll()
	val isFull: Boolean
	val isEmpty: Boolean
	val viewers: Set<Player?>?
	val type: InventoryType?
	val holder: InventoryHolder?
	fun onOpen(who: Player)
	fun open(who: Player): Boolean
	fun close(who: Player)
	fun onClose(who: Player)
	fun onSlotChange(index: Int, before: Item?, send: Boolean)

	companion object {
		const val MAX_STACK = 64
	}
}