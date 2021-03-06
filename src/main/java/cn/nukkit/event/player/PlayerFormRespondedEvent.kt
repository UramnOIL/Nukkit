package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.HandlerList
import cn.nukkit.form.response.FormResponse
import cn.nukkit.form.window.FormWindow

class PlayerFormRespondedEvent(player: Player?, formID: Int, window: FormWindow) : PlayerEvent() {
	var formID: Int
		protected set
	var window: FormWindow
		protected set
	protected var closed = false

	/**
	 * Can be null if player closed the window instead of submitting it
	 *
	 * @return response
	 */
	val response: FormResponse
		get() = window.response

	/**
	 * Defines if player closed the window or submitted it
	 *
	 * @return form closed
	 */
	fun wasClosed(): Boolean {
		return window.wasClosed()
	}

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		this.formID = formID
		this.window = window
	}
}