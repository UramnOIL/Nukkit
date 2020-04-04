package cn.nukkit.event.plugin

import cn.nukkit.event.Event
import cn.nukkit.event.HandlerList
import cn.nukkit.plugin.Plugin

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class PluginEvent(val plugin: Plugin) : Event() {

	companion object {
		val handlers = HandlerList()
	}

}