package cn.nukkit.plugin

import cn.nukkit.Server
import cn.nukkit.command.CommandExecutor
import cn.nukkit.utils.Config
import java.io.File
import java.io.InputStream

/**
 * 所有Nukkit插件必须实现的接口。<br></br>
 * An interface what must be implemented by all Nukkit plugins.
 *
 *
 * 对于插件作者，我们建议让插件主类继承[cn.nukkit.plugin.PluginBase]类，而不是实现这个接口。<br></br>
 * For plugin developers: it's recommended to use [cn.nukkit.plugin.PluginBase] for an actual plugin
 * instead of implement this interface.
 *
 * @author MagicDroidX(code) @ Nukkit Project
 * @author 粉鞋大妈(javadoc) @ Nukkit Project
 * @see cn.nukkit.plugin.PluginBase
 *
 * @see cn.nukkit.plugin.PluginDescription
 *
 * @since Nukkit 1.0 | Nukkit API 1.0.0
 */
interface Plugin : CommandExecutor {
	/**
	 * 在一个Nukkit插件被加载时调用的方法。这个方法会在[Plugin.onEnable]之前调用。<br></br>
	 * Called when a Nukkit plugin is loaded, before [Plugin.onEnable] .
	 *
	 *
	 * 应该填写加载插件时需要作出的动作。例如：初始化数组、初始化数据库连接。<br></br>
	 * Use this to init a Nukkit plugin, such as init arrays or init database connections.
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	fun onLoad()

	/**
	 * 在一个Nukkit插件被启用时调用的方法。<br></br>
	 * Called when a Nukkit plugin is enabled.
	 *
	 *
	 * 应该填写插件启用时需要作出的动作。例如：读取配置文件、读取资源、连接数据库。<br></br>
	 * Use this to open config files, open resources, connect databases.
	 *
	 *
	 * 注意到可能存在的插件管理器插件，这个方法在插件多次重启时可能被调用多次。<br></br>
	 * Notes that there may be plugin manager plugins,
	 * this method can be called many times when a plugin is restarted many times.
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	fun onEnable()

	/**
	 * 返回这个Nukkit插件是否已启用。<br></br>
	 * Whether this Nukkit plugin is enabled.
	 *
	 * @return 这个插件是否已经启用。<br></br>Whether this plugin is enabled.
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	val isEnabled: Boolean

	/**
	 * 在一个Nukkit插件被停用时调用的方法。<br></br>
	 * Called when a Nukkit plugin is disabled.
	 *
	 *
	 * 应该填写插件停用时需要作出的动作。例如：关闭数据库，断开资源。<br></br>
	 * Use this to free open things and finish actions,
	 * such as disconnecting databases and close resources.
	 *
	 *
	 * 注意到可能存在的插件管理器插件，这个方法在插件多次重启时可能被调用多次。<br></br>
	 * Notes that there may be plugin manager plugins,
	 * this method can be called many times when a plugin is restarted many times.
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	fun onDisable()

	/**
	 * 返回这个Nukkit插件是否已停用。<br></br>
	 * Whether this Nukkit plugin is disabled.
	 *
	 * @return 这个插件是否已经停用。<br></br>Whether this plugin is disabled.
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	val isDisabled: Boolean

	val dataFolder: File

	val description: PluginDescription

	/**
	 * 返回运行这个插件的服务器的[cn.nukkit.Server]对象。<br></br>
	 * Gets the server which is running this plugin, and returns as a [cn.nukkit.Server] object.
	 *
	 * @see cn.nukkit.Server
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	val server: Server

	/**
	 * 返回这个插件的名字。<br></br>
	 * Returns the name of this plugin.
	 *
	 *
	 * Nukkit会从已经读取的插件描述中获取插件的名字。<br></br>
	 * Nukkit will read plugin name from plugin description.
	 *
	 * @see cn.nukkit.plugin.Plugin.description
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	val name: String

	/**
	 * 返回这个插件的日志记录器为[cn.nukkit.plugin.PluginLogger]对象。<br></br>
	 * Returns the logger of this plugin as a [cn.nukkit.plugin.PluginLogger] object.
	 *
	 *
	 * 使用日志记录器，你可以在控制台和日志文件输出信息。<br></br>
	 * You can use a plugin logger to output messages to the console and log file.
	 *
	 * @see cn.nukkit.plugin.PluginLogger
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	val logger: PluginLogger

	/**
	 * 返回这个插件的加载器为[cn.nukkit.plugin.PluginLoader]对象。<br></br>
	 * Returns the loader of this plugin as a [cn.nukkit.plugin.PluginLoader] object.
	 *
	 * @see cn.nukkit.plugin.PluginLoader
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	val pluginLoader: PluginLoader

	/**
	 * 读取这个插件特定的资源文件，并返回为`InputStream`对象。<br></br>
	 * Reads a resource of this plugin, and returns as an `InputStream` object.
	 *
	 *
	 * 对于jar格式的Nukkit插件，Nukkit会在jar包内的资源文件夹(一般为resources文件夹)寻找资源文件。<br></br>
	 * For jar-packed Nukkit plugins, Nukkit will look for your resource file in the resources folder,
	 * which is normally named 'resources' and placed in plugin jar file.
	 *
	 *
	 * 当你需要把一个文件的所有内容读取为字符串，可以使用[cn.nukkit.utils.Utils.readFile]函数，
	 * 来从`InputStream`读取所有内容为字符串。例如：<br></br>
	 * When you need to read the whole file content as a String, you can use [cn.nukkit.utils.Utils.readFile]
	 * to read from a `InputStream` and get whole content as a String. For example:
	 *
	 * `String string = Utils.readFile(this.getResource("string.txt"));`
	 *
	 * @param filename 要读取的资源文件名字。<br></br>The name of the resource file to read.
	 * @return 读取的资源文件的 `InputStream`对象。若错误会返回`null`<br></br>
	 * The resource as an `InputStream` object, or `null` when an error occurred.
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	fun getResource(filename: String): InputStream

	/**
	 * 保存这个Nukkit插件的资源。<br></br>
	 * Saves the resource of this plugin.
	 *
	 *
	 * 对于jar格式的Nukkit插件，Nukkit会在jar包内的资源文件夹寻找资源文件，然后保存到数据文件夹。<br></br>
	 * For jar-packed Nukkit plugins, Nukkit will look for your resource file in the resources folder,
	 * which is normally named 'resources' and placed in plugin jar file, and copy it into data folder.
	 *
	 *
	 * 这个函数通常用来在插件被加载(load)时，保存默认的资源文件。这样插件在启用(enable)时不会错误读取空的资源文件，
	 * 用户也无需从开发者处手动下载资源文件后再使用插件。<br></br>
	 * This is usually used to save the default plugin resource when the plugin is LOADED .If this is used,
	 * it won't happen to load an empty resource when plugin is ENABLED, and plugin users are not required to get
	 * default resources from the developer and place it manually.
	 *
	 *
	 * 如果需要替换已存在的资源文件，建议使用[cn.nukkit.plugin.Plugin.saveResource]<br></br>
	 * If you need to REPLACE an existing resource file, it's recommended
	 * to use [cn.nukkit.plugin.Plugin.saveResource].
	 *
	 * @param filename 要保存的资源文件名字。<br></br>The name of the resource file to save.
	 * @return 保存是否成功。<br></br>true if the saving action is successful.
	 * @see cn.nukkit.plugin.Plugin.saveDefaultConfig
	 *
	 * @see cn.nukkit.plugin.Plugin.saveResource
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	fun saveResource(filename: String): Boolean

	/**
	 * 保存或替换这个Nukkit插件的资源。<br></br>
	 * Saves or replaces the resource of this plugin.
	 *
	 *
	 * 对于jar格式的Nukkit插件，Nukkit会在jar包内的资源文件夹寻找资源文件，然后保存到数据文件夹。<br></br>
	 * For jar-packed Nukkit plugins, Nukkit will look for your resource file in the resources folder,
	 * which is normally named 'resources' and placed in plugin jar file, and copy it into data folder.
	 *
	 *
	 * 如果需要保存默认的资源文件，建议使用[cn.nukkit.plugin.Plugin.saveResource]<br></br>
	 * If you need to SAVE DEFAULT resource file, it's recommended
	 * to use [cn.nukkit.plugin.Plugin.saveResource].
	 *
	 * @param filename 要保存的资源文件名字。<br></br>The name of the resource file to save.
	 * @param replace  是否替换目标文件。<br></br>if true, Nukkit will replace the target resource file.
	 * @return 保存是否成功。<br></br>true if the saving action is successful.
	 * @see cn.nukkit.plugin.Plugin.saveResource
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	fun saveResource(filename: String, replace: Boolean): Boolean
	fun saveResource(filename: String, outputName: String, replace: Boolean): Boolean

	/**
	 * 返回这个Nukkit插件配置文件的[cn.nukkit.utils.Config]对象。<br></br>
	 * The config file this Nukkit plugin as a [cn.nukkit.utils.Config] object.
	 *
	 *
	 * 一般地，插件的配置保存在数据文件夹下的config.yml文件。<br></br>
	 * Normally, the plugin config is saved in the 'config.yml' file in its data folder.
	 *
	 * @return 插件的配置文件。<br></br>The configuration of this plugin.
	 * @see cn.nukkit.plugin.Plugin.dataFolder
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	fun getConfig(): Config?

	/**
	 * 保存这个Nukkit插件的配置文件。<br></br>
	 * Saves the plugin config.
	 *
	 * @see cn.nukkit.plugin.Plugin.dataFolder
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	fun saveConfig()

	/**
	 * 保存这个Nukkit插件的默认配置文件。<br></br>
	 * Saves the DEFAULT plugin config.
	 *
	 *
	 * 执行这个函数时，Nukkit会在资源文件夹内寻找开发者配置好的默认配置文件config.yml，然后保存在数据文件夹。
	 * 如果数据文件夹已经有一个config.yml文件，Nukkit不会替换这个文件。<br></br>
	 * When this is used, Nukkit will look for the default 'config.yml' file which is configured by plugin developer
	 * and save it to the data folder. If a config.yml file exists in the data folder, Nukkit won't replace it.
	 *
	 *
	 * 这个函数通常用来在插件被加载(load)时，保存默认的配置文件。这样插件在启用(enable)时不会错误读取空的配置文件，
	 * 用户也无需从开发者处手动下载配置文件保存后再使用插件。<br></br>
	 * This is usually used to save the default plugin config when the plugin is LOADED .If this is used,
	 * it won't happen to load an empty config when plugin is ENABLED, and plugin users are not required to get
	 * default config from the developer and place it manually.
	 *
	 * @see cn.nukkit.plugin.Plugin.dataFolder
	 *
	 * @see cn.nukkit.plugin.Plugin.saveResource
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	fun saveDefaultConfig()

	/**
	 * 重新读取这个Nukkit插件的默认配置文件。<br></br>
	 * Reloads the plugin config.
	 *
	 *
	 * 执行这个函数时，Nukkit会从数据文件夹中的config.yml文件重新加载配置。
	 * 这样用户在调整插件配置后，无需重启就可以马上使用新的配置。<br></br>
	 * By using this, Nukkit will reload the config from 'config.yml' file, then it isn't necessary to restart
	 * for plugin user who changes the config and needs to use new config at once.
	 *
	 * @see cn.nukkit.plugin.Plugin.dataFolder
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	fun reloadConfig()
}