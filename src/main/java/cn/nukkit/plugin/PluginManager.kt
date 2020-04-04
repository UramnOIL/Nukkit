package cn.nukkit.plugin

import cn.nukkit.Server
import cn.nukkit.command.PluginCommand
import cn.nukkit.command.SimpleCommandMap
import cn.nukkit.event.*
import cn.nukkit.permission.Permissible
import cn.nukkit.permission.Permission
import cn.nukkit.utils.PluginException
import cn.nukkit.utils.Utils
import co.aikar.timings.Timings
import java.io.File
import java.lang.Class
import java.lang.Deprecated
import java.lang.Exception
import java.lang.IllegalAccessException
import java.lang.IllegalArgumentException
import java.lang.Integer
import java.lang.NoClassDefFoundError
import java.lang.NoSuchMethodException
import java.lang.NullPointerException
import java.lang.RuntimeException
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.*
import java.util.regex.Pattern
import kotlin.Boolean
import kotlin.String
import kotlin.Throwable
import kotlin.also
import kotlin.arrayOf
import kotlin.collections.HashMap
import kotlin.require

/**
 * @author MagicDroidX
 */
open class PluginManager(private val server: Server, private val commandMap: SimpleCommandMap) {
	private val mutablePlugins: MutableMap<String, Plugin> = LinkedHashMap()
	val plugins
		get() = mutablePlugins
	protected val permissions: MutableMap<String, Permission> = HashMap()
	protected val defaultPerms: MutableMap<String, Permission> = HashMap()
	protected val defaultPermsOp: MutableMap<String, Permission> = HashMap()
	protected val permSubs: MutableMap<String, MutableSet<Permissible>> = HashMap()
	protected val defSubs = Collections.newSetFromMap(WeakHashMap<Permissible, Boolean>())
	protected val defSubsOp = Collections.newSetFromMap(WeakHashMap<Permissible, Boolean>())
	protected val fileAssociations: MutableMap<String, PluginLoader> = HashMap()

	fun registerInterface(loaderClass: Class<out PluginLoader?>?): Boolean {
		return if (loaderClass != null) {
			try {
				val constructor: Constructor<*> = loaderClass.getDeclaredConstructor(Server::class.java)
				constructor.isAccessible = true
				fileAssociations[loaderClass.name] = constructor.newInstance(server) as PluginLoader
				true
			} catch (e: Exception) {
				false
			}
		} else false
	}

	fun loadPlugin(path: String, loaders: Map<String, PluginLoader>? = null) = this.loadPlugin(File(path), loaders)

	fun loadPlugin(file: File, loaders: Map<String, PluginLoader>? = null): Plugin? {
		(loaders ?: fileAssociations).values.forEach { loader ->
			loader.pluginFilters.forEach filters@ { pattern ->
				if (!pattern.matcher(file.name).matches()) {
					return@filters
				}
				val description = loader.getPluginDescription(file) ?: return@filters
				try {
					val plugin = loader.loadPlugin(file) ?: return@filters
					mutablePlugins[plugin.description.name] = plugin
					val pluginCommands = parseYamlCommands(plugin)
					if (pluginCommands.isNotEmpty()) {
						commandMap.registerAll(plugin.description.name, pluginCommands)
					}
					return plugin
				} catch (e: Exception) {
					Server.instance.logger.critical("Could not load plugin", e)
					return null
				}
			}
		}
		return null
	}

	fun loadPlugins(dictionary: String): Map<String, Plugin> {
		return this.loadPlugins(File(dictionary))
	}

	fun loadPlugins(dictionary: String, newLoaders: List<String>): Map<String, Plugin> {
		return this.loadPlugins(File(dictionary), newLoaders)
	}

