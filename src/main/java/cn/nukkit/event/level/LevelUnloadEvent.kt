package cn.nukkit.event.level

import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.level.Level

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class LevelUnloadEvent(level: Level) : LevelEvent(level), Cancellable {
	companion object {
		val handlers = HandlerList()
	}
}