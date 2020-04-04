package cn.nukkit.plugin

import cn.nukkit.Server
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.command.PluginIdentifiableCommand
import cn.nukkit.utils.Config
import cn.nukkit.utils.Utils
import com.google.common.base.Preconditions
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * 一般的Nukkit插件需要继承的类。<br></br>
 * A class to be extended by a normal Nukkit plugin.
 *
 * @author MagicDroidX(code) @ Nukkit Project
 * @author 粉鞋大妈(javadoc) @ Nukkit Project
 * @see cn.nukkit.plugin.PluginDescription
 *
 * @since Nukkit 1.0 | Nukkit API 1.0.0
 */
abstract class PluginBase : Plugin {
	private var loader: PluginLoader? = null
	private var server: Server? = null
	private override var isEnabled = false
	private var initialized = false
	private var description: PluginDescription? = null
	private override var dataFolder: File? = null
	private var config: Config? = null
	private var configFile: File? = null
	private var file: File? = null
	private var logger: PluginLogger? = null
	override fun onLoad() {}
	override fun onEnable() {}
	override fun onDisable() {}
	override fun isEnabled(): Boolean {
		return isEnabled
	}

	/**
	 * 加载这个插件。<br></br>
	 * Enables this plugin.
	 *
	 *
	 *
	 * 如果你需要卸载这个插件，建议使用[.setEnabled]<br></br>
	 * If you need to disable this plugin, it's recommended to use [.setEnabled]
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	fun setEnabled() {
		this.setEnabled(true)
	}

	/**
	 * 加载或卸载这个插件。<br></br>
	 * Enables or disables this plugin.
	 *
	 *
	 *
	 * 插件管理器插件常常使用这个方法。<br></br>
	 * It's normally used by a plugin manager plugin to manage plugins.
	 *
	 * @param value `true`为加载，`false`为卸载。<br></br>`true` for enable, `false` for disable.
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	fun setEnabled(value: Boolean) {
		if (isEnabled != value) {
			isEnabled = value
			if (isEnabled) {
				onEnable()
			} else {
				onDisable()
			}
		}
	}

	override fun isDisabled(): Boolean {
		return !isEnabled
	}

	override fun getDataFolder(): File? {
		return dataFolder
	}

	override fun getDescription(): PluginDescription? {
		return description
	}

	/**
	 * 初始化这个插件。<br></br>
	 * Initialize the plugin.
	 *
	 *
	 *
	 * 这个方法会在加载(load)之前被插件加载器调用，初始化关于插件的一些事项，不能被重写。<br></br>
	 * Called by plugin loader before load, and initialize the plugin. Can't be overridden.
	 *
	 * @param loader      加载这个插件的插件加载器的`PluginLoader`对象。<br></br>
	 * The plugin loader ,which loads this plugin, as a `PluginLoader` object.
	 * @param server      运行这个插件的服务器的`Server`对象。<br></br>
	 * The server running this plugin, as a `Server` object.
	 * @param description 描述这个插件的`PluginDescription`对象。<br></br>
	 * A `PluginDescription` object that describes this plugin.
	 * @param dataFolder  这个插件的数据的文件夹。<br></br>
	 * The data folder of this plugin.
	 * @param file        这个插件的文件`File`对象。对于jar格式的插件，就是jar文件本身。<br></br>
	 * The `File` object of this plugin itself. For jar-packed plugins, it is the jar file itself.
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	fun init(loader: PluginLoader?, server: Server?, description: PluginDescription?, dataFolder: File?, file: File?) {
		if (!initialized) {
			initialized = true
			this.loader = loader
			this.server = server
			this.description = description
			this.dataFolder = dataFolder
			this.file = file
			configFile = File(this.dataFolder, "config.yml")
			logger = PluginLogger(this)
		}
	}

	override fun getLogger(): PluginLogger? {
		return logger
	}

	/**
	 * 返回这个插件是否已经初始化。<br></br>
	 * Returns if this plugin is initialized.
	 *
	 * @return 这个插件是否已初始化。<br></br>if this plugin is initialized.
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	fun isInitialized(): Boolean {
		return initialized
	}

	/**
	 * TODO: FINISH JAVADOC
	 */
	fun getCommand(name: String): PluginIdentifiableCommand? {
		var command = getServer()!!.getPluginCommand(name)
		if (command == null || command.plugin != this) {
			command = getServer()!!.getPluginCommand(description!!.name!!.toLowerCase() + ":" + name)
		}
		return if (command != null && command.plugin == this) {
			command
		} else {
			null
		}
	}