	@JvmOverloads
	fun loadPlugins(dictionary: File, newLoaders: List<String>? = null, includeDir: Boolean = false): Map<String, Plugin> {
		if (dictionary.isDirectory) {
			return HashMap()
		}
		val plugins: MutableMap<String, File> = LinkedHashMap()
		val loadedPlugins: MutableMap<String, Plugin> = LinkedHashMap()
		val dependencies: MutableMap<String, MutableList<String>> = LinkedHashMap()
		val softDependencies: MutableMap<String, MutableList<String>> = LinkedHashMap()
		var loaders: MutableMap<String, PluginLoader> = LinkedHashMap()
		if (newLoaders != null) {
			for (loader in newLoaders) {
				if (fileAssociations.containsKey(loader)) {
					loaders[loader] = fileAssociations[loader]!!
				}
			}
		} else {
			loaders = fileAssociations
		}
		for(loader in loaders.values) {
			val filteredFiles = dictionary.listFiles { _, name ->
				for (pattern in loader.pluginFilters) {
					if (pattern.matcher(name).matches()) {
						return@listFiles true
					}
				}
				false
			}!!
			for (file in filteredFiles) {
				if (file.isDirectory && !includeDir) {
					continue
				}
				try {
					val description = loader.getPluginDescription(file) ?: continue
					val name = description.name
					if (plugins.containsKey(name) || plugins[name] != null) {
						server.logger.error(server.language.translateString("nukkit.plugin.duplicateError", name))
						continue
					}
					var compatible = false
					for (version in description.apis) {
						try {
							//Check the format: majorVersion.minorVersion.patch
							require(Pattern.matches("[0-9]\\.[0-9]\\.[0-9]", version))
						} catch (e: NullPointerException) {
							server.logger.error(server.language.translateString("nukkit.plugin.loadError", arrayOf(name, "Wrong API format")))
							continue
						} catch (e: IllegalArgumentException) {
							server.logger.error(server.language.translateString("nukkit.plugin.loadError", arrayOf(name, "Wrong API format")))
							continue
						}
						val versionArray = version.split("\\.").toTypedArray()
						val apiVersion = server.apiVersion.split("\\.").toTypedArray()

						//Completely different API version
						if (Integer.valueOf(versionArray[0]) != Integer.valueOf(apiVersion[0])) {
							continue
						}

						//If the plugin requires new API features, being backwards compatible
						if (Integer.valueOf(versionArray[1]) > Integer.valueOf(apiVersion[1])) {
							continue
						}

						compatible = true
						break
					}

					if (!compatible) {
						server.logger.error(server.language.translateString("nukkit.plugin.loadError", arrayOf(name, "%nukkit.plugin.incompatibleAPI")))
					}

					plugins[name] = file
					softDependencies[name] = description.softDepend
					dependencies[name] = description.depend
					for (before in description.loadBefore) {
						if (softDependencies.containsKey(before)) {
							softDependencies[before]!!.add(name)
						} else {
							val list = mutableListOf<String>()
							list.add(name)
							softDependencies[before] = list
						}
					}
				} catch (e: Exception) {
					server.logger.error(server.language.translateString("nukkit.plugin" +
							".fileError", file.name, dictionary.toString(), Utils
							.getExceptionMessage(e)))
					val logger = server.logger
					logger.logException(e)
				}
			}
		}
		while (!plugins.isEmpty()) {
			var missingDependency = true
			for ((name, file) in plugins) {
				if (dependencies.containsKey(name)) {
					for (dependency in dependencies[name]!!) {
						if (loadedPlugins.containsKey(dependency) || plugins[dependency] != null) {
							dependencies[name]!!.remove(dependency)
						} else if (!plugins.containsKey(dependency)) {
							server.logger.critical(server.language.translateString("nukkit" +
									".plugin.loadError", arrayOf(name, "%nukkit.plugin.unknownDependency")))
							break
						}
					}
					if (dependencies[name]!!.isEmpty()) {
						dependencies.remove(name)
					}
				}
				if (softDependencies.containsKey(name)) {
					for (dependency in softDependencies[name]!!) {
						if (loadedPlugins.containsKey(dependency) || plugins[dependency] != null) {
							softDependencies[name]!!.remove(dependency)
						}
					}
					if (softDependencies[name]!!.isEmpty()) {
						softDependencies.remove(name)
					}
				}
				if (!dependencies.containsKey(name) && !softDependencies.containsKey(name)) {
					plugins.remove(name)
					missingDependency = false
					val plugin = this.loadPlugin(file, loaders)
					if (plugin != null) {
						loadedPlugins[name] = plugin
					} else {
						server.logger.critical(server.language.translateString("nukkit.plugin.genericLoadError", name))
					}
				}
			}
			if (missingDependency) {
				for ((name, file) in plugins) {
					if (!dependencies.containsKey(name)) {
						softDependencies.remove(name)
						plugins.remove(name)
						missingDependency = false
						val plugin = this.loadPlugin(file, loaders)
						if (plugin != null) {
							loadedPlugins[name] = plugin
						} else {
							server.logger.critical(server.language.translateString("nukkit.plugin.genericLoadError", name))
						}
					}
				}
				if (missingDependency) {
					for (name in plugins.keys) {
						server.logger.critical(server.language.translateString("nukkit.plugin.loadError", arrayOf(name, "%nukkit.plugin.circularDependency")))
					}
					plugins.clear()
				}
			}
		}

		return loadedPlugins
	}

