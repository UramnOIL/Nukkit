package cn.nukkit.utils

import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer

/**
 * Created by fromgate on 26.04.2016.
 */
open class ConfigSection
/**
 * Empty ConfigSection constructor
 */
() : LinkedHashMap<String?, Any?>() {
	/**
	 * Constructor of ConfigSection that contains initial key/value data
	 *
	 * @param key
	 * @param value
	 */
	constructor(key: String, value: Any?) : this() {
		this[key] = value
	}

	/**
	 * Constructor of ConfigSection, based on values stored in map.
	 *
	 * @param map
	 */
	constructor(map: LinkedHashMap<String?, Any>?) : this() {
		if (map == null || map.isEmpty()) return
		for ((key, value) in map) {
			if (value is LinkedHashMap<*, *>) {
				super.put(key, ConfigSection(value))
			} else if (value is List<*>) {
				super.put(key, parseList(value))
			} else {
				super.put(key, value)
			}
		}
	}

	private fun parseList(list: List<*>): List<*> {
		val newList: MutableList<Any> = ArrayList()
		for (o in list) {
			if (o is LinkedHashMap<*, *>) {
				newList.add(ConfigSection(o))
			} else {
				newList.add(o)
			}
		}
		return newList
	}

	/**
	 * Get root section as LinkedHashMap
	 *
	 * @return
	 */
	val allMap: Map<String, Any>
		get() = LinkedHashMap(this)

	/**
	 * Get new instance of config section
	 *
	 * @return
	 */
	val all: ConfigSection
		get() = ConfigSection(this)

	/**
	 * Get object by key. If section does not contain value, return null
	 */
	override fun get(key: String?): Any? {
		return this.get<Any?>(key, null)
	}

	/**
	 * Get object by key. If section does not contain value, return default value
	 *
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	operator fun <T> get(key: String?, defaultValue: T): T? {
		if (key == null || key.isEmpty()) return defaultValue
		if (super.containsKey(key)) return super.get(key) as T?
		val keys = key.split("\\.", 2.toBoolean()).toTypedArray()
		if (!super.containsKey(keys[0])) return defaultValue
		val value = super.get(keys[0])
		if (value is ConfigSection) {
			return value.get(keys[1], defaultValue)
		}
		return defaultValue
	}

	/**
	 * Store value into config section
	 *
	 * @param key
	 * @param value
	 */
	operator fun set(key: String, value: Any?) {
		val subKeys = key.split("\\.", 2.toBoolean()).toTypedArray()
		if (subKeys.size > 1) {
			var childSection: ConfigSection? = ConfigSection()
			if (this.containsKey(subKeys[0]) && super.get(subKeys[0]) is ConfigSection) childSection = super.get(subKeys[0]) as ConfigSection?
			childSection!![subKeys[1]] = value
			super.put(subKeys[0], childSection)
		} else super.put(subKeys[0], value)
	}

	/**
	 * Check type of section element defined by key. Return true this element is ConfigSection
	 *
	 * @param key
	 * @return
	 */
	fun isSection(key: String?): Boolean {
		val value = this[key]
		return value is ConfigSection
	}

	/**
	 * Get config section element defined by key
	 *
	 * @param key
	 * @return
	 */
	fun getSection(key: String?): ConfigSection {
		return this.get(key, ConfigSection())!!
	}
	//@formatter:off
	/**
	 * Get all ConfigSections in root path.
	 * Example config:
	 * a1:
	 * b1:
	 * c1:
	 * c2:
	 * a2:
	 * b2:
	 * c3:
	 * c4:
	 * a3: true
	 * a4: "hello"
	 * a5: 100
	 *
	 *
	 * getSections() will return new ConfigSection, that contains sections a1 and a2 only.
	 *
	 * @return
	 */
	//@formatter:on
	val sections: ConfigSection
		get() = getSections(null)

	/**
	 * Get sections (and only sections) from provided path
	 *
	 * @param key - config section path, if null or empty root path will used.
	 * @return
	 */
	fun getSections(key: String?): ConfigSection {
		val sections = ConfigSection()
		val parent = (if (key == null || key.isEmpty()) all else getSection(key)) ?: return sections
		parent.forEach(BiConsumer { key1: String, value: Any? -> if (value is ConfigSection) sections[key1] = value })
		return sections
	}

	/**
	 * Get int value of config section element
	 *
	 * @param key - key (inside) current section (default value equals to 0)
	 * @return
	 */
	fun getInt(key: String?): Int {
		return this.getInt(key, 0)
	}

	/**
	 * Get int value of config section element
	 *
	 * @param key          - key (inside) current section
	 * @param defaultValue - default value that will returned if section element is not exists
	 * @return
	 */
	fun getInt(key: String?, defaultValue: Int): Int {
		return this.get(key, defaultValue as Number).intValue()
	}

	/**
	 * Check type of section element defined by key. Return true this element is Integer
	 *
	 * @param key
	 * @return
	 */
	fun isInt(key: String?): Boolean {
		val `val` = get(key)
		return `val` is Int
	}

	/**
	 * Get long value of config section element
	 *
	 * @param key - key (inside) current section
	 * @return
	 */
	fun getLong(key: String?): Long {
		return this.getLong(key, 0)
	}

	/**
	 * Get long value of config section element
	 *
	 * @param key          - key (inside) current section
	 * @param defaultValue - default value that will returned if section element is not exists
	 * @return
	 */
	fun getLong(key: String?, defaultValue: Long): Long {
		return this.get(key, defaultValue as Number).longValue()
	}

	/**
	 * Check type of section element defined by key. Return true this element is Long
	 *
	 * @param key
	 * @return
	 */
	fun isLong(key: String?): Boolean {
		val `val` = get(key)
		return `val` is Long
	}

	/**
	 * Get double value of config section element
	 *
	 * @param key - key (inside) current section
	 * @return
	 */
	fun getDouble(key: String?): Double {
		return this.getDouble(key, 0.0)
	}

	/**
	 * Get double value of config section element
	 *
	 * @param key          - key (inside) current section
	 * @param defaultValue - default value that will returned if section element is not exists
	 * @return
	 */
	fun getDouble(key: String?, defaultValue: Double): Double {
		return this.get(key, defaultValue as Number).doubleValue()
	}

	/**
	 * Check type of section element defined by key. Return true this element is Double
	 *
	 * @param key
	 * @return
	 */
	fun isDouble(key: String?): Boolean {
		val `val` = get(key)
		return `val` is Double
	}

	/**
	 * Get String value of config section element
	 *
	 * @param key - key (inside) current section
	 * @return
	 */
	fun getString(key: String?): String {
		return this.getString(key, "")
	}

	/**
	 * Get String value of config section element
	 *
	 * @param key          - key (inside) current section
	 * @param defaultValue - default value that will returned if section element is not exists
	 * @return
	 */
	fun getString(key: String?, defaultValue: String): String {
		val result: Any = this.get(key, defaultValue)!!
		return result.toString()
	}

	/**
	 * Check type of section element defined by key. Return true this element is String
	 *
	 * @param key
	 * @return
	 */
	fun isString(key: String?): Boolean {
		val `val` = get(key)
		return `val` is String
	}

	/**
	 * Get boolean value of config section element
	 *
	 * @param key - key (inside) current section
	 * @return
	 */
	fun getBoolean(key: String?): Boolean {
		return this.getBoolean(key, false)
	}

	/**
	 * Get boolean value of config section element
	 *
	 * @param key          - key (inside) current section
	 * @param defaultValue - default value that will returned if section element is not exists
	 * @return
	 */
	fun getBoolean(key: String?, defaultValue: Boolean): Boolean {
		return this.get(key, defaultValue)!!
	}

	/**
	 * Check type of section element defined by key. Return true this element is Integer
	 *
	 * @param key
	 * @return
	 */
	fun isBoolean(key: String?): Boolean {
		val `val` = get(key)
		return `val` is Boolean
	}

	/**
	 * Get List value of config section element
	 *
	 * @param key - key (inside) current section
	 * @return
	 */
	fun getList(key: String?): List<*>? {
		return this.getList(key, null)
	}

	/**
	 * Get List value of config section element
	 *
	 * @param key         - key (inside) current section
	 * @param defaultList - default value that will returned if section element is not exists
	 * @return
	 */
	fun getList(key: String?, defaultList: List<*>?): List<*>? {
		return this.get(key, defaultList)
	}

	/**
	 * Check type of section element defined by key. Return true this element is List
	 *
	 * @param key
	 * @return
	 */
	fun isList(key: String?): Boolean {
		val `val` = get(key)
		return `val` is List<*>
	}

	/**
	 * Get String List value of config section element
	 *
	 * @param key - key (inside) current section
	 * @return
	 */
	fun getStringList(key: String?): List<String> {
		val value = this.getList(key) ?: return ArrayList(0)
		val result: MutableList<String> = ArrayList()
		for (o in value) {
			if (o is String || o is Number || o is Boolean || o is Char) {
				result.add(o.toString())
			}
		}
		return result
	}

	/**
	 * Get Integer List value of config section element
	 *
	 * @param key - key (inside) current section
	 * @return
	 */
	fun getIntegerList(key: String?): List<Int> {
		val list = getList(key) ?: return ArrayList(0)
		val result: MutableList<Int> = ArrayList()
		for (`object` in list) {
			if (`object` is Int) {
				result.add(`object`)
			} else if (`object` is String) {
				try {
					result.add(Integer.valueOf(`object` as String?))
				} catch (ex: Exception) {
					//ignore
				}
			} else if (`object` is Char) {
				result.add(`object`.toInt())
			} else if (`object` is Number) {
				result.add(`object`.intValue())
			}
		}
		return result
	}

	/**
	 * Get Boolean List value of config section element
	 *
	 * @param key - key (inside) current section
	 * @return
	 */
	fun getBooleanList(key: String?): List<Boolean> {
		val list = getList(key) ?: return ArrayList(0)
		val result: MutableList<Boolean> = ArrayList()
		for (`object` in list) {
			if (`object` is Boolean) {
				result.add(`object`)
			} else if (`object` is String) {
				if (java.lang.Boolean.TRUE.toString() == `object`) {
					result.add(true)
				} else if (java.lang.Boolean.FALSE.toString() == `object`) {
					result.add(false)
				}
			}
		}
		return result
	}

	/**
	 * Get Double List value of config section element
	 *
	 * @param key - key (inside) current section
	 * @return
	 */
	fun getDoubleList(key: String?): List<Double> {
		val list = getList(key) ?: return ArrayList(0)
		val result: MutableList<Double> = ArrayList()
		for (`object` in list) {
			if (`object` is Double) {
				result.add(`object`)
			} else if (`object` is String) {
				try {
					result.add(java.lang.Double.valueOf(`object` as String?))
				} catch (ex: Exception) {
					//ignore
				}
			} else if (`object` is Char) {
				result.add(`object`.toDouble())
			} else if (`object` is Number) {
				result.add(`object`.doubleValue())
			}
		}
		return result
	}

	/**
	 * Get Float List value of config section element
	 *
	 * @param key - key (inside) current section
	 * @return
	 */
	fun getFloatList(key: String?): List<Float> {
		val list = getList(key) ?: return ArrayList(0)
		val result: MutableList<Float> = ArrayList()
		for (`object` in list) {
			if (`object` is Float) {
				result.add(`object`)
			} else if (`object` is String) {
				try {
					result.add(java.lang.Float.valueOf(`object` as String?))
				} catch (ex: Exception) {
					//ignore
				}
			} else if (`object` is Char) {
				result.add(`object`.toFloat())
			} else if (`object` is Number) {
				result.add(`object`.floatValue())
			}
		}
		return result
	}

	/**
	 * Get Long List value of config section element
	 *
	 * @param key - key (inside) current section
	 * @return
	 */
	fun getLongList(key: String?): List<Long> {
		val list = getList(key) ?: return ArrayList(0)
		val result: MutableList<Long> = ArrayList()
		for (`object` in list) {
			if (`object` is Long) {
				result.add(`object`)
			} else if (`object` is String) {
				try {
					result.add(java.lang.Long.valueOf(`object` as String?))
				} catch (ex: Exception) {
					//ignore
				}
			} else if (`object` is Char) {
				result.add(`object`.toLong())
			} else if (`object` is Number) {
				result.add(`object`.longValue())
			}
		}
		return result
	}

	/**
	 * Get Byte List value of config section element
	 *
	 * @param key - key (inside) current section
	 * @return
	 */
	fun getByteList(key: String?): List<Byte> {
		val list = getList(key) ?: return ArrayList(0)
		val result: MutableList<Byte> = ArrayList()
		for (`object` in list) {
			if (`object` is Byte) {
				result.add(`object`)
			} else if (`object` is String) {
				try {
					result.add(java.lang.Byte.valueOf(`object` as String?))
				} catch (ex: Exception) {
					//ignore
				}
			} else if (`object` is Char) {
				result.add(`object`.toChar().toByte())
			} else if (`object` is Number) {
				result.add(`object`.byteValue())
			}
		}
		return result
	}

	/**
	 * Get Character List value of config section element
	 *
	 * @param key - key (inside) current section
	 * @return
	 */
	fun getCharacterList(key: String?): List<Char> {
		val list = getList(key) ?: return ArrayList(0)
		val result: MutableList<Char> = ArrayList()
		for (`object` in list) {
			if (`object` is Char) {
				result.add(`object`)
			} else if (`object` is String) {
				val str = `object`
				if (str.length == 1) {
					result.add(str[0])
				}
			} else if (`object` is Number) {
				result.add(`object`.intValue() as Char)
			}
		}
		return result
	}

	/**
	 * Get Short List value of config section element
	 *
	 * @param key - key (inside) current section
	 * @return
	 */
	fun getShortList(key: String?): List<Short> {
		val list = getList(key) ?: return ArrayList(0)
		val result: MutableList<Short> = ArrayList()
		for (`object` in list) {
			if (`object` is Short) {
				result.add(`object`)
			} else if (`object` is String) {
				try {
					result.add(`object` as String?. toShort ())
				} catch (ex: Exception) {
					//ignore
				}
			} else if (`object` is Char) {
				result.add(`object`.toChar().toShort())
			} else if (`object` is Number) {
				result.add(`object`.shortValue())
			}
		}
		return result
	}

	/**
	 * Get Map List value of config section element
	 *
	 * @param key - key (inside) current section
	 * @return
	 */
	fun getMapList(key: String?): List<Map<*, *>> {
		val list: List<Map<*, *>>? = getList(key)
		val result: MutableList<Map<*, *>> = ArrayList()
		if (list == null) {
			return result
		}
		for (`object` in list) {
			if (`object` is Map<*, *>) {
				result.add(`object`)
			}
		}
		return result
	}
	/**
	 * Check existence of config section element
	 *
	 * @param key
	 * @param ignoreCase
	 * @return
	 */
	/**
	 * Check existence of config section element
	 *
	 * @param key
	 * @return
	 */
	@JvmOverloads
	fun exists(key: String, ignoreCase: Boolean = false): Boolean {
		var key = key
		if (ignoreCase) key = key.toLowerCase()
		for (existKey in getKeys(true)) {
			if (ignoreCase) existKey = existKey.toLowerCase()
			if (existKey == key) return true
		}
		return false
	}

	/**
	 * Remove config section element
	 *
	 * @param key
	 */
	override fun remove(key: String?) {
		if (key == null || key.isEmpty()) return
		if (super.containsKey(key)) super.remove(key) else if (this.containsKey(".")) {
			val keys = key.split("\\.", 2.toBoolean()).toTypedArray()
			if (super.get(keys[0]) is ConfigSection) {
				val section = super.get(keys[0]) as ConfigSection?
				section!!.remove(keys[1])
			}
		}
	}

	/**
	 * Get all keys
	 *
	 * @param child - true = include child keys
	 * @return
	 */
	fun getKeys(child: Boolean): Set<String> {
		val keys: MutableSet<String> = LinkedHashSet()
		this.forEach(BiConsumer { key: String, value: Any? ->
			keys.add(key)
			if (value is ConfigSection) {
				if (child) value.getKeys(true).forEach(Consumer { childKey: String -> keys.add("$key.$childKey") })
			}
		})
		return keys
	}

	/**
	 * Get all keys
	 *
	 * @return
	 */
	override val keys: Set<String>
		get() = getKeys(true)
}