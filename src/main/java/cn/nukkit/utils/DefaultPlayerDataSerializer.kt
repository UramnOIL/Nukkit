package cn.nukkit.utils

import cn.nukkit.Server
import com.google.common.base.Preconditions
import lombok.RequiredArgsConstructor
import java.io.*
import java.util.*

@RequiredArgsConstructor
class DefaultPlayerDataSerializer : PlayerDataSerializer {
	private val server: Server? = null

	@Throws(IOException::class)
	override fun read(name: String, uuid: UUID): Optional<InputStream> {
		val path = server!!.dataPath + "players/" + name + ".dat"
		val file = File(path)
		return if (!file.exists()) {
			Optional.empty()
		} else Optional.of(FileInputStream(file))
	}

	@Throws(IOException::class)
	override fun write(name: String, uuid: UUID): OutputStream {
		Preconditions.checkNotNull(name, "name")
		val path = server!!.dataPath + "players/" + name + ".dat"
		val file = File(path)
		return FileOutputStream(file)
	}
}