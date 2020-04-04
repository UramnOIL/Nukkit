package cn.nukkit.network.protocol.types

import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

class EntityLink(var fromEntityUniquieId: Long, var toEntityUniquieId: Long, var type: Byte, var immediate: Boolean) {

	companion object {
		const val TYPE_REMOVE: Byte = 0
		const val TYPE_RIDER: Byte = 1
		const val TYPE_PASSENGER: Byte = 2
	}

}