package cn.nukkit.plugin

import java.io.File
import java.net.URLClassLoader
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class PluginClassLoader(private val loader: JavaPluginLoader, parent: ClassLoader?, file: File?) : URLClassLoader(arrayOf(file!!.toURI().toURL()), parent) {
	private val classes: MutableMap<String, Class<*>?> = HashMap()

	@Throws(ClassNotFoundException::class)
	override fun findClass(name: String): Class<*>? {
		return this.findClass(name, true)
	}

	@Throws(ClassNotFoundException::class)
	fun findClass(name: String, checkGlobal: Boolean): Class<*>? {
		if (name.startsWith("cn.nukkit.") || name.startsWith("net.minecraft.")) {
			throw ClassNotFoundException(name)
		}
		var result = classes[name]
		if (result == null) {
			if (checkGlobal) {
				result = loader.getClassByName(name)
			}
			if (result == null) {
				result = super.findClass(name)
				if (result != null) {
					loader.setClass(name, result)
				}
			}
			classes[name] = result
		}
		return result
	}

	fun getClasses(): Set<String> {
		return classes.keys
	}

}