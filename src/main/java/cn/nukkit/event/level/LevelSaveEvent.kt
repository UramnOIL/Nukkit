package cn.nukkit.event.level

import cn.nukkit.event.HandlerList
import cn.nukkit.level.Level

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class LevelSaveEvent(level: Level) : LevelEvent(level) {
	companion object {
		val handlers = HandlerList()
	}
}