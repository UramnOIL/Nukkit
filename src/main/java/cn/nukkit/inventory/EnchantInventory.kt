package cn.nukkit.inventory

import cn.nukkit.Player
import cn.nukkit.level.Position

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EnchantInventory(playerUI: PlayerUIInventory, position: Position) : FakeBlockUIComponent(playerUI, InventoryType.ENCHANT_TABLE, 14, position) {
	override fun onOpen(who: Player) {
		super.onOpen(who)
		who.craftingType = Player.CRAFTING_ENCHANT
	}

	override fun onClose(who: Player) {
		super.onClose(who)
		if (getViewers()!!.size == 0) {
			for (i in 0..1) {
				who.inventory.addItem(getItem(i)!!)
				this.clear(i)
			}
		}
	}
}