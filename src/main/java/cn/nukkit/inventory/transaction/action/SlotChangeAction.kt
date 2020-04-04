package cn.nukkit.inventory.transaction.action

import cn.nukkit.Player
import cn.nukkit.inventory.Inventory
import cn.nukkit.inventory.transaction.InventoryTransaction
import cn.nukkit.item.Item
import java.util.*

/**
 * @author CreeperFace
 */
class SlotChangeAction(
		/**
		 * Returns the inventory involved in this action.
		 *
		 * @return inventory
		 */
		var inventory: Inventory?,
		/**
		 * Returns the inventorySlot in the inventory which this action modified.
		 *
		 * @return slot
		 */
		val slot: Int, sourceItem: Item?, targetItem: Item?) : InventoryAction(sourceItem, targetItem) {

	/**
	 * Checks if the item in the inventory at the specified inventorySlot is the same as this action's source item.
	 *
	 * @param source player
	 * @return valid
	 */
	override fun isValid(source: Player?): Boolean {
		val check = inventory!!.getItem(slot)
		return check!!.equalsExact(sourceItem)
	}

	/**
	 * Sets the item into the target inventory.
	 *
	 * @param source player
	 * @return successfully executed
	 */
	override fun execute(source: Player?): Boolean {
		return inventory!!.setItem(slot, targetItem, false)
	}

	/**
	 * Sends inventorySlot changes to other viewers of the inventory. This will not send any change back to the source Player.
	 *
	 * @param source player
	 */
	override fun onExecuteSuccess(source: Player?) {
		val viewers: MutableSet<Player?> = HashSet(inventory.getViewers())
		viewers.remove(source)
		inventory!!.sendSlot(slot, viewers)
	}

	/**
	 * Sends the original inventorySlot contents to the source player to revert the action.
	 *
	 * @param source player
	 */
	override fun onExecuteFail(source: Player?) {
		inventory!!.sendSlot(slot, source!!)
	}

	override fun onAddToTransaction(transaction: InventoryTransaction) {
		transaction.addInventory(inventory)
	}

}