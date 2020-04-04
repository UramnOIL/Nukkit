package cn.nukkit.inventory.transaction.action

import cn.nukkit.Player
import cn.nukkit.inventory.transaction.CraftingTransaction
import cn.nukkit.inventory.transaction.InventoryTransaction
import cn.nukkit.item.Item

/**
 * @author CreeperFace
 */
class CraftingTakeResultAction(sourceItem: Item?, targetItem: Item?) : InventoryAction(sourceItem, targetItem) {
	override fun onAddToTransaction(transaction: InventoryTransaction) {
		if (transaction is CraftingTransaction) {
			transaction.primaryOutput = getSourceItem()
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