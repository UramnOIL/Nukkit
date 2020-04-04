package cn.nukkit

import cn.nukkit.network.protocol.ProtocolInfo
import cn.nukkit.utils.ServerKiller
import com.google.common.base.Preconditions
import joptsimple.OptionParser
import joptsimple.OptionSet
import joptsimple.OptionSpec
import lombok.extern.log4j.Log4j2
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.Configuration
import org.apache.logging.log4j.core.config.LoggerConfig
import java.io.IOException
import java.io.InputStream
import java.util.Properties

/*
 * `_   _       _    _    _ _
 * | \ | |     | |  | |  (_) |
 * |  \| |_   _| | _| | ___| |_
 * | . ` | | | | |/ / |/ / | __|
 * | |\  | |_| |   <|   <| | |_
 * |_| \_|\__,_|_|\_\_|\_\_|\__|
 */
/**
 * Nukkit启动类，包含`main`函数。<br></br>
 * The launcher class of Nukkit, including the `main` function.
 *
 * @author MagicDroidX(code) @ Nukkit Project
 * @author 粉鞋大妈(javadoc) @ Nukkit Project
 * @since Nukkit 1.0 | Nukkit API 1.0.0
 */
object Nukkit {
	val GIT_INFO: Properties? = gitInfo
	val VERSION = version
	const val API_VERSION = "1.0.9"
	const val CODENAME = ""

	@Deprecated
	val MINECRAFT_VERSION: String = ProtocolInfo.MINECRAFT_VERSION

	@Deprecated
	val MINECRAFT_VERSION_NETWORK: String = ProtocolInfo.MINECRAFT_VERSION_NETWORK
	val PATH: String = System.getProperty("user.dir").toString() + "/"
	val DATA_PATH: String = System.getProperty("user.dir").toString() + "/"
	val PLUGIN_PATH = DATA_PATH + "plugins"
	val START_TIME: Long = System.currentTimeMillis()
	var ANSI = true
	var TITLE = false
	var shortTitle = requiresShortTitle()
	var DEBUG = 1
	fun main(args: Array<String?>?) {
		// Force IPv4 since Nukkit is not compatible with IPv6
		System.setProperty("java.net.preferIPv4Stack", "true")
		System.setProperty("log4j.skipJansi", "false")

		// Force Mapped ByteBuffers for LevelDB till fixed.
		System.setProperty("leveldb.mmap", "true")

		// Define args
		val parser = OptionParser()
		parser.allowsUnrecognizedOptions()
		val helpSpec: OptionSpec<Void> = parser.accepts("help", "Shows this page").forHelp()
		val ansiSpec: OptionSpec<Void> = parser.accepts("disable-ansi", "Disables console coloring")
		val titleSpec: OptionSpec<Void> = parser.accepts("enable-title", "Enables title at the top of the window")
		val vSpec: OptionSpec<String> = parser.accepts("v", "Set verbosity of logging").withRequiredArg().ofType(String::class.java)
		val verbositySpec: OptionSpec<String> = parser.accepts("verbosity", "Set verbosity of logging").withRequiredArg().ofType(String::class.java)
		val languageSpec: OptionSpec<String> = parser.accepts("language", "Set a predefined language").withOptionalArg().ofType(String::class.java)

		// Parse arguments
		val options: OptionSet = parser.parse(args)
		if (options.has(helpSpec)) {
			try {
				// Display help page
				parser.printHelpOn(System.out)
			} catch (e: IOException) {
				// ignore
			}
			return
		}
		ANSI = !options.has(ansiSpec)
		TITLE = options.has(titleSpec)
		var verbosity: String = options.valueOf(vSpec)
		if (verbosity == null) {
			verbosity = options.valueOf(verbositySpec)
		}
		if (verbosity != null) {
			try {
				val level: Level = Level.valueOf(verbosity)
				logLevel = level
			} catch (e: Exception) {
				// ignore
			}
		}
		val language: String = options.valueOf(languageSpec)
		try {
			if (TITLE) {
				System.out.print(0x1b as Char.toString() + "]0;Nukkit is starting up..." + 0x07.toChar())
			}
			Server(PATH, DATA_PATH, PLUGIN_PATH, language)
		} catch (t: Throwable) {
			log.throwing(t)
		}
		if (TITLE) {
			System.out.print(0x1b as Char.toString() + "]0;Stopping Server..." + 0x07.toChar())
		}
		log.info("Stopping other threads")
		for (thread in java.lang.Thread.getAllStackTraces().keySet()) {
			if (thread !is InterruptibleThread) {
				continue
			}
			log.debug("Stopping {} thread", thread.getClass().getSimpleName())
			if (thread.isAlive()) {
				thread.interrupt()
			}
		}
		val killer = ServerKiller(8)
		killer.start()
		if (TITLE) {
			System.out.print(0x1b as Char.toString() + "]0;Server Stopped" + 0x07.toChar())
		}
		System.exit(0)
	}

	private fun requiresShortTitle(): Boolean {
		//Shorter title for windows 8/2012
		val osName: String = System.getProperty("os.name").toLowerCase()
		return osName.contains("windows") && (osName.contains("windows 8") || osName.contains("2012"))
	}

	private val gitInfo: Properties?
		private get() {
			val gitFileStream: InputStream = Nukkit::class.java.getClassLoader().getResourceAsStream("git.properties")
					?: return null
			val properties = Properties()
			try {
				properties.load(gitFileStream)
			} catch (e: IOException) {
				return null
			}
			return properties
		}

	private val version: String
		private get() {
			val version = StringBuilder()
			version.append("git-")
			var commitId: String?
			return if (GIT_INFO == null || GIT_INFO.getProperty("git.commit.id.abbrev").also({ commitId = it }) == null) {
				version.append("null").toString()
			} else version.append(commitId).toString()
		}

	var logLevel: Level
		get() {
			val ctx: LoggerContext = LogManager.getContext(false) as LoggerContext
			val log4jConfig: Configuration = ctx.getConfiguration()
			val loggerConfig: LoggerConfig = log4jConfig.getLoggerConfig(org.apache.logging.log4j.LogManager.ROOT_LOGGER_NAME)
			return loggerConfig.getLevel()
		}
		set(level) {
			Preconditions.checkNotNull(level, "level")
			val ctx: LoggerContext = LogManager.getContext(false) as LoggerContext
			val log4jConfig: Configuration = ctx.getConfiguration()
			val loggerConfig: LoggerConfig = log4jConfig.getLoggerConfig(org.apache.logging.log4j.LogManager.ROOT_LOGGER_NAME)
			loggerConfig.setLevel(level)
			ctx.updateLoggers()
		}
}