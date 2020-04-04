package cn.nukkit.resourcepacks

import com.google.gson.JsonObject
import java.util.*

abstract class AbstractResourcePack : ResourcePack {
	protected var manifest: JsonObject? = null
	private var id: UUID? = null
	protected fun verifyManifest(): Boolean {
		return if (manifest!!.has("format_version") && manifest!!.has("header") && manifest!!.has("modules")) {
			val header = manifest!!.getAsJsonObject("header")
			header.has("description") &&
					header.has("name") &&
					header.has("uuid") &&
					header.has("version") && header.getAsJsonArray("version").size() == 3
		} else {
			false
		}
	}

	override val packName: String?
		get() = manifest!!.getAsJsonObject("header")["name"].asString

	override val packId: UUID?
		get() {
			if (id == null) {
				id = UUID.fromString(manifest!!.getAsJsonObject("header")["uuid"].asString)
			}
			return id
		}

	override val packVersion: String
		get() {
			val version = manifest!!.getAsJsonObject("header")["version"].asJsonArray
			return java.lang.String.join(".", version[0].asString,
					version[1].asString,
					version[2].asString)
		}
}