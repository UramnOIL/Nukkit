package cn.nukkit.utils

import cn.nukkit.Server
import cn.nukkit.scheduler.FileWriteTask
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * author: MagicDroidX
 * Nukkit
 */
class Config {
	/**
	 * Get root (main) config section of the Config
	 *
	 * @return
	 */
	//private LinkedHashMap<String, Object> config = new LinkedHashMap<>();
	var rootSection = ConfigSection()
		private set
	private var file: File? = null
	var isCorrect = false
		private set
	private var type = DETECT

	companion object {
		const val DETECT = -1 //Detect by file extension
		const val PROPERTIES = 0 // .properties
		const val CNF = PROPERTIES // .cnf
		const val JSON = 1 // .js, .json
		const val YAML = 2 // .yml, .yaml

		//public static final int EXPORT = 3; // .export, .xport
		//public static final int SERIALIZED = 4; // .sl
		const val ENUM = 5 // .txt, .list, .enum
		const val ENUMERATION = ENUM
		val format: MutableMap<String, Int> = TreeMap()

		init {
			format["properties"] = PROPERTIES
			format["con"] = PROPERTIES
			format["conf"] = PROPERTIES
			format["config"] = PROPERTIES
			format["js"] = JSON
			format["json"] = JSON
			format["yml"] = YAML
			format["yaml"] = YAML
			//format.put("sl", Config.SERIALIZED);
			//format.put("serialize", Config.SERIALIZED);
			format["txt"] = ENUM
			format["list"] = ENUM
			format["enum"] = ENUM
		}
	}
	/**
	 * Constructor for Config instance with undefined file object
	 *
	 * @param type - Config type
	 */
	/**
	 * Constructor for Config (YAML) instance with undefined file object
	 */
	@JvmOverloads
	constructor(type: Int = YAML) {
		this.type = type
		isCorrect = true
		rootSection = ConfigSection()
	}

	constructor(file: File) : this(file.toString(), DETECT) {}
	constructor(file: File, type: Int) : this(file.toString(), type, ConfigSection()) {}

	@Deprecated("")
	constructor(file: String?, type: Int, defaultMap: LinkedHashMap<String?, Any>?) {
		this.load(file, type, ConfigSection(defaultMap))
	}

	@JvmOverloads
	constructor(file: String?, type: Int = DETECT, defaultMap: ConfigSection = ConfigSection()) {
		this.load(file, type, defaultMap)
	}

	constructor(file: File, type: Int, defaultMap: ConfigSection) {
		this.load(file.toString(), type, defaultMap)
	}

	@Deprecated("")
	constructor(file: File, type: Int, defaultMap: LinkedHashMap<String?, Any>?) : this(file.toString(), type, ConfigSection(defaultMap)) {
	}

	fun reload() {
		rootSection.clear()
		isCorrect = false
		//this.load(this.file.toString());
		checkNotNull(file) { "Failed to reload Config. File object is undefined." }
		this.load(file.toString(), type)
	}

	@JvmOverloads
	fun load(file: String?, type: Int = DETECT, defaultMap: ConfigSection = ConfigSection()): Boolean {
		isCorrect = true
		this.type = type
		this.file = File(file)
		if (!this.file!!.exists()) {
			try {
				this.file!!.parentFile.mkdirs()
				this.file!!.createNewFile()
			} catch (e: IOException) {
				MainLogger.getLogger().error("Could not create Config " + this.file.toString(), e)
			}
			rootSection = defaultMap
			this.save()
		} else {
			if (this.type == DETECT) {
				var extension = ""
				if (this.file!!.name.lastIndexOf(".") != -1 && this.file!!.name.lastIndexOf(".") != 0) {
					extension = this.file!!.name.substring(this.file!!.name.lastIndexOf(".") + 1)
				}
				if (format.containsKey(extension)) {
					this.type = format[extension]!!
				} else {
					isCorrect = false
				}
			}
			if (isCorrect) {
				var content = ""
				try {
					content = Utils.readFile(this.file)
				} catch (e: IOException) {
					Server.instance.logger.logException(e)
				}
				parseContent(content)
				if (!isCorrect) return false
				if (this.setDefault(defaultMap) > 0) {
					this.save()
				}
			} else {
				return false
			}
		}
		return true
	}

	fun load(inputStream: InputStream?): Boolean {
		if (inputStream == null) return false
		if (isCorrect) {
			val content: String
			content = try {
				Utils.readFile(inputStream)
			} catch (e: IOException) {
				Server.instance.logger.logException(e)
				return false
			}
			parseContent(content)
		}
		return isCorrect
	}

	fun check(): Boolean {
		return isCorrect
	}

