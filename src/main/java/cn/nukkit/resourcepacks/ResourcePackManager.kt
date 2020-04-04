package cn.nukkit.resourcepacks

import cn.nukkit.Server
import com.google.common.io.Files
import java.io.File
import java.util.*

class ResourcePackManager(path: File) {
	private val resourcePacksById: MutableMap<UUID?, ResourcePack> = HashMap()
	val resourceStack: Array<ResourcePack>

	fun getPackById(id: UUID?): ResourcePack? {
		return resourcePacksById[id]
	}

	init {
		if (!path.exists()) {
			path.mkdirs()
		} else require(path.isDirectory) {
			Server.instance.language
					.translateString("nukkit.resources.invalid-path", path.name)
		}
		val loadedResourcePacks: MutableList<ResourcePack> = ArrayList()
		for (pack in path.listFiles()) {
			try {
				var resourcePack: ResourcePack? = null
				if (!pack.isDirectory) { //directory resource packs temporarily unsupported
					when (Files.getFileExtension(pack.name)) {
						"zip", "mcpack" -> resourcePack = ZippedResourcePack(pack)
						else -> Server.instance.logger.warning(Server.instance.language
								.translateString("nukkit.resources.unknown-format", pack.name))
					}
				}
				if (resourcePack != null) {
					loadedResourcePacks.add(resourcePack)
					resourcePacksById[resourcePack.packId] = resourcePack
				}
			} catch (e: IllegalArgumentException) {
				Server.instance.logger.warning(Server.instance.language
						.translateString("nukkit.resources.fail", pack.name, e.message))
			}
		}
		resourceStack = loadedResourcePacks.toTypedArray()
		Server.instance.logger.info(Server.instance.language
				.translateString("nukkit.resources.success", resourceStack.size.toString()))
	}
}