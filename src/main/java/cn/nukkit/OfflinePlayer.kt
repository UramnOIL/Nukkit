package cn.nukkit

import cn.nukkit.metadata.MetadataValue
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.plugin.Plugin
import java.util.*

/**
 * 描述一个不在线的玩家的类。<br></br>
 * Describes an offline player.
 *
 * @author MagicDroidX(code) @ Nukkit Project
 * @author 粉鞋大妈(javadoc) @ Nukkit Project
 * @see cn.nukkit.Player
 *
 * @since Nukkit 1.0 | Nukkit API 1.0.0
 */
class OfflinePlayer @JvmOverloads constructor(private val server: Server, uuid: UUID?, name: String? = null) : IPlayer {
	private val namedTag: CompoundTag?

	constructor(server: Server, name: String?) : this(server, null, name) {}

	override fun isOnline(): Boolean {
		return this.player != null
	}

	override fun getName(): String? {
		return if (namedTag != null && namedTag.contains("NameTag")) {
			namedTag.getString("NameTag")
		} else null
	}

	override fun getUniqueId(): UUID {
		if (namedTag != null) {
			val least = namedTag.getLong("UUIDLeast")
			val most = namedTag.getLong("UUIDMost")
			if (least != 0L && most != 0L) {
				return UUID(most, least)
			}
		}
		return null
	}

	override fun getServer(): Server {
		return server
	}

	override fun isOp(): Boolean {
		return server.isOp(this.name.toLowerCase())
	}

	override fun setOp(value: Boolean) {
		if (value == this.isOp) {
			return
		}
		if (value) {
			server.addOp(this.name.toLowerCase())
		} else {
			server.removeOp(this.name.toLowerCase())
		}
	}

	override fun isBanned(): Boolean {
		return server.nameBans.isBanned(this.name)
	}

	override fun setBanned(value: Boolean) {
		if (value) {
			server.nameBans.addBan(this.name, null, null, null)
		} else {
			server.nameBans.remove(this.name)
		}
	}

	override fun isWhitelisted(): Boolean {
		return server.isWhitelisted(this.name.toLowerCase())
	}

	override fun setWhitelisted(value: Boolean) {
		if (value) {
			server.addWhitelist(this.name.toLowerCase())
		} else {
			server.removeWhitelist(this.name.toLowerCase())
		}
	}

	override fun getPlayer(): Player {
		return server.getPlayerExact(this.name)!!
	}

	override fun getFirstPlayed(): Long {
		return (if (namedTag != null) namedTag.getLong("firstPlayed") else null)!!
	}

	override fun getLastPlayed(): Long {
		return (if (namedTag != null) namedTag.getLong("lastPlayed") else null)!!
	}

	override fun hasPlayedBefore(): Boolean {
		return namedTag != null
	}

	override fun setMetadata(metadataKey: String, newMetadataValue: MetadataValue) {
		server.playerMetadata.setMetadata(this, metadataKey, newMetadataValue)
	}

	override fun getMetadata(metadataKey: String): List<MetadataValue> {
		return server.playerMetadata.getMetadata(this, metadataKey)
	}

	override fun hasMetadata(metadataKey: String): Boolean {
		return server.playerMetadata.hasMetadata(this, metadataKey)
	}

	override fun removeMetadata(metadataKey: String, owningPlugin: Plugin) {
		server.playerMetadata.removeMetadata(this, metadataKey, owningPlugin)
	}

	/**
	 * 初始化这个`OfflinePlayer`对象。<br></br>
	 * Initializes the object `OfflinePlayer`.
	 *
	 * @param server 这个玩家所在服务器的`Server`对象。<br></br>
	 * The server this player is in, as a `Server` object.
	 * @param uuid   这个玩家的UUID。<br></br>
	 * UUID of this player.
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	init {
		var nbt: CompoundTag?
		nbt = if (uuid != null) {
			server.getOfflinePlayerData(uuid, false)
		} else if (name != null) {
			server.getOfflinePlayerData(name, false)
		} else {
			throw IllegalArgumentException("Name and UUID cannot both be null")
		}
		if (nbt == null) {
			nbt = CompoundTag()
		}
		namedTag = nbt
		if (uuid != null) {
			namedTag.putLong("UUIDMost", uuid.mostSignificantBits)
			namedTag.putLong("UUIDLeast", uuid.leastSignificantBits)
		} else {
			namedTag.putString("NameTag", name)
		}
	}
}