package cn.nukkit.event.server

import cn.nukkit.event.HandlerList
import cn.nukkit.utils.PlayerDataSerializer
import com.google.common.base.Preconditions
import java.util.*

class PlayerDataSerializeEvent(name: String, serializer: PlayerDataSerializer) : ServerEvent() {
	val name: Optional<String>
	val uuid: Optional<UUID>
	private var serializer: PlayerDataSerializer

	fun getSerializer(): PlayerDataSerializer {
		return serializer
	}

	fun setSerializer(serializer: PlayerDataSerializer) {
		this.serializer = Preconditions.checkNotNull(serializer, "serializer")
	}

	companion object {
		val handlers = HandlerList()
	}

	init {
		Preconditions.checkNotNull(name)
		this.serializer = Preconditions.checkNotNull(serializer)
		var uuid: UUID? = null
		try {
			uuid = UUID.fromString(name)
		} catch (e: Exception) {
			// ignore
		}
		this.uuid = Optional.ofNullable(uuid)
		this.name = if (this.uuid.isPresent) Optional.empty() else Optional.of(name)
	}
}