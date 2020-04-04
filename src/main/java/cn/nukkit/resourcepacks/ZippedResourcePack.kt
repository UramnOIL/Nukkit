package cn.nukkit.resourcepacks

import cn.nukkit.Server
import com.google.gson.JsonParser
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.MessageDigest
import java.util.zip.ZipFile

class ZippedResourcePack(file: File) : AbstractResourcePack() {
	private val file: File
	override var sha256: ByteArray? = null
		get() {
			if (field == null) {
				try {
					field = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file.toPath()))
				} catch (e: Exception) {
					Server.instance.logger.logException(e)
				}
			}
			return field
		}
		private set
	override val packSize: Int
		get() = file.length().toInt()

	override fun getPackChunk(off: Int, len: Int): ByteArray {
		val chunk: ByteArray
		chunk = if (packSize - off > len) {
			ByteArray(len)
		} else {
			ByteArray(packSize - off)
		}
		try {
			FileInputStream(file).use { fis ->
				fis.skip(off.toLong())
				fis.read(chunk)
			}
		} catch (e: Exception) {
			Server.instance.logger.logException(e)
		}
		return chunk
	}

	init {
		require(file.exists()) {
			Server.instance.language
					.translateString("nukkit.resources.zip.not-found", file.name)
		}
		this.file = file
		try {
			ZipFile(file).use { zip ->
				val entry = zip.getEntry("manifest.json")
				requireNotNull(entry) {
					Server.instance.language
							.translateString("nukkit.resources.zip.no-manifest")
				}
				manifest = JsonParser()
						.parse(InputStreamReader(zip.getInputStream(entry), StandardCharsets.UTF_8))
						.asJsonObject
			}
		} catch (e: IOException) {
			Server.instance.logger.logException(e)
		}
		require(verifyManifest()) {
			Server.instance.language
					.translateString("nukkit.resources.zip.invalid-manifest")
		}
	}
}