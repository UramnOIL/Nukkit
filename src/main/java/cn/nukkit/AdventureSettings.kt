package cn.nukkit

import cn.nukkit.Server.Companion.broadcastPacket
import cn.nukkit.network.protocol.AdventureSettingsPacket
import java.util.*

/**
 * Nukkit Project
 * Author: MagicDroidX
 */
class AdventureSettings(private var player: Player) : Cloneable {
	private val values: MutableMap<Type, Boolean> = EnumMap(Type::class.java)
	fun clone(newPlayer: Player): AdventureSettings? {
		return try {
			val settings = super.clone() as AdventureSettings
			settings.player = newPlayer
			settings
		} catch (e: CloneNotSupportedException) {
			null
		}
	}

	operator fun set(type: Type, value: Boolean): AdventureSettings {
		values[type] = value
		return this
	}

	operator fun get(type: Type): Boolean {
		val value = values[type]
		return value ?: type.defaultValue
	}

	fun update() {
		val pk = AdventureSettingsPacket()
		for (t in Type.values()) {
			pk.setFlag(t.id, get(t))
		}
		pk.commandPermission = (if (player.isOp) AdventureSettingsPacket.PERMISSION_OPERATOR else AdventureSettingsPacket.PERMISSION_NORMAL).toLong()
		pk.playerPermission = (if (player.isOp) Player.PERMISSION_OPERATOR else Player.PERMISSION_MEMBER).toLong()
		pk.entityUniqueId = player.id
		broadcastPacket(player.viewers.values, pk)
		player.dataPacket(pk)
		player.resetInAirTicks()
	}

	enum class Type(val id: Int, val defaultValue: Boolean) {
		WORLD_IMMUTABLE(AdventureSettingsPacket.WORLD_IMMUTABLE, false), AUTO_JUMP(AdventureSettingsPacket.AUTO_JUMP, true), ALLOW_FLIGHT(AdventureSettingsPacket.ALLOW_FLIGHT, false), NO_CLIP(AdventureSettingsPacket.NO_CLIP, false), WORLD_BUILDER(AdventureSettingsPacket.WORLD_BUILDER, true), FLYING(AdventureSettingsPacket.FLYING, false), MUTED(AdventureSettingsPacket.MUTED, false), BUILD_AND_MINE(AdventureSettingsPacket.BUILD_AND_MINE, true), DOORS_AND_SWITCHED(AdventureSettingsPacket.DOORS_AND_SWITCHES, true), OPEN_CONTAINERS(AdventureSettingsPacket.OPEN_CONTAINERS, true), ATTACK_PLAYERS(AdventureSettingsPacket.ATTACK_PLAYERS, true), ATTACK_MOBS(AdventureSettingsPacket.ATTACK_MOBS, true), OPERATOR(AdventureSettingsPacket.OPERATOR, false), TELEPORT(AdventureSettingsPacket.TELEPORT, false);

	}

	companion object {
		const val PERMISSION_NORMAL = 0
		const val PERMISSION_OPERATOR = 1
		const val PERMISSION_HOST = 2
		const val PERMISSION_AUTOMATION = 3
		const val PERMISSION_ADMIN = 4
	}

}