	fun getPermission(name: String): Permission? {
		return if (permissions.containsKey(name)) {
			permissions[name]
		} else null
	}

	fun addPermission(permission: Permission?): Boolean {
		if (!permissions.containsKey(permission!!.name)) {
			permissions[permission.name] = permission
			calculatePermissionDefault(permission)
			return true
		}
		return false
	}

	fun removePermission(name: String) {
		permissions.remove(name)
	}

	fun removePermission(permission: Permission?) {
		this.removePermission(permission!!.name)
	}

	fun getDefaultPermissions(op: Boolean): Map<String, Permission> {
		return if (op) {
			defaultPermsOp
		} else {
			defaultPerms
		}
	}

	fun recalculatePermissionDefaults(permission: Permission) {
		if (permissions.containsKey(permission.name)) {
			defaultPermsOp.remove(permission.name)
			defaultPerms.remove(permission.name)
			calculatePermissionDefault(permission)
		}
	}

	private fun calculatePermissionDefault(permission: Permission?) {
		Timings.permissionDefaultTimer.startTiming()
		if (permission!!.default == Permission.DEFAULT_OP || permission.default == Permission.DEFAULT_TRUE) {
			defaultPermsOp[permission.name] = permission
			dirtyPermissibles(true)
		}
		if (permission.default == Permission.DEFAULT_NOT_OP || permission.default == Permission.DEFAULT_TRUE) {
			defaultPerms[permission.name] = permission
			dirtyPermissibles(false)
		}
		Timings.permissionDefaultTimer.startTiming()
	}

	private fun dirtyPermissibles(op: Boolean) {
		for (p in getDefaultPermSubscriptions(op)) {
			p.recalculatePermissions()
		}
	}

	fun subscribeToPermission(permission: String, permissible: Permissible) {
		if (!permSubs.containsKey(permission)) {
			permSubs[permission] = Collections.newSetFromMap(WeakHashMap())
		}
		permSubs[permission]!!.add(permissible)
	}

	fun unsubscribeFromPermission(permission: String, permissible: Permissible) {
		if (permSubs.containsKey(permission)) {
			permSubs[permission]!!.remove(permissible)
			if (permSubs[permission]!!.size == 0) {
				permSubs.remove(permission)
			}
		}
	}

	fun getPermissionSubscriptions(permission: String): Set<Permissible> {
		return permSubs[permission] ?: HashSet()
	}

	fun subscribeToDefaultPerms(op: Boolean, permissible: Permissible) {
		if (op) {
			defSubsOp.add(permissible)
		} else {
			defSubs.add(permissible)
		}
	}

