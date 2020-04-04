package cn.nukkit.plugin

import cn.nukkit.permission.Permission
import cn.nukkit.utils.PluginException
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.util.*
import kotlin.collections.ArrayList

/* TODO Add these to Javadoc：
 *     <li><i>softdepend</i><br>
 *     <br>
 *     </li>
 *     <li><i>loadbefore</i><br>
 *     <br>
 *     </li>
 */
/**
 * 描述一个Nukkit插件的类。<br></br>
 * Describes a Nukkit plugin.
 *
 * 在jar格式的插件中，插件的描述内容可以在plugin.yml中定义。比如这个：<br></br>
 * The description of a jar-packed plugin can be defined in the 'plugin.yml' file. For example:
 * <blockquote><pre>
 * **name:** HelloWorldPlugin
 * **main:** com.cnblogs.xtypr.helloworldplugin.HelloWorldPlugin
 * **version:** "1.0.0"
 * **api:** ["1.0.0"]
 * load: POSTWORLD
 * author: 粉鞋大妈
 * description: A simple Hello World plugin for Nukkit
 * website: http://www.cnblogs.com/xtypr
 * permissions:
 * helloworldplugin.command.helloworld:
 * description: Allows to use helloworld command.
 * default: true
 * commands:
 * helloworld:
 * description: the helloworld command
 * usage: "/helloworld"
 * permission: helloworldplugin.command.helloworld
 * depend:
 * - TestPlugin1
</pre></blockquote> *
 * 在使用plugin.yml来定义插件时，`name`、`main`、`version`、`api`这几个字段是必需的，
 * 要让Nukkit能够正常加载你的插件，必须要合理地填写这几个字段。<br></br>
 * When using plugin.yml file to define your plugin, it's REQUIRED to fill these items:
 * `name`,`main`,`version` and `api`.You are supposed to fill these items to make sure
 * your plugin can be normally loaded by Nukkit.<br></br>
 *
 *
 * 接下来对所有的字段做一些说明，**加粗**的字段表示必需，*斜体*表示可选：（来自
 * [粉鞋大妈的博客文章](http://www.cnblogs.com/xtypr/p/nukkit_plugin_start_from_0_about_config.html)）<br></br>
 * Here are some instructions for there items, **bold** means required, *italic* means optional: (From
 * [a blog article of @粉鞋大妈](http://www.cnblogs.com/xtypr/p/nukkit_plugin_start_from_0_about_config.html))
 *
 *
 *  * **name**<br></br>
 * 字符串，表示这个插件的名字，名字是区分不同插件的标准之一。
 * 插件的名字*不能包含“nukkit”“minecraft”“mojang”*这几个字符串，而且不应该包含空格。<br></br>
 * String, the plugin name. Name is one of the ways to distinguish different plugins.
 * A plugin name *can't contain 'nukkit' 'minecraft' 'mojang'*, and shouldn't contain spaces.
 *  * **version**<br></br>
 * 字符串，表示这个插件的版本号。使用类似于1.0.0这样的版本号时，应该使用引号包围来防止误识别。<br></br>
 * String, the version string of plugin. When using the version string like "1.0.0",
 * quotation marks are required to add, or there will be an exception.
 *  * **api**<br></br>
 * 字符串序列，表示这个插件支持的Nukkit API版本号列表。插件作者应该调试能支持的API，然后把版本号添加到这个列表。<br></br>
 * A set of String, the Nukkit API versions that the plugin supports. Plugin developers should debug in different
 * Nukkit APIs and try out the versions supported, and add them to this list.
 *  * **main**<br></br>
 * 字符串，表示这个插件的主类。插件的主类*不能放在“cn.nukkit”包下*。<br></br>
 * String, the main class of plugin. The main class* can't be placed at 'cn.nukkit' package*.
 *  * *author* or *authors*<br></br>
 * 字符串/字符串序列，两个任选一个，表示这个插件的作者/作者列表。<br></br>
 * String or A set of String. One of two is chosen, to describe the author or the list of authors.
 *  * *website*<br></br>
 * 字符串，表示这个插件的网站。插件使用者或者开发者可以访问这个网站来获取插件更多的信息。
 * 这个网站可以是插件发布帖子或者插件官网等。<br></br>
 * String, the website of plugin. More information can be found by visiting this website. The website
 * can be a forum post or the official website.
 *  * *description*<br></br>
 * 字符串，表示这个插件的一些描述。<br></br>
 * String, some description of plugin.
 *  * *depend*<br></br>
 * 序列，表示这个插件所依赖的一个或一些插件的名字的列表。参见：[PluginDescription.getDepend]<br></br>
 * List, strings for plugin names, what is depended on by this plugin. See:
 * [PluginDescription.getDepend]
 *  * *prefix*<br></br>
 * 字符串，表示这个插件的消息头衔。参见：[PluginDescription.getPrefix]<br></br>
 * String, the message title of the plugin. See: [PluginDescription.getPrefix]
 *  * *load*<br></br>
 * 字符串，表示这个插件的加载顺序，或者说在什么时候加载。参见：[PluginLoadOrder]<br></br>
 * String, the load order of plugin, or when the plugin loads. See: [PluginLoadOrder]
 *  * *commands*<br></br>
 * 序列，表示这个插件的命令列表。<br></br>
 * List, the command list.
 *  * *permissions*<br></br>
 * 序列，表示这个插件的权限组列表。<br></br>
 * List, the list of permission groups defined.
 *
 *
 * @author MagicDroidX(code) @ Nukkit Project
 * @author iNevet(code and javadoc) @ Nukkit Project
 * @author 粉鞋大妈(javadoc) @ Nukkit Project
 * @see Plugin
 *
 * @see PluginLoadOrder
 *
 * @since Nukkit 1.0 | Nukkit API 1.0.0
 */