	/**
	 * Save configuration into provided file. Internal file object will be set to new file.
	 *
	 * @param file
	 * @param async
	 * @return
	 */
	fun save(file: File?, async: Boolean): Boolean {
		this.file = file
		return save(async)
	}

	fun save(file: File?): Boolean {
		this.file = file
		return save()
	}

	@JvmOverloads
	fun save(async: Boolean = false): Boolean {
		checkNotNull(file) { "Failed to save Config. File object is undefined." }
		return if (isCorrect) {
			var content: String? = ""
			when (type) {
				PROPERTIES -> content = writeProperties()
				JSON -> content = GsonBuilder().setPrettyPrinting().create().toJson(rootSection)
				YAML -> {
					val dumperOptions = DumperOptions()
					dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
					val yaml = Yaml(dumperOptions)
					content = yaml.dump(rootSection)
				}
				ENUM -> for (o in rootSection.entries) {
					val entry = o as Map.Entry<*, *>
					content += """
						${entry.key.toString()}

						""".trimIndent()
				}
			}
			if (async) {
				Server.instance.scheduler.scheduleAsyncTask(FileWriteTask(file!!, content!!))
			} else {
				try {
					Utils.writeFile(file, content)
				} catch (e: IOException) {
					Server.instance.logger.logException(e)
				}
			}
			true
		} else {
			false
		}
	}

	operator fun set(key: String, value: Any?) {
		rootSection[key] = value
	}

	operator fun get(key: String?): Any? {
		return this.get<Any?>(key, null)
	}

	operator fun <T> get(key: String?, defaultValue: T): T? {
		return if (isCorrect) rootSection[key, defaultValue] else defaultValue
	}

	fun getSection(key: String?): ConfigSection? {
		return if (isCorrect) rootSection.getSection(key) else ConfigSection()
	}

	fun isSection(key: String?): Boolean {
		return rootSection.isSection(key)
	}

	fun getSections(key: String?): ConfigSection? {
		return if (isCorrect) rootSection.getSections(key) else ConfigSection()
	}

	val sections: ConfigSection?
		get() = if (isCorrect) rootSection.sections else ConfigSection()

	fun getInt(key: String?): Int {
		return this.getInt(key, 0)
	}

	fun getInt(key: String?, defaultValue: Int): Int {
		return if (isCorrect) rootSection.getInt(key, defaultValue) else defaultValue
	}

	fun isInt(key: String?): Boolean {
		return rootSection.isInt(key)
	}

	fun getLong(key: String?): Long {
		return this.getLong(key, 0)
	}

	fun getLong(key: String?, defaultValue: Long): Long {
		return if (isCorrect) rootSection.getLong(key, defaultValue) else defaultValue
	}

	fun isLong(key: String?): Boolean {
		return rootSection.isLong(key)
	}

	fun getDouble(key: String?): Double {
		return this.getDouble(key, 0.0)
	}

	fun getDouble(key: String?, defaultValue: Double): Double {
		return if (isCorrect) rootSection.getDouble(key, defaultValue) else defaultValue
	}

	fun isDouble(key: String?): Boolean {
		return rootSection.isDouble(key)
	}

	fun getString(key: String?): String? {
		return this.getString(key, "")
	}

	fun getString(key: String?, defaultValue: String): String? {
		return if (isCorrect) rootSection.getString(key, defaultValue) else defaultValue
	}

	fun isString(key: String?): Boolean {
		return rootSection.isString(key)
	}

	fun getBoolean(key: String?): Boolean {
		return this.getBoolean(key, false)
	}

	fun getBoolean(key: String?, defaultValue: Boolean): Boolean {
		return if (isCorrect) rootSection.getBoolean(key, defaultValue) else defaultValue
	}

	fun isBoolean(key: String?): Boolean {
		return rootSection.isBoolean(key)
	}

	fun getList(key: String?): List<*>? {
		return this.getList(key, null)
	}

	fun getList(key: String?, defaultList: List<*>?): List<*>? {
		return if (isCorrect) rootSection.getList(key, defaultList) else defaultList
	}

	fun isList(key: String?): Boolean {
		return rootSection.isList(key)
	}

	fun getStringList(key: String?): List<String?>? {
		return rootSection.getStringList(key)
	}

	fun getIntegerList(key: String?): List<Int?>? {
		return rootSection.getIntegerList(key)
	}

	fun getBooleanList(key: String?): List<Boolean?>? {
		return rootSection.getBooleanList(key)
	}

	fun getDoubleList(key: String?): List<Double?>? {
		return rootSection.getDoubleList(key)
	}

	fun getFloatList(key: String?): List<Float?>? {
		return rootSection.getFloatList(key)
	}

	fun getLongList(key: String?): List<Long?>? {
		return rootSection.getLongList(key)
	}

	fun getByteList(key: String?): List<Byte?>? {
		return rootSection.getByteList(key)
	}

	fun getCharacterList(key: String?): List<Char?>? {
		return rootSection.getCharacterList(key)
	}

	fun getShortList(key: String?): List<Short?>? {
		return rootSection.getShortList(key)
	}

	fun getMapList(key: String?): List<Map<*, *>?>? {
		return rootSection.getMapList(key)
	}

	fun setAll(map: LinkedHashMap<String?, Any>?) {
		rootSection = ConfigSection(map)
	}

	fun setAll(section: ConfigSection) {
		rootSection = section
	}

	fun exists(key: String): Boolean {
		return rootSection.exists(key)
	}

	fun exists(key: String, ignoreCase: Boolean): Boolean {
		return rootSection.exists(key, ignoreCase)
	}

	fun remove(key: String?) {
		rootSection.remove(key)
	}

	val all: Map<String?, Any?>?
		get() = rootSection.allMap

	fun setDefault(map: LinkedHashMap<String?, Any>?): Int {
		return setDefault(ConfigSection(map))
	}

	fun setDefault(map: ConfigSection): Int {
		val size = rootSection.size
		rootSection = fillDefaults(map, rootSection)
		return rootSection.size - size
	}

	private fun fillDefaults(defaultMap: ConfigSection, data: ConfigSection): ConfigSection {
		for (key in defaultMap.keys) {
			if (!data.containsKey(key)) {
				data[key] = defaultMap[key]
			}
		}
		return data
	}

	private fun parseList(content: String) {
		var content = content
		content = content.replace("\r\n", "\n")
		for (v in content.split("\n").toTypedArray()) {
			if (v.trim { it <= ' ' }.isEmpty()) {
				continue
			}
			rootSection[v] = true
		}
	}

	private fun writeProperties(): String {
		var content = """
			#Properties Config file
			#${SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Date())}

			""".trimIndent()
		for (o in rootSection.entries) {
			val entry = o as Map.Entry<*, *>
			var v = entry.value!!
			val k = entry.key!!
			if (v is Boolean) {
				v = if (v) "on" else "off"
			}
			content += "$k=$v\r\n"
		}
		return content
	}

	private fun parseProperties(content: String) {
		for (line in content.split("\n").toTypedArray()) {
			if (Pattern.compile("[a-zA-Z0-9\\-_.]*+=+[^\\r\\n]*").matcher(line).matches()) {
				val splitIndex = line.indexOf('=')
				if (splitIndex == -1) {
					continue
				}
				val key = line.substring(0, splitIndex)
				val value = line.substring(splitIndex + 1)
				val valueLower = value.toLowerCase()
				if (rootSection.containsKey(key)) {
					MainLogger.getLogger().debug("[Config] Repeated property " + key + " on file " + file.toString())
				}
				when (valueLower) {
					"on", "true", "yes" -> rootSection[key] = true
					"off", "false", "no" -> rootSection[key] = false
					else -> rootSection[key] = value
				}
			}
		}
	}

	@Deprecated("use {@link #get(String)} instead")
	fun getNested(key: String?): Any? {
		return get(key)
	}

	@Deprecated("use {@link #get(String, Object)} instead")
	fun <T> getNested(key: String?, defaultValue: T): T {
		return get(key, defaultValue)
	}

	@Deprecated("use {@link #get(String)} instead")
	fun <T> getNestedAs(key: String?, type: Class<T>?): T? {
		return get(key) as T?
	}

	@Deprecated("use {@link #remove(String)} instead")
	fun removeNested(key: String?) {
		remove(key)
	}

	private fun parseContent(content: String) {
		when (type) {
			PROPERTIES -> parseProperties(content)
			JSON -> {
				val builder = GsonBuilder()
				val gson = builder.create()
				rootSection = ConfigSection(gson.fromJson(content, object : TypeToken<LinkedHashMap<String?, Any?>?>() {}.type))
			}
			YAML -> {
				val dumperOptions = DumperOptions()
				dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
				val yaml = Yaml(dumperOptions)
				rootSection = ConfigSection(yaml.loadAs(content, LinkedHashMap::class.java))
			}
			ENUM -> parseList(content)
			else -> isCorrect = false
		}
	}

	val keys: Set<String?>?
		get() = if (isCorrect) rootSection.getKeys() else HashSet()

	fun getKeys(child: Boolean): Set<String?>? {
		return if (isCorrect) rootSection.getKeys(child) else HashSet()
	}
}