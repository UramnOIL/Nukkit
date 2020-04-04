package cn.nukkit.inventory.transaction.action

import cn.nukkit.Player
import cn.nukkit.event.player.PlayerDropItemEvent
import cn.nukkit.item.Item

/**
 * @author CreeperFace
 */
class DropItemAction(source: Item?, target: Item?) : InventoryAction(source, target) {
	/**
	 * Verifies that the source item of a drop-item action must be air. This is not strictly necessary, just a sanity
	 * check.
	 */
	override fun isValid(source: Player?): Boolean {
		return sourceItem!!.isNull
	}

	override fun onPreExecute(source: Player?): Boolean {
		var ev: PlayerDropItemEvent
		source!!.getServer().pluginManager.callEvent(PlayerDropItemEvent(source, targetItem!!).also { ev = it })
		return !ev.isCancelled
	}

	/**
	 * Drops the target item in front of the player.
	 */
	override fun execute(source: Player?): Boolean {
		return source!!.dropItem(targetItem!!)
	}

	override fun onExecuteSuccess(source: Player?) {}
	override fun onExecuteFail(source: Player?) {}
}