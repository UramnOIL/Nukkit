package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.level.Level
import cn.nukkit.level.Location
import cn.nukkit.level.Position
import cn.nukkit.math.Vector3

class PlayerTeleportEvent private constructor(player: Player) : PlayerEvent(), Cancellable {
	var cause: TeleportCause? = null
		private set
	var from: Location? = null
		private set
	var to: Location? = null
		private set

	constructor(player: Player, from: Location?, to: Location?, cause: TeleportCause?) : this(player) {
		this.from = from
		this.to = to
		this.cause = cause
	}

	constructor(player: Player, from: Vector3, to: Vector3, cause: TeleportCause?) : this(player) {
		this.from = vectorToLocation(player.level, from)
		this.from = vectorToLocation(player.level, to)
		this.cause = cause
	}

	private fun vectorToLocation(baseLevel: Level, vector: Vector3): Location {
		if (vector is Location) return vector
		return if (vector is Position) vector.location else Location(vector.getX(), vector.getY(), vector.getZ(), 0, 0, baseLevel)
	}

	enum class TeleportCause {
		COMMAND,  // For Nukkit tp command only
		PLUGIN,  // Every plugin
		NETHER_PORTAL,  // Teleport using Nether portal
		ENDER_PEARL,  // Teleport by ender pearl
		CHORUS_FRUIT,  // Teleport by chorus fruit
		UNKNOWN // Unknown cause
	}

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
	}
}