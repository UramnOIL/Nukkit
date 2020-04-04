package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

/**
 * call when a player moves wrongly
 *
 * @author WilliamGao
 */
class PlayerInvalidMoveEvent(player: Player?, revert: Boolean) : PlayerEvent(), Cancellable {
	/**
	 * @param revert revert movement
	 */
	var isRevert: Boolean
		@Deprecated("""If you just simply want to disable the movement check, please use {@link Player#setCheckMovement(boolean)} instead.
      """) set

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		isRevert = revert
	}
}