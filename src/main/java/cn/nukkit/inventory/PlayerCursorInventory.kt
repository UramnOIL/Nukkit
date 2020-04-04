package cn.nukkit.inventory

/**
 * @author CreeperFace
 */
class PlayerCursorInventory internal constructor(private val playerUI: PlayerUIInventory) : PlayerUIComponent(playerUI, 0, 1) {

	/**
	 * This override is here for documentation and code completion purposes only.
	 *
	 * @return Player
	 */
	override var holder: InventoryHolder?
		get() = playerUI.getHolder()
		set(holder) {
			super.holder = holder
		}

}