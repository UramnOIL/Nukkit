package cn.nukkit.inventory

import cn.nukkit.Player
import cn.nukkit.level.Position

/**
 * author: Rover656
 */
class BeaconInventory(playerUI: PlayerUIInventory, position: Position) : FakeBlockUIComponent(playerUI, InventoryType.BEACON, 27, position) {
	override fun onClose(who: Player) {
		super.onClose(who)

		//Drop item in slot
		getHolder().getLevel().dropItem(getHolder().add(0.5, 0.5, 0.5), getItem(0))
		this.clear(0)
	}
}