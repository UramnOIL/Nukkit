package cn.nukkit.utils

import cn.nukkit.Server
import cn.nukkit.plugin.Plugin
import java.io.File
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType

/**
 * SimpleConfig for Nukkit
 * added 11/02/2016 by fromgate
 */
abstract class SimpleConfig(private val configFile: File) {

	@JvmOverloads
	constructor(plugin: Plugin, fileName: String = "config.yml") : this(File(plugin.dataFolder.toString() + File.separator + fileName)) {
	}

	@JvmOverloads
	fun save(async: Boolean = false): Boolean {
		if (configFile.exists()) try {
			configFile.createNewFile()
		} catch (e: Exception) {
			return false
		}
		val cfg = Config(configFile, Config.YAML)
		for (field in this.javaClass.declaredFields) {
			if (skipSave(field)) continue
			val path = getPath(field)
			try {
				if (path != null) cfg[path] = field[this]
			} catch (e: Exception) {
				return false
			}
		}
		cfg.save(async)
		return true
	}

	fun load(): Boolean {
		if (!configFile.exists()) return false
		val cfg = Config(configFile, Config.YAML)
		for (field in this.javaClass.declaredFields) {
			if (field.name == "configFile") continue
			if (skipSave(field)) continue
			val path = getPath(field) ?: continue
			if (path.isEmpty()) continue
			field.isAccessible = true
			try {
				if (field.type == Int::class.javaPrimitiveType || field.type == Int::class.java) field[this] = cfg.getInt(path, field.getInt(this)) else if (field.type == Boolean::class.javaPrimitiveType || field.type == Boolean::class.java) field[this] = cfg.getBoolean(path, field.getBoolean(this)) else if (field.type == Long::class.javaPrimitiveType || field.type == Long::class.java) field[this] = cfg.getLong(path, field.getLong(this)) else if (field.type == Double::class.javaPrimitiveType || field.type == Double::class.java) field[this] = cfg.getDouble(path, field.getDouble(this)) else if (field.type == String::class.java) field[this] = cfg.getString(path, (field[this] as String)) else if (field.type == ConfigSection::class.java) field[this] = cfg.getSection(path) else if (field.type == MutableList::class.java) {
					val genericFieldType = field.genericType
					if (genericFieldType is ParameterizedType) {
						val fieldArgClass = genericFieldType.actualTypeArguments[0] as Class<*>
						if (fieldArgClass == Int::class.java) field[this] = cfg.getIntegerList(path) else if (fieldArgClass == Boolean::class.java) field[this] = cfg.getBooleanList(path) else if (fieldArgClass == Double::class.java) field[this] = cfg.getDoubleList(path) else if (fieldArgClass == Char::class.java) field[this] = cfg.getCharacterList(path) else if (fieldArgClass == Byte::class.java) field[this] = cfg.getByteList(path) else if (fieldArgClass == Float::class.java) field[this] = cfg.getFloatList(path) else if (fieldArgClass == Short::class.java) field[this] = cfg.getFloatList(path) else if (fieldArgClass == String::class.java) field[this] = cfg.getStringList(path)
					} else field[this] = cfg.getList(path) // Hell knows what's kind of List was found :)
				} else throw IllegalStateException("SimpleConfig did not supports class: " + field.type.name + " for config field " + configFile.name)
			} catch (e: Exception) {
				Server.instance.logger.logException(e)
				return false
			}
		}
		return true
	}

	private fun getPath(field: Field): String? {
		var path: String? = null
		if (field.isAnnotationPresent(Path::class.java)) {
			val pathDefine = field.getAnnotation(Path::class.java)
			path = pathDefine.value()
		}
		if (path == null || path.isEmpty()) path = field.name.replace("_".toRegex(), ".")
		if (Modifier.isFinal(field.modifiers)) return null
		if (Modifier.isPrivate(field.modifiers)) field.isAccessible = true
		return path
	}

	private fun skipSave(field: Field): Boolean {
		return if (!field.isAnnotationPresent(Skip::class.java)) false else field.getAnnotation(Skip::class.java).skipSave()
	}

	private fun skipLoad(field: Field): Boolean {
		return if (!field.isAnnotationPresent(Skip::class.java)) false else field.getAnnotation(Skip::class.java).skipLoad()
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(AnnotationTarget.FIELD)
	annotation class Path(val value: String = "")

	@Retention(RetentionPolicy.RUNTIME)
	@Target(AnnotationTarget.FIELD)
	annotation class Skip(val skipSave: Boolean = true, val skipLoad: Boolean = true)

	init {
		configFile.parentFile.mkdirs()
	}
}