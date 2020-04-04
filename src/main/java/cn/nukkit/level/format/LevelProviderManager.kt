package cn.nukkit.level.format

import cn.nukkit.Server
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
object LevelProviderManager {
	internal val providers: MutableMap<String, Class<out LevelProvider>?> = HashMap()
	fun addProvider(server: Server?, clazz: Class<out LevelProvider>) {
		try {
			providers[clazz.getMethod("getProviderName").invoke(null) as String] = clazz
		} catch (e: Exception) {
			Server.instance!!.logger.logException(e)
		}
	}

	fun getProvider(path: String?): Class<out LevelProvider>? {
		for (provider in providers.values) {
			try {
				if (provider!!.getMethod("isValid", String::class.java).invoke(null, path) as Boolean) {
					return provider
				}
			} catch (e: Exception) {
				Server.instance!!.logger.logException(e)
			}
		}
		return null
	}

	fun getProviderByName(name: String): Class<out LevelProvider>? {
		var name = name
		name = name.trim { it <= ' ' }.toLowerCase()
		return providers.getOrDefault(name, null)
	}
}