	fun unsubscribeFromDefaultPerms(op: Boolean, permissible: Permissible) {
		if (op) {
			defSubsOp.remove(permissible)
		} else {
			defSubs.remove(permissible)
		}
	}

	fun getDefaultPermSubscriptions(op: Boolean): Set<Permissible> {
		return if (op) {
			HashSet(defSubsOp)
		} else {
			HashSet(defSubs)
		}
	}

	fun isPluginEnabled(plugin: Plugin): Boolean {
		return if (mutablePlugins.containsKey(plugin.description.name)) {
			plugin.isEnabled
		} else {
			false
		}
	}

	fun enablePlugin(plugin: Plugin) {
		if (!plugin.isEnabled) {
			try {
				plugin.description.permissions.forEach {
					addPermission(it)
				}
				plugin.pluginLoader.enablePlugin(plugin)
			} catch (e: Throwable) {
				server.logger.logException(RuntimeException(e))
				disablePlugin(plugin)
			}
		}
	}

	protected fun parseYamlCommands(plugin: Plugin): List<PluginCommand<*>> {
		val pluginCmds: MutableList<PluginCommand<*>> = ArrayList()
		for ((key1, value) in plugin.description.commands) {
			val key = key1 as String
			if (key.contains(":")) {
				server.logger.critical(server.language.translateString("nukkit.plugin.commandError", arrayOf(key, plugin.description.fullName)))
				continue
			}
			if (value is Map<*, *>) {
				val newCmd: PluginCommand<*> = PluginCommand(key, plugin)
				if (value.containsKey("description")) {
					newCmd.description = (value["description"] as String?)!!
				}
				if (value.containsKey("usage")) {
					newCmd.usage = (value["usage"] as String?)!!
				}
				if (value.containsKey("aliases")) {
					val aliases = value["aliases"]
					if (aliases is List<*>) {
						val aliasList: MutableList<String?> = ArrayList()
						for (alias in aliases as List<String>) {
							if (alias.contains(":")) {
								server.logger.critical(server.language.translateString("nukkit.plugin.aliasError", arrayOf(alias, plugin.description!!.fullName)))
								continue
							}
							aliasList.add(alias)
						}
						newCmd.setAliases(aliasList.toTypedArray())
					}
				}
				if (value.containsKey("permission")) {
					newCmd.permission = value["permission"] as String?
				}
				if (value.containsKey("permission-message")) {
					newCmd.permissionMessage = value["permission-message"] as String?
				}
				pluginCmds.add(newCmd)
			}
		}
		return pluginCmds
	}

	fun disablePlugins() {
		val plugins: ListIterator<Plugin> = ArrayList(mutablePlugins.values).listIterator(mutablePlugins.size)
		while (plugins.hasPrevious()) {
			disablePlugin(plugins.previous())
		}
	}

	fun disablePlugin(plugin: Plugin) {
		if (plugin.isEnabled) {
			try {
				plugin.pluginLoader.disablePlugin(plugin)
			} catch (e: Exception) {
				val logger = server.logger
				logger.logException(e)
			}
			server.scheduler.cancelTask(plugin)
			HandlerList.unregisterAll(plugin)
			plugin.description.permissions.forEach {
				this.removePermission(it)
			}
		}
	}

	fun clearPlugins() {
		disablePlugins()
		mutablePlugins.clear()
		fileAssociations.clear()
		permissions.clear()
		defaultPerms.clear()
		defaultPermsOp.clear()
	}

	fun callEvent(event: Event) {
		try {
			for (registration in getEventListeners(event.javaClass).registeredListeners) {
				if (!registration.plugin.isEnabled) {
					continue
				}
				try {
					registration.callEvent(event)
				} catch (e: Exception) {
					server.logger.critical(server.language.translateString("nukkit.plugin.eventError", event.getEventName(), registration.plugin.description.fullName, e.message, registration.listener.javaClass.name))
					server.logger.logException(e)
				}
			}
		} catch (e: IllegalAccessException) {
			server.logger.logException(e)
		}
	}

