package cn.nukkit.event.level

import cn.nukkit.event.HandlerList
import cn.nukkit.level.Level
import cn.nukkit.level.Position

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class SpawnChangeEvent(level: Level, val previousSpawn: Position) : LevelEvent(level) {

	companion object {
		val handlers = HandlerList()
	}

}