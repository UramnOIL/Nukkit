package cn.nukkit.inventory.transaction.action

import cn.nukkit.Player
import cn.nukkit.inventory.transaction.CraftingTransaction
import cn.nukkit.inventory.transaction.InventoryTransaction
import cn.nukkit.item.Item

/**
 * @author CreeperFace
 */
class CraftingTransferMaterialAction(sourceItem: Item?, targetItem: Item?, private val slot: Int) : InventoryAction(sourceItem, targetItem) {
	override fun onAddToTransaction(transaction: InventoryTransaction) {
		if (transaction is CraftingTransaction) {
			if (sourceItem!!.isNull) {
				transaction.setInput(slot, targetItem)
			} else if (targetItem!!.isNull) {
				transaction.setExtraOutput(slot, sourceItem)
			} else {
				throw RuntimeException("Invalid " + javaClass.name + ", either source or target item must be air, got source: " + sourceItem + ", target: " + targetItem)
			}
		} else {
			throw RuntimeException(javaClass.name + " can only be added to CraftingTransactions")
		}
	}

	override fun isValid(source: Player?): Boolean {
		return true
	}

	override fun execute(source: Player?): Boolean {
		return true
	}

	override fun onExecuteSuccess(`$source`: Player?) {}
	override fun onExecuteFail(source: Player?) {}

}