package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.form.window.FormWindow
import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.collections.set

/**
 * @author CreeperFace
 */
class PlayerServerSettingsRequestEvent(player: Player?, settings: MutableMap<Int, FormWindow>) : PlayerEvent(), Cancellable {
	private var settings: MutableMap<Int, FormWindow>
	fun getSettings(): Map<Int, FormWindow> {
		return settings
	}

	fun setSettings(settings: MutableMap<Int, FormWindow>) {
		this.settings = settings
	}

	fun setSettings(id: Int, window: FormWindow) {
		settings[id] = window
	}

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		this.settings = settings
	}
}