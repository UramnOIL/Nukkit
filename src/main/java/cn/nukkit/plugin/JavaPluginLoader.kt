package cn.nukkit.plugin

import cn.nukkit.Server
import cn.nukkit.event.plugin.PluginDisableEvent
import cn.nukkit.event.plugin.PluginEnableEvent
import cn.nukkit.utils.PluginException
import cn.nukkit.utils.Utils
import java.io.File
import java.io.IOException
import java.util.*
import java.util.jar.JarFile
import java.util.regex.Pattern

/**
 * Created by Nukkit Team.
 */
class JavaPluginLoader(private val server: Server) : PluginLoader {
	private val classes: MutableMap<String, Class<*>> = HashMap()
	private val classLoaders: MutableMap<String?, PluginClassLoader> = HashMap()

	@Throws(Exception::class)
	override fun loadPlugin(file: File?): Plugin? {
		val description = this.getPluginDescription(file)
		if (description != null) {
			server.logger.info(server.language.translateString("nukkit.plugin.load", description.fullName))
			val dataFolder = File(file!!.parentFile, description.name)
			check(!(dataFolder.exists() && !dataFolder.isDirectory)) { "Projected dataFolder '" + dataFolder.toString() + "' for " + description.name + " exists and is not a directory" }
			val className = description.main
			val classLoader = PluginClassLoader(this, this.javaClass.classLoader, file)
			classLoaders[description.name] = classLoader
			val plugin: PluginBase
			try {
				val javaClass = classLoader.loadClass(className)
				if (!PluginBase::class.java.isAssignableFrom(javaClass)) {
					throw PluginException("Main class `" + description.main + "' does not extend PluginBase")
				}
				try {
					val pluginClass = javaClass.asSubclass(PluginBase::class.java) as Class<PluginBase>
					plugin = pluginClass.newInstance()
					initPlugin(plugin, description, dataFolder, file)
					return plugin
				} catch (e: ClassCastException) {
					throw PluginException("Error whilst initializing main class `" + description.main + "'", e)
				} catch (e: InstantiationException) {
					Server.instance!!.logger.logException(e)
				} catch (e: IllegalAccessException) {
					Server.instance!!.logger.logException(e)
				}
			} catch (e: ClassNotFoundException) {
				throw PluginException("Couldn't load plugin " + description.name + ": main class not found")
			}
		}
		return null
	}

	@Throws(Exception::class)
	override fun loadPlugin(filename: String?): Plugin? {
		return this.loadPlugin(File(filename))
	}

	override fun getPluginDescription(file: File?): PluginDescription? {
		try {
			JarFile(file).use { jar ->
				var entry = jar.getJarEntry("nukkit.yml")
				if (entry == null) {
					entry = jar.getJarEntry("plugin.yml")
					if (entry == null) {
						return null
					}
				}
				jar.getInputStream(entry).use { stream -> return PluginDescription(Utils.readFile(stream)) }
			}
		} catch (e: IOException) {
			return null
		}
	}

	override fun getPluginDescription(filename: String?): PluginDescription? {
		return this.getPluginDescription(File(filename))
	}

	override fun getPluginFilters(): Array<Pattern> {
		return arrayOf(Pattern.compile("^.+\\.jar$"))
	}

	private fun initPlugin(plugin: PluginBase, description: PluginDescription, dataFolder: File, file: File?) {
		plugin.init(this, server, description, dataFolder, file)
		plugin.onLoad()
	}

	override fun enablePlugin(plugin: Plugin) {
		if (plugin is PluginBase && !plugin.isEnabled()) {
			server.logger.info(server.language.translateString("nukkit.plugin.enable", plugin.getDescription()!!.fullName))
			plugin.isEnabled = true
			server.pluginManager.callEvent(PluginEnableEvent(plugin))
		}
	}

	override fun disablePlugin(plugin: Plugin) {
		if (plugin is PluginBase && plugin.isEnabled()) {
			server.logger.info(server.language.translateString("nukkit.plugin.disable", plugin.getDescription()!!.fullName))
			server.serviceManager.cancel(plugin)
			server.pluginManager.callEvent(PluginDisableEvent(plugin))
			plugin.isEnabled = false
		}
	}

	fun getClassByName(name: String): Class<*>? {
		var cachedClass = classes[name]
		if (cachedClass != null) {
			return cachedClass
		} else {
			for (loader in classLoaders.values) {
				try {
					cachedClass = loader.findClass(name, false)
				} catch (e: ClassNotFoundException) {
					//ignore
				}
				if (cachedClass != null) {
					return cachedClass
				}
			}
		}
		return null
	}

	fun setClass(name: String, clazz: Class<*>) {
		if (!classes.containsKey(name)) {
			classes[name] = clazz
		}
	}

	private fun removeClass(name: String) {
		val clazz = classes.remove(name)!!
	}

}