class PluginDescription(yamlMap: Map<String, Any>) {
	/**
	 * 返回这个插件的名字。<br></br>
	 * Returns the name of this plugin.
	 *
	 * @see PluginDescription
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	val name: String = (yamlMap["name"] as String?)!!.replace("[^A-Za-z0-9 _.-]".toRegex(), "").replace(" ", "_")

	/**
	 * 返回这个插件的主类名。<br></br>
	 * Returns the main class name of this plugin.
	 *
	 *
	 * 一个插件的加载都是从主类开始的。主类的名字在插件的配置文件中定义后可以通过这个函数返回。一个返回值例子：<br></br>
	 * The load action of a Nukkit plugin begins from main class. The name of main class should be defined
	 * in the plugin configuration, and it can be returned by this function. An example for return value: <br></br>
	 * `"com.example.ExamplePlugin"`
	 *
	 * @see PluginDescription
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	val main: String = yamlMap["main"] as String

	val apis: List<String> = yamlMap["api"]?.let { if(it is List<*>) it as List<String> else mutableListOf(it as String) }!!
	/**
	 * 返回这个插件所依赖的插件名字。<br></br>
	 * The names of the plugins what is depended by this plugin.
	 *
	 * Nukkit插件的依赖有这些注意事项：<br></br>Here are some note for Nukkit plugin depending:
	 *
	 *  * 一个插件不能依赖自己（否则会报错）。<br></br>A plugin can not depend on itself (or there will be an exception).
	 *  * 如果一个插件依赖另一个插件，那么必须要安装依赖的插件后才能加载这个插件。<br></br>
	 * If a plugin relies on another one, the another one must be installed at the same time, or Nukkit
	 * won't load this plugin.
	 *  * 当一个插件所依赖的插件不存在时，Nukkit不会加载这个插件，但是会提醒用户去安装所依赖的插件。<br></br>
	 * When the required dependency plugin does not exists, Nukkit won't load this plugin, but will tell the
	 * user that this dependency is required.
	 *
	 *
	 *
	 * 举个例子，如果A插件依赖于B插件，在没有安装B插件而安装A插件的情况下，Nukkit会阻止A插件的加载。
	 * 只有在安装B插件前安装了它所依赖的A插件，Nukkit才会允许加载B插件。<br></br>
	 * For example, there is a Plugin A which relies on Plugin B. If you installed A without installing B,
	 * Nukkit won't load A because its dependency B is lost. Only when B is installed, A will be loaded
	 * by Nukkit.
	 *
	 * @see PluginDescription
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	val depend: MutableList<String> = yamlMap["depend"] as MutableList<String>? ?: mutableListOf()

	val softDepend: MutableList<String> = yamlMap["softdepend"] as MutableList<String>? ?: mutableListOf()

	val loadBefore: List<String> = yamlMap["loadbefore"] as List<String>? ?: listOf()

	/**
	 * 返回这个插件的版本号。<br></br>
	 * Returns the version string of this plugin.
	 *
	 * @return 这个插件的版本号。<br></br>The version string od this plugin.
	 * @see PluginDescription
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	val version: String = yamlMap["version"].toString()
	/**
	 * 返回这个插件定义的命令列表。<br></br>
	 * Returns all the defined commands of this plugin.
	 *
	 * @return 这个插件定义的命令列表。<br></br>A yamlMap of all defined commands of this plugin.
	 * @see PluginDescription
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	val commands: Map<String, Any> = (yamlMap["commands"] as Map<String, Any>?) ?: mapOf<String, Any>()

	/**
	 * 返回这个插件的描述文字。<br></br>
	 * Returns the description text of this plugin.
	 *
	 * @return 这个插件的描述文字。<br></br>The description text of this plugin.
	 * @see PluginDescription
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	val description: String = yamlMap["description"] as String? ?: ""

	/**
	 * 返回这个插件的网站。<br></br>
	 * Returns the website of this plugin.
	 *
	 * @return 这个插件的网站。<br></br>The website of this plugin.
	 * @see PluginDescription
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	val authors: List<String> = mutableListOf(*yamlMap["authors"] as Array<String>).apply{
		val author = yamlMap["author"] as String?
		if(author != null) this.add(author)
	}
	val website: String? = yamlMap["website"] as String?
	/**
	 * 返回这个插件的信息前缀。<br></br>
	 * Returns the message title of this plugin.
	 *
	 *
	 * 插件的信息前缀在记录器记录信息时，会作为信息头衔使用。如果没有定义记录器，会使用插件的名字作为信息头衔。<br></br>
	 * When a PluginLogger logs, the message title is used as the prefix of message. If prefix is undefined,
	 * the plugin name will be used instead.
	 * The message title of this plugin, or`null` if undefined.
	 * @see PluginLogger
	 *
	 * @see PluginDescription
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	val prefix: String? = yamlMap["prefix"] as String?

	/**
	 * 返回这个插件加载的顺序，即插件应该在什么时候加载。<br></br>
	 * Returns the order the plugin loads, or when the plugin is loaded.
	 *
	 * @return 这个插件加载的顺序。<br></br>The order the plugin loads.
	 * @see PluginDescription
	 *
	 * @see PluginLoadOrder
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	val order: PluginLoadOrder = yamlMap["load"]?.let { runCatching { PluginLoadOrder.valueOf(yamlMap["load"] as String) }.getOrNull() } ?: PluginLoadOrder.POSTWORLD

	/**
	 * 返回这个插件定义的权限列表。<br></br>
	 * Returns all the defined permissions of this plugin.
	 *
	 * @return 这个插件定义的权限列表。<br></br>A map of all defined permissions of this plugin.
	 * @see PluginDescription
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	val permissions: List<Permission> = yamlMap["permissions"]?.let { Permission.loadPermissions(it as Map<String, Any>) } ?: listOf()

	init {
		if (name == "") {
			throw PluginException("Invalid PluginDescription name")
		}
	}

	constructor(yamlString: String): this(Yaml(DumperOptions().apply { this.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK }).loadAs(yamlString, Map::class.java) as Map<String, Any>)


	/**
	 * 返回这个插件完整的名字。<br></br>
	 * Returns the full name of this plugin.
	 *
	 *
	 * 一个插件完整的名字由`名字+" v"+版本号`组成。比如：<br></br>
	 * A full name of a plugin is composed by `name+" v"+version`.for example:
	 *
	 * `HelloWorld v1.0.0`
	 *
	 * @see PluginDescription
	 *
	 * @since Nukkit 1.0 | Nukkit API 1.0.0
	 */
	val fullName = "$name v$version"
}