	fun registerEvents(listener: Listener, plugin: Plugin) {
		if (!plugin.isEnabled) {
			throw PluginException("Plugin attempted to register " + listener.javaClass.name + " while not enabled")
		}
		val ret: Map<Class<out Event>, Set<RegisteredListener>> = HashMap()
		val methods: Set<Method>
		try {
			val publicMethods = listener.javaClass.methods
			val privateMethods = listener.javaClass.declaredMethods
			methods = HashSet(publicMethods.size + privateMethods.size, 1.0f)
			Collections.addAll(methods, *publicMethods)
			Collections.addAll(methods, *privateMethods)
		} catch (e: NoClassDefFoundError) {
			plugin.logger.error("Plugin " + plugin.description.fullName + " has failed to register events for " + listener.javaClass + " because " + e.message + " does not exist.")
			return
		}
		for (method in methods) {
			val eh = method.getAnnotation(EventHandler::class.java) ?: continue
			if (method.isBridge || method.isSynthetic) {
				continue
			}
			var checkClass: Class<*>
			if (method.parameterTypes.size != 1 || !Event::class.java.isAssignableFrom(method.parameterTypes[0].also { checkClass = it })) {
				plugin.logger!!.error(plugin.description.fullName + " attempted to register an invalid EventHandler method signature \"" + method.toGenericString() + "\" in " + listener.javaClass)
				continue
			}
			val eventClass = checkClass.asSubclass(Event::class.java)
			method.isAccessible = true
			var clazz: Class<*> = eventClass
			while (Event::class.java.isAssignableFrom(clazz)) {

				// This loop checks for extending deprecated events
				if (clazz.getAnnotation(Deprecated::class.java) != null) {
					if (java.lang.Boolean.valueOf(server.config.toString())) {
						server.logger.warning(server.language.translateString("nukkit.plugin.deprecatedEvent", plugin.name, clazz.name, listener.javaClass.name + "." + method.name + "()"))
					}
					break
				}
				clazz = clazz.superclass
			}
			registerEvent(eventClass, listener, eh.priority, MethodEventExecutor(method), plugin, eh.ignoreCancelled)
		}
	}

	@JvmOverloads
	@Throws(PluginException::class)
	inline fun <reified T : Event> registerEvent(listener: Listener, priority: EventPriority?, executor: EventExecutor, plugin: Plugin, ignoreCancelled: Boolean = false) {
		if (!plugin.isEnabled) {
			throw PluginException("Plugin attempted to register $event while not enabled")
		}
		try {
			val timing = Timings.getPluginEventTiming(T::class, listener, executor, plugin)
			getEventListeners(event).register(RegisteredListener(listener, executor, priority, plugin, ignoreCancelled, timing))
		} catch (e: IllegalAccessException) {
			Server.instance!!.logger.logException(e)
		}
	}

	@Throws(IllegalAccessException::class)
	private fun getEventListeners(clazz: KClass<Event>): HandlerList {
		return try {
			val method = getRegistrationClass(T::class.java).getDeclaredMethod("getHandlers")
			method.isAccessible = true
			method.invoke(null) as HandlerList
		} catch (e: NullPointerException) {
			throw IllegalArgumentException("getHandlers method in ${T::class.simpleName} was not static!")
		} catch (e: Exception) {
			throw IllegalAccessException(Utils.getExceptionMessage(e))
		}
	}

	private fun getRegistrationClass(clazz: KClass<out Event>): Class<out Event?> {
		return try {
			clazz.getDeclaredMethod("getHandlers")
			clazz
		} catch (e: NoSuchMethodException) {
			if (clazz.superclass != null && clazz.superclass != Event::class.java
					&& Event::class.java.isAssignableFrom(clazz.superclass)) {
				getRegistrationClass(clazz.superclass.asSubclass(Event::class.java))
			} else {
				throw IllegalAccessException("Unable to find handler list for event " + clazz.name + ". Static getHandlers method required!")
			}
		}
	}

}