	override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<String?>?): Boolean {
		return false
	}

	override fun getResource(filename: String?): InputStream {
		return this.javaClass.classLoader.getResourceAsStream(filename)
	}

	override fun saveResource(filename: String?): Boolean {
		return saveResource(filename, false)
	}

	override fun saveResource(filename: String?, replace: Boolean): Boolean {
		return saveResource(filename, filename, replace)
	}

	override fun saveResource(filename: String?, outputName: String?, replace: Boolean): Boolean {
		Preconditions.checkArgument(filename != null && outputName != null, "Filename can not be null!")
		Preconditions.checkArgument(filename!!.trim { it <= ' ' }.length != 0 && outputName!!.trim { it <= ' ' }.length != 0, "Filename can not be empty!")
		val out = File(dataFolder, outputName)
		if (!out.exists() || replace) {
			try {
				getResource(filename).use { resource ->
					if (resource != null) {
						val outFolder = out.parentFile
						if (!outFolder.exists()) {
							outFolder.mkdirs()
						}
						Utils.writeFile(out, resource)
						return true
					}
				}
			} catch (e: IOException) {
				Server.instance!!.logger.logException(e)
			}
		}
		return false
	}

	override fun getConfig(): Config? {
		if (config == null) {
			reloadConfig()
		}
		return config
	}

	override fun saveConfig() {
		if (!getConfig()!!.save()) {
			getLogger()!!.critical("Could not save config to " + configFile.toString())
		}
	}

	override fun saveDefaultConfig() {
		if (!configFile!!.exists()) {
			this.saveResource("config.yml", false)
		}
	}

	override fun reloadConfig() {
		config = Config(configFile)
		val configStream = getResource("config.yml")
		if (configStream != null) {
			val dumperOptions = DumperOptions()
			dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
			val yaml = Yaml(dumperOptions)
			try {
				config!!.setDefault(yaml.loadAs(Utils.readFile(configFile), LinkedHashMap::class.java))
			} catch (e: IOException) {
				Server.instance!!.logger.logException(e)
			}
		}
	}

	override fun getServer(): Server? {
		return server
	}

	override fun getName(): String? {
		return description!!.name
	}

	/**
	 * 返回这个插件完整的名字。<br></br>
	 * Returns the full name of this plugin.
	 *
	 *
	 *
	 * 一个插件完整的名字由`名字+" v"+版本号`组成。比如：<br></br>
	 * A full name of a plugin is composed by `name+" v"+version`.for example:
	 *
	 * `HelloWorld v1.0.0`
	 *
	 * @return 这个插件完整的名字。<br></br>The full name of this plugin.
	 * @see cn.nukkit.plugin.PluginDescription.getFullName
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	fun getFullName(): String? {
		return description!!.fullName
	}

	/**
	 * 返回这个插件的文件`File`对象。对于jar格式的插件，就是jar文件本身。<br></br>
	 * Returns the `File` object of this plugin itself. For jar-packed plugins, it is the jar file itself.
	 *
	 * @return 这个插件的文件 `File`对象。<br></br>The `File` object of this plugin itself.
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	protected fun getFile(): File? {
		return file
	}

	override fun getPluginLoader(): PluginLoader? {
		return loader
	}
}