package cn.nukkit

import cn.nukkit.block.Block
import cn.nukkit.blockentity.*
import cn.nukkit.command.CommandSender
import cn.nukkit.command.ConsoleCommandSender
import cn.nukkit.command.PluginIdentifiableCommand
import cn.nukkit.command.SimpleCommandMap
import cn.nukkit.console.NukkitConsole
import cn.nukkit.entity.Attribute
import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityHuman
import cn.nukkit.entity.data.Skin
import cn.nukkit.entity.item.*
import cn.nukkit.entity.mob.*
import cn.nukkit.entity.passive.*
import cn.nukkit.entity.projectile.*
import cn.nukkit.entity.weather.EntityLightning
import cn.nukkit.event.HandlerList
import cn.nukkit.event.level.LevelInitEvent
import cn.nukkit.event.level.LevelLoadEvent
import cn.nukkit.event.server.BatchPacketsEvent
import cn.nukkit.event.server.PlayerDataSerializeEvent
import cn.nukkit.event.server.QueryRegenerateEvent
import cn.nukkit.inventory.CraftingManager
import cn.nukkit.inventory.Recipe
import cn.nukkit.item.Item
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.lang.BaseLang
import cn.nukkit.lang.TextContainer
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.level.EnumLevel
import cn.nukkit.level.GlobalBlockPalette
import cn.nukkit.level.Level
import cn.nukkit.level.biome.EnumBiome
import cn.nukkit.level.format.LevelProvider
import cn.nukkit.level.format.LevelProviderManager
import cn.nukkit.level.format.anvil.Anvil
import cn.nukkit.level.format.leveldb.LevelDB
import cn.nukkit.level.format.mcregion.McRegion
import cn.nukkit.level.generator.Flat
import cn.nukkit.level.generator.Generator
import cn.nukkit.level.generator.Nether
import cn.nukkit.level.generator.Normal
import cn.nukkit.math.NukkitMath
import cn.nukkit.metadata.EntityMetadataStore
import cn.nukkit.metadata.LevelMetadataStore
import cn.nukkit.metadata.PlayerMetadataStore
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.DoubleTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.network.CompressBatchedTask
import cn.nukkit.network.Network
import cn.nukkit.network.RakNetInterface
import cn.nukkit.network.protocol.BatchPacket
import cn.nukkit.network.protocol.DataPacket
import cn.nukkit.network.protocol.PlayerListPacket
import cn.nukkit.network.protocol.ProtocolInfo
import cn.nukkit.network.query.QueryHandler
import cn.nukkit.network.rcon.RCON
import cn.nukkit.permission.BanList
import cn.nukkit.permission.DefaultPermissions
import cn.nukkit.plugin.JavaPluginLoader
import cn.nukkit.plugin.Plugin
import cn.nukkit.plugin.PluginLoadOrder
import cn.nukkit.plugin.PluginManager
import cn.nukkit.plugin.service.NKServiceManager
import cn.nukkit.plugin.service.ServiceManager
import cn.nukkit.potion.Effect
import cn.nukkit.potion.Potion
import cn.nukkit.resourcepacks.ResourcePackManager
import cn.nukkit.scheduler.ServerScheduler
import cn.nukkit.scheduler.Task
import cn.nukkit.utils.*
import cn.nukkit.utils.bugreport.ExceptionHandler
import co.aikar.timings.Timings
import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableMap
import jdk.javadoc.internal.doclets.toolkit.util.DocPath.parent
import lombok.extern.log4j.Log4j2
import org.iq80.leveldb.CompressionType
import org.iq80.leveldb.DB
import org.iq80.leveldb.Options
import org.iq80.leveldb.impl.Iq80DBFactory
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern

/**
 * @author MagicDroidX
 * @author Box
 */
@Log4j2
class Server internal constructor(filePath: String, dataPath: String, pluginPath: String, predefinedLanguage: String) {
	val nameBans: BanList
	val iPBans: BanList
	val ops: Config
	val whitelist: Config
	private val isRunning = AtomicBoolean(true)
	private var hasStopped = false
	val pluginManager: PluginManager
	private val profilingTickrate = 20
	val scheduler: ServerScheduler
	var tick = 0
		private set
	var nextTick: Long = 0
		private set
	private val tickAverage = floatArrayOf(20f, 20f, 20f, 20f, 20f, 20f, 20f, 20f, 20f, 20f, 20f, 20f, 20f, 20f, 20f, 20f, 20f, 20f, 20f, 20f)
	private val useAverage = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
	private var maxTick = 20f
	private var maxUse = 0f
	private var sendUsageTicker = 0
	private val dispatchSignals = false
	private val console: NukkitConsole
	private val consoleThread: ConsoleThread
	val commandMap: SimpleCommandMap
	val craftingManager: CraftingManager
	val resourcePackManager: ResourcePackManager

	//todo: use ticker to check console
	val consoleSender: ConsoleCommandSender
	var maxPlayers: Int
	private var autoSave = true
	private var rcon: RCON? = null
	val entityMetadata: EntityMetadataStore
	val playerMetadata: PlayerMetadataStore
	val levelMetadata: LevelMetadataStore
	val network: Network
	private var networkCompressionAsync = true
    var networkCompressionLevel = 7
	private var networkZlibProvider = 0
	private var autoTickRate = true
	private var autoTickRateLimit = 20
	private var alwaysTickPlayers = false
	private var baseTickRate = 1
	private var getAllowFlight: Boolean? = null
	private var difficulty = Int.MAX_VALUE
	var defaultGamemode = Int.MAX_VALUE
		get() {
			if (field == Int.MAX_VALUE) {
				field = gamemode
			}
			return field
		}
		private set
	private var autoSaveTicker = 0
	private var autoSaveTicks = 6000
	val language: BaseLang
	var isLanguageForced = false
	val serverUniqueId: UUID
	val filePath: String
	val dataPath: String
	val pluginPath: String
	private val uniquePlayers: MutableSet<UUID> = HashSet()
	private var queryHandler: QueryHandler? = null
	var queryInformation: QueryRegenerateEvent
		private set
	val properties: Config

	//Revising later...
	val config: Config
	private val players: MutableMap<String, Player> = HashMap()
	private val playerList: MutableMap<UUID, Player> = HashMap()
	private val identifier: MutableMap<Int, String> = HashMap()
	private val parent: MutableMap<Int, Level> = mutableMapOf()
	private val levels: MutableMap<Int, Level> = object : MutableMap<Int, Level> by parent {
		override fun put(key: Int, value: Level): Level? {
			val result = parent.put(key, value)
			levelArray = values.toTypedArray()
			return result
		}

		override fun remove(key: Int, value: Level): Boolean {
			val result = parent.remove(key, value)
			levelArray = this.values.toTypedArray()
			return result
		}

		override fun remove(key: Int): Level? {
			val result = parent.remove(key)
			levelArray = this.values.toTypedArray()
			return result
		}
	}
	private var levelArray = arrayOfNulls<Level>(0)
	val serviceManager: ServiceManager = NKServiceManager()
	var defaultLevel: Level? = null
		set(defaultLevel) {
			if (defaultLevel == null || isLevelLoaded(defaultLevel.folderName) && defaultLevel !== this.defaultLevel) {
				field = defaultLevel
			}
		}
	val isNetherAllowed: Boolean
	private val currentThread: Thread
	private var watchdog: Watchdog? = null
	private var nameLookup: DB? = null
	private var playerDataSerializer: PlayerDataSerializer = DefaultPlayerDataSerializer(this)

	init {
		var predefinedLanguage = predefinedLanguage
		Preconditions.checkState(instance == null, "Already initialized!")
		currentThread = Thread.currentThread() // Saves the current thread instance as a reference, used in Server#isPrimaryThread()
		instance = this
		this.filePath = filePath
		if (!File(dataPath + "worlds/").exists()) {
			File(dataPath + "worlds/").mkdirs()
		}
		if (!File(dataPath + "players/").exists()) {
			File(dataPath + "players/").mkdirs()
		}
		if (!File(pluginPath).exists()) {
			File(pluginPath).mkdirs()
		}
		this.dataPath = File(dataPath).absolutePath + "/"
		this.pluginPath = File(pluginPath).absolutePath + "/"
		console = NukkitConsole(this)
		consoleThread = ConsoleThread()
		consoleThread.start()

		//todo: VersionString 现在不必要
		if (!File(this.dataPath + "nukkit.yml").exists()) {
			logger.info(TextFormat.GREEN.toString() + "Welcome! Please choose a language first!")
			try {
				val languageList = this.javaClass.classLoader.getResourceAsStream("lang/language.list")
						?: throw IllegalStateException("lang/language.list is missing. If you are running a development version, make sure you have run 'git submodule update --init'.")
				val lines = Utils.readFile(languageList).split("\n").toTypedArray()
				for (line in lines) {
					logger.info(line)
				}
			} catch (e: IOException) {
				throw RuntimeException(e)
			}
			val fallback = BaseLang.FALLBACK_LANGUAGE
			var language: String? = null
			while (language == null) {
				var lang: String
				lang = if (predefinedLanguage != null) {
					log.info("Trying to load language from predefined language: $predefinedLanguage")
					predefinedLanguage
				} else {
					console.readLine()
				}
				val conf = this.javaClass.classLoader.getResourceAsStream("lang/$lang/lang.ini")
				if (conf != null) {
					language = lang
				} else if (predefinedLanguage != null) {
					log.warn("No language found for predefined language: $predefinedLanguage, please choose a valid language")
					predefinedLanguage = null
				}
			}
			var advacedConf = this.javaClass.classLoader.getResourceAsStream("lang/$language/nukkit.yml")
			if (advacedConf == null) {
				advacedConf = this.javaClass.classLoader.getResourceAsStream("lang/$fallback/nukkit.yml")
			}
			try {
				Utils.writeFile(this.dataPath + "nukkit.yml", advacedConf)
			} catch (e: IOException) {
				throw RuntimeException(e)
			}
		}
		console.isExecutingCommands = true
		log.info("Loading {} ...", TextFormat.GREEN.toString() + "nukkit.yml" + TextFormat.WHITE)
		config = Config(this.dataPath + "nukkit.yml", Config.YAML)
		log.info("Loading {} ...", TextFormat.GREEN.toString() + "server.properties" + TextFormat.WHITE)
		properties = Config(this.dataPath + "server.properties", Config.PROPERTIES, object : ConfigSection() {
			init {
				put("motd", "A Nukkit Powered Server")
				put("sub-motd", "https://nukkitx.com")
				put("server-port", 19132)
				put("server-ip", "0.0.0.0")
				put("view-distance", 10)
				put("white-list", false)
				put("achievements", true)
				put("announce-player-achievements", true)
				put("spawn-protection", 16)
				put("max-players", 20)
				put("allow-flight", false)
				put("spawn-animals", true)
				put("spawn-mobs", true)
				put("gamemode", 0)
				put("force-gamemode", false)
				put("hardcore", false)
				put("pvp", true)
				put("difficulty", 1)
				put("generator-settings", "")
				put("level-name", "world")
				put("level-seed", "")
				put("level-type", "DEFAULT")
				put("allow-nether", true)
				put("enable-query", true)
				put("enable-rcon", false)
				put("rcon.password", Base64.getEncoder().encodeToString(UUID.randomUUID().toString().replace("-", "").toByteArray()).substring(3, 13))
				put("auto-save", true)
				put("force-resources", false)
				put("xbox-auth", true)
			}
		})

		// Allow Nether? (determines if we create a nether world if one doesn't exist on startup)
		isNetherAllowed = properties.getBoolean("allow-nether", true)
		isLanguageForced = this.getConfig("settings.force-language", false)!!
		language = BaseLang(this.getConfig("settings.language", BaseLang.FALLBACK_LANGUAGE))
		log.info(language.translateString("language.selected", *arrayOf(language.name, language.lang)))
		log.info(language.translateString("nukkit.server.start", TextFormat.AQUA.toString() + version + TextFormat.RESET))
		var poolSize = this.getConfig("settings.async-workers", "auto" as Any)!!
		if (poolSize !is Int) {
			poolSize = try {
				Integer.valueOf(poolSize as String)
			} catch (e: Exception) {
				Math.max(Runtime.getRuntime().availableProcessors() + 1, 4)
			}
		}
		ServerScheduler.WORKERS = poolSize as Int
		networkZlibProvider = this.getConfig("network.zlib-provider", 2)!!
		Zlib.setProvider(networkZlibProvider)
		networkCompressionLevel = this.getConfig("network.compression-level", 7)!!
		networkCompressionAsync = this.getConfig("network.async-compression", true)!!
		autoTickRate = this.getConfig("level-settings.auto-tick-rate", true)!!
		autoTickRateLimit = this.getConfig("level-settings.auto-tick-rate-limit", 20)!!
		alwaysTickPlayers = this.getConfig("level-settings.always-tick-players", false)!!
		baseTickRate = this.getConfig("level-settings.base-tick-rate", 1)!!
		scheduler = ServerScheduler()
		if (this.getPropertyBoolean("enable-rcon", false)) {
			try {
				rcon = RCON(this, this.getPropertyString("rcon.password", ""), if (ip != "") ip else "0.0.0.0", this.getPropertyInt("rcon.port", port))
			} catch (e: IllegalArgumentException) {
				log.error(language.translateString(e.message, e.cause!!.message))
			}
		}
		entityMetadata = EntityMetadataStore()
		playerMetadata = PlayerMetadataStore()
		levelMetadata = LevelMetadataStore()
		ops = Config(this.dataPath + "ops.txt", Config.ENUM)
		whitelist = Config(this.dataPath + "white-list.txt", Config.ENUM)
		nameBans = BanList(this.dataPath + "banned-players.json")
		nameBans.load()
		iPBans = BanList(this.dataPath + "banned-ips.json")
		iPBans.load()
		maxPlayers = this.getPropertyInt("max-players", 20)
		setAutoSave(this.getPropertyBoolean("auto-save", true))
		if (this.getPropertyBoolean("hardcore", false) && getDifficulty() < 3) {
			setPropertyInt("difficulty", 3)
		}
		Nukkit.DEBUG = NukkitMath.clamp(this.getConfig("debug.level", 1)!!, 1, 3)
		val logLevel = (Nukkit.DEBUG + 3) * 100
		val currentLevel = Nukkit.getLogLevel()
		for (level in org.apache.logging.log4j.Level.values()) {
			if (level.intLevel() == logLevel && level.intLevel() > currentLevel.intLevel()) {
				Nukkit.setLogLevel(level)
				break
			}
		}
		val bugReport: Boolean
		if (config.exists("settings.bug-report")) {
			bugReport = config.getBoolean("settings.bug-report")
			properties.remove("bug-report")
		} else {
			bugReport = this.getPropertyBoolean("bug-report", true) //backwards compat
		}
		if (bugReport) {
			ExceptionHandler.registerExceptionHandler()
		}
		log.info(language.translateString("nukkit.server.networkStart", *arrayOf(if (ip == "") "*" else ip, port.toString())))
		serverUniqueId = UUID.randomUUID()
		network = Network(this)
		network.name = motd
		network.subName = subMotd
		log.info(language.translateString("nukkit.server.info", name, TextFormat.YELLOW.toString() + nukkitVersion + TextFormat.WHITE, TextFormat.AQUA.toString() + codename + TextFormat.WHITE, apiVersion))
		log.info(language.translateString("nukkit.server.license", name))
		consoleSender = ConsoleCommandSender()
		commandMap = SimpleCommandMap(this)
		registerEntities()
		registerBlockEntities()
		Block.init()
		Enchantment.init()
		Item.init()
		EnumBiome.values() //load class, this also registers biomes
		Effect.init()
		Potion.init()
		Attribute.init()
		GlobalBlockPalette.getOrCreateRuntimeId(0, 0) //Force it to load

		// Convert legacy data before plugins get the chance to mess with it.
		nameLookup = try {
			Iq80DBFactory.factory.open(File(dataPath, "players"), Options()
					.createIfMissing(true)
					.compressionType(CompressionType.ZLIB_RAW))
		} catch (e: IOException) {
			throw RuntimeException(e)
		}
		convertLegacyPlayerData()
		craftingManager = CraftingManager()
		resourcePackManager = ResourcePackManager(File(Nukkit.DATA_PATH, "resource_packs"))
		pluginManager = PluginManager(this, commandMap)
		pluginManager.subscribeToPermission(BROADCAST_CHANNEL_ADMINISTRATIVE, consoleSender)
		pluginManager.registerInterface(JavaPluginLoader::class.java)
		queryInformation = QueryRegenerateEvent(this, 5)
		network.registerInterface(RakNetInterface(this))
		pluginManager.loadPlugins(this.pluginPath)
		enablePlugins(PluginLoadOrder.STARTUP)
		LevelProviderManager.addProvider(this, Anvil::class.java)
		LevelProviderManager.addProvider(this, McRegion::class.java)
		LevelProviderManager.addProvider(this, LevelDB::class.java)
		Generator.addGenerator(Flat::class.java, "flat", Generator.TYPE_FLAT)
		Generator.addGenerator(Normal::class.java, "normal", Generator.TYPE_INFINITE)
		Generator.addGenerator(Normal::class.java, "default", Generator.TYPE_INFINITE)
		Generator.addGenerator(Nether::class.java, "nether", Generator.TYPE_NETHER)
		//todo: add old generator and hell generator
		for (name in this.getConfig("worlds", HashMap<String, Any>())!!.keys) {
			if (!loadLevel(name)) {
				var seed: Long
				seed = try {
					(this.getConfig<Any>("worlds.$name.seed") as Int).toLong()
				} catch (e: Exception) {
					System.currentTimeMillis()
				}
				val options: MutableMap<String?, Any?> = HashMap()
				val opts = this.getConfig("worlds.$name.generator", Generator.getGenerator("default").simpleName)!!.split(":").toTypedArray()
				val generator = Generator.getGenerator(opts[0])
				if (opts.size > 1) {
					var preset = ""
					for (i in 1 until opts.size) {
						preset += opts[i] + ":"
					}
					preset = preset.substring(0, preset.length - 1)
					options["preset"] = preset
				}
				generateLevel(name, seed, generator, options)
			}
		}
		if (defaultLevel == null) {
			var defaultName = this.getPropertyString("level-name", "world")
			if (defaultName == null || defaultName.trim { it <= ' ' }.isEmpty()) {
				logger.warning("level-name cannot be null, using default")
				defaultName = "world"
				setPropertyString("level-name", defaultName)
			}
			if (!loadLevel(defaultName)) {
				val seed: Long
				val seedString = this.getProperty("level-seed", System.currentTimeMillis()).toString()
				seed = try {
					java.lang.Long.valueOf(seedString)
				} catch (e: NumberFormatException) {
					seedString.hashCode().toLong()
				}
				generateLevel(defaultName, if (seed == 0L) System.currentTimeMillis() else seed)
			}
			defaultLevel = getLevelByName(defaultName)
		}
		properties.save(true)
		if (defaultLevel == null) {
			logger.emergency(language.translateString("nukkit.level.defaultError"))
			forceShutdown()
			return
		}
		EnumLevel.initLevels()
		if (this.getConfig("ticks-per.autosave", 6000)!! > 0) {
			autoSaveTicks = this.getConfig("ticks-per.autosave", 6000)!!
		}
		enablePlugins(PluginLoadOrder.POSTWORLD)
		if (Nukkit.DEBUG < 2) {
			watchdog = Watchdog(this, 60000)
			watchdog.start()
		}
		start()
	}

	fun broadcastMessage(message: String?): Int {
		return this.broadcast(message, BROADCAST_CHANNEL_USERS)
	}

	fun broadcastMessage(message: TextContainer?): Int {
		return this.broadcast(message, BROADCAST_CHANNEL_USERS)
	}

	fun broadcastMessage(message: String?, recipients: Array<CommandSender>): Int {
		for (recipient in recipients) {
			recipient.sendMessage(message)
		}
		return recipients.size
	}

	fun broadcastMessage(message: String?, recipients: Collection<CommandSender>): Int {
		for (recipient in recipients) {
			recipient.sendMessage(message)
		}
		return recipients.size
	}

	fun broadcastMessage(message: TextContainer?, recipients: Collection<CommandSender>): Int {
		for (recipient in recipients) {
			recipient.sendMessage(message)
		}
		return recipients.size
	}

	fun broadcast(message: String?, permissions: String): Int {
		val recipients: MutableSet<CommandSender> = HashSet()
		for (permission in permissions.split(";").toTypedArray()) {
			for (permissible in pluginManager.getPermissionSubscriptions(permission)) {
				if (permissible is CommandSender && permissible.hasPermission(permission)) {
					recipients.add(permissible)
				}
			}
		}
		for (recipient in recipients) {
			recipient.sendMessage(message)
		}
		return recipients.size
	}

	fun broadcast(message: TextContainer?, permissions: String): Int {
		val recipients: MutableSet<CommandSender> = HashSet()
		for (permission in permissions.split(";").toTypedArray()) {
			for (permissible in pluginManager.getPermissionSubscriptions(permission)) {
				if (permissible is CommandSender && permissible.hasPermission(permission)) {
					recipients.add(permissible)
				}
			}
		}
		for (recipient in recipients) {
			recipient.sendMessage(message)
		}
		return recipients.size
	}

	@JvmOverloads
	fun batchPackets(players: Array<Player>?, packets: Array<DataPacket?>?, forceSync: Boolean = false) {
		if (players == null || packets == null || players.size == 0 || packets.size == 0) {
			return
		}
		val ev = BatchPacketsEvent(players, packets, forceSync)
		pluginManager.callEvent(ev)
		if (ev.isCancelled) {
			return
		}
		Timings.playerNetworkSendTimer.startTiming()
		val payload = arrayOfNulls<ByteArray>(packets.size * 2)
		var size = 0
		for (i in packets.indices) {
			val p = packets[i]
			if (!p!!.isEncoded) {
				p.encode()
			}
			val buf = p.buffer
			payload[i * 2] = Binary.writeUnsignedVarInt(buf.size.toLong())
			payload[i * 2 + 1] = buf
			packets[i] = null
			size += payload[i * 2].length
			size += payload[i * 2 + 1].length
		}
		val targets: MutableList<String?> = ArrayList()
		for (p in players) {
			if (p.isConnected) {
				targets.add(identifier[p.rawHashCode()])
			}
		}
		if (!forceSync && networkCompressionAsync) {
			scheduler.scheduleAsyncTask(CompressBatchedTask(payload, targets, networkCompressionLevel))
		} else {
			try {
				val data = Binary.appendBytes(payload)
				broadcastPacketsCallback(Zlib.deflate(data, networkCompressionLevel), targets)
			} catch (e: Exception) {
				throw RuntimeException(e)
			}
		}
		Timings.playerNetworkSendTimer.stopTiming()
	}

	fun broadcastPacketsCallback(data: ByteArray?, identifiers: List<String?>) {
		val pk = BatchPacket()
		pk.payload = data
		for (i in identifiers) {
			if (players.containsKey(i)) {
				players[i]!!.dataPacket(pk)
			}
		}
	}

	fun enablePlugins(type: PluginLoadOrder) {
		for (plugin in ArrayList(pluginManager.plugins.values)) {
			if (!plugin.isEnabled && type == plugin.description.order) {
				enablePlugin(plugin)
			}
		}
		if (type == PluginLoadOrder.POSTWORLD) {
			commandMap.registerServerAliases()
			DefaultPermissions.registerCorePermissions()
		}
	}

	fun enablePlugin(plugin: Plugin?) {
		pluginManager.enablePlugin(plugin)
	}

	fun disablePlugins() {
		pluginManager.disablePlugins()
	}

	@Throws(ServerException::class)
	fun dispatchCommand(sender: CommandSender?, commandLine: String): Boolean {
		// First we need to check if this command is on the main thread or not, if not, warn the user
		if (!isPrimaryThread) {
			logger.warning("Command Dispatched Async: $commandLine")
			logger.warning("Please notify author of plugin causing this execution to fix this bug!", Throwable())
			// TODO: We should sync the command to the main thread too!
		}
		if (sender == null) {
			throw ServerException("CommandSender is not valid")
		}
		if (commandMap.dispatch(sender, commandLine)) {
			return true
		}
		sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.unknown", commandLine))
		return false
	}

	fun reload() {
		log.info("Reloading...")
		log.info("Saving levels...")
		for (level in levelArray) {
			level!!.save()
		}
		pluginManager.disablePlugins()
		pluginManager.clearPlugins()
		commandMap.clearCommands()
		log.info("Reloading properties...")
		properties.reload()
		maxPlayers = this.getPropertyInt("max-players", 20)
		if (this.getPropertyBoolean("hardcore", false) && getDifficulty() < 3) {
			setPropertyInt("difficulty", 3.also { difficulty = it })
		}
		iPBans.load()
		nameBans.load()
		reloadWhitelist()
		ops.reload()
		for (entry in iPBans.entires.values) {
			network.blockAddress(entry.name, -1)
		}
		pluginManager.registerInterface(JavaPluginLoader::class.java)
		pluginManager.loadPlugins(pluginPath)
		enablePlugins(PluginLoadOrder.STARTUP)
		enablePlugins(PluginLoadOrder.POSTWORLD)
		Timings.reset()
	}

	fun shutdown() {
		isRunning.compareAndSet(true, false)
	}

	fun forceShutdown() {
		if (hasStopped) {
			return
		}
		try {
			isRunning.compareAndSet(true, false)
			hasStopped = true
			if (rcon != null) {
				rcon.close()
			}
			for (player in ArrayList(players.values)) {
				player.close(player.leaveMessage, this.getConfig("settings.shutdown-message", "Server closed"))
			}
			logger.debug("Disabling all plugins")
			pluginManager.disablePlugins()
			logger.debug("Removing event handlers")
			HandlerList.unregisterAll()
			logger.debug("Stopping all tasks")
			scheduler.cancelAllTasks()
			scheduler.mainThreadHeartbeat(Int.MAX_VALUE)
			logger.debug("Unloading all levels")
			for (level in levelArray) {
				unloadLevel(level, true)
			}
			logger.debug("Closing console")
			consoleThread.interrupt()
			logger.debug("Stopping network interfaces")
			for (interfaz in network.interfaces) {
				interfaz.shutdown()
				network.unregisterInterface(interfaz)
			}
			if (nameLookup != null) {
				nameLookup.close()
			}
			logger.debug("Disabling timings")
			Timings.stopServer()
			if (watchdog != null) {
				watchdog.kill()
			}
			//todo other things
		} catch (e: Exception) {
			log.fatal("Exception happened while shutting down, exiting the process", e)
			System.exit(1)
		}
	}

	fun start() {
		if (this.getPropertyBoolean("enable-query", true)) {
			queryHandler = QueryHandler()
		}
		for (entry in iPBans.entires.values) {
			network.blockAddress(entry.name, -1)
		}

		//todo send usage setting
		tick = 0
		log.info(language.translateString("nukkit.server.defaultGameMode", getGamemodeString(gamemode)))
		log.info(language.translateString("nukkit.server.startFinished", ((System.currentTimeMillis() - Nukkit.START_TIME).toDouble() / 1000).toString()))
		tickProcessor()
		forceShutdown()
	}

	fun handlePacket(address: String?, port: Int, payload: ByteArray) {
		try {
			if (payload.size > 2 && Arrays.equals(Binary.subBytes(payload, 0, 2), byteArrayOf(0xfe.toByte(), 0xfd.toByte())) && queryHandler != null) {
				queryHandler!!.handle(address, port, payload)
			}
		} catch (e: Exception) {
			log.error("Error whilst handling packet", e)
			network.blockAddress(address, 600)
		}
	}

	private var lastLevelGC = 0
	fun tickProcessor() {
		nextTick = System.currentTimeMillis()
		try {
			while (isRunning.get()) {
				try {
					tick()
					val next = nextTick
					val current = System.currentTimeMillis()
					if (next - 0.1 > current) {
						var allocated = next - current - 1
						run {
							// Instead of wasting time, do something potentially useful
							var offset = 0
							for (i in levelArray.indices) {
								offset = (i + lastLevelGC) % levelArray.size
								val level = levelArray[offset]
								level!!.doGarbageCollection(allocated - 1)
								allocated = next - System.currentTimeMillis()
								if (allocated <= 0) {
									break
								}
							}
							lastLevelGC = offset + 1
						}
						if (allocated > 0) {
							Thread.sleep(allocated, 900000)
						}
					}
				} catch (e: RuntimeException) {
					logger.logException(e)
				}
			}
		} catch (e: Throwable) {
			log.fatal("Exception happened while ticking server", e)
			log.fatal(Utils.getAllThreadDumps())
		}
	}

	fun onPlayerCompleteLoginSequence(player: Player) {
		sendFullPlayerListData(player)
	}

	fun onPlayerLogin(player: Player) {
		if (sendUsageTicker > 0) {
			uniquePlayers.add(player.uniqueId)
		}
	}

	fun addPlayer(identifier: String, player: Player) {
		players[identifier] = player
		this.identifier[player.rawHashCode()] = identifier
	}

	fun addOnlinePlayer(player: Player) {
		playerList[player.uniqueId] = player
		this.updatePlayerListData(player.uniqueId, player.id, player.getDisplayName(), player.skin, player.loginChainData.xuid)
	}

	fun removeOnlinePlayer(player: Player) {
		if (playerList.containsKey(player.uniqueId)) {
			playerList.remove(player.uniqueId)
			val pk = PlayerListPacket()
			pk.type = PlayerListPacket.TYPE_REMOVE
			pk.entries = arrayOf(PlayerListPacket.Entry(player.uniqueId))
			broadcastPacket(playerList.values, pk)
		}
	}

	fun updatePlayerListData(uuid: UUID?, entityId: Long, name: String?, skin: Skin?, players: Array<Player>) {
		this.updatePlayerListData(uuid, entityId, name, skin, "", players)
	}

	fun updatePlayerListData(uuid: UUID?, entityId: Long, name: String?, skin: Skin?, xboxUserId: String?, players: Array<Player>) {
		val pk = PlayerListPacket()
		pk.type = PlayerListPacket.TYPE_ADD
		pk.entries = arrayOf(PlayerListPacket.Entry(uuid, entityId, name, skin, xboxUserId))
		broadcastPacket(players, pk)
	}

	@JvmOverloads
	fun updatePlayerListData(uuid: UUID, entityId: Long, name: String?, skin: Skin?, xboxUserId: String? = "", players: Collection<Player> = playerList.values) {
		this.updatePlayerListData(uuid, entityId, name, skin, xboxUserId,
				players.stream()
						.filter { p: Player -> p.uniqueId != uuid }
						.toArray { _Dummy_.__Array__() })
	}

	fun removePlayerListData(uuid: UUID?, players: Array<Player>) {
		val pk = PlayerListPacket()
		pk.type = PlayerListPacket.TYPE_REMOVE
		pk.entries = arrayOf(PlayerListPacket.Entry(uuid))
		broadcastPacket(players, pk)
	}

	@JvmOverloads
	fun removePlayerListData(uuid: UUID?, players: Collection<Player> = playerList.values) {
		this.removePlayerListData(uuid, players.toTypedArray())
	}

	fun sendFullPlayerListData(player: Player) {
		val pk = PlayerListPacket()
		pk.type = PlayerListPacket.TYPE_ADD
		pk.entries = playerList.values.stream()
				.map { p: Player ->
					PlayerListPacket.Entry(
							p.uniqueId,
							p.id,
							p.getDisplayName(),
							p.skin,
							p.loginChainData.xuid)
				}
				.toArray { _Dummy_.__Array__() }
		player.dataPacket(pk)
	}

	fun sendRecipeList(player: Player) {
		player.dataPacket(CraftingManager.packet)
	}

	private fun checkTickUpdates(currentTick: Int, tickTime: Long) {
		for (p in ArrayList(players.values)) {
			/*if (!p.loggedIn && (tickTime - p.creationTime) >= 10000 && p.kick(PlayerKickEvent.Reason.LOGIN_TIMEOUT, "Login timeout")) {
                continue;
            }

            client freezes when applying resource packs
            todo: fix*/
			if (alwaysTickPlayers) {
				p.onUpdate(currentTick)
			}
		}

		//Do level ticks
		for (level in levelArray) {
			if (level!!.tickRate > baseTickRate && --level.tickRateCounter > 0) {
				continue
			}
			try {
				val levelTime = System.currentTimeMillis()
				level.doTick(currentTick)
				val tickMs = (System.currentTimeMillis() - levelTime).toInt()
				level.tickRateTime = tickMs
				if (autoTickRate) {
					if (tickMs < 50 && level.tickRate > baseTickRate) {
						var r: Int
						level.tickRate = level.tickRate - 1.also { r = it }
						if (r > baseTickRate) {
							level.tickRateCounter = level.tickRate
						}
						logger.debug("Raising level \"" + level.name + "\" tick rate to " + level.tickRate + " ticks")
					} else if (tickMs >= 50) {
						if (level.tickRate == baseTickRate) {
							level.tickRate = Math.max(baseTickRate + 1, Math.min(autoTickRateLimit, tickMs / 50))
							logger.debug("Level \"" + level.name + "\" took " + NukkitMath.round(tickMs.toDouble(), 2) + "ms, setting tick rate to " + level.tickRate + " ticks")
						} else if (tickMs / level.tickRate >= 50 && level.tickRate < autoTickRateLimit) {
							level.tickRate = level.tickRate + 1
							logger.debug("Level \"" + level.name + "\" took " + NukkitMath.round(tickMs.toDouble(), 2) + "ms, setting tick rate to " + level.tickRate + " ticks")
						}
						level.tickRateCounter = level.tickRate
					}
				}
			} catch (e: Exception) {
				log.error(language.translateString("nukkit.level.tickError",
						*arrayOf(level.folderName, Utils.getExceptionMessage(e))))
			}
		}
	}

	fun doAutoSave() {
		if (getAutoSave()) {
			Timings.levelSaveTimer.startTiming()
			for (player in ArrayList(players.values)) {
				if (player.isOnline) {
					player.save(true)
				} else if (!player.isConnected) {
					removePlayer(player)
				}
			}
			for (level in levelArray) {
				level!!.save()
			}
			Timings.levelSaveTimer.stopTiming()
		}
	}

	private fun tick(): Boolean {
		val tickTime = System.currentTimeMillis()

		// TODO
		val time = tickTime - nextTick
		if (time < -25) {
			try {
				Thread.sleep(Math.max(5, -time - 25))
			} catch (e: InterruptedException) {
				instance!!.logger.logException(e)
			}
		}
		val tickTimeNano = System.nanoTime()
		if (tickTime - nextTick < -25) {
			return false
		}
		Timings.fullServerTickTimer.startTiming()
		++tick
		Timings.connectionTimer.startTiming()
		network.processInterfaces()
		if (rcon != null) {
			rcon.check()
		}
		Timings.connectionTimer.stopTiming()
		Timings.schedulerTimer.startTiming()
		scheduler.mainThreadHeartbeat(tick)
		Timings.schedulerTimer.stopTiming()
		checkTickUpdates(tick, tickTime)
		for (player in ArrayList(players.values)) {
			player.checkNetwork()
		}
		if (tick and 15 == 0) {
			titleTick()
			network.resetStatistics()
			maxTick = 20f
			maxUse = 0f
			if (tick and 511 == 0) {
				try {
					pluginManager.callEvent(QueryRegenerateEvent(this, 5).also { queryInformation = it })
					if (queryHandler != null) {
						queryHandler!!.regenerateInfo()
					}
				} catch (e: Exception) {
					log.error(e)
				}
			}
			network.updateName()
		}
		if (autoSave && ++autoSaveTicker >= autoSaveTicks) {
			autoSaveTicker = 0
			doAutoSave()
		}
		if (sendUsageTicker > 0 && --sendUsageTicker == 0) {
			sendUsageTicker = 6000
			//todo sendUsage
		}
		if (tick % 100 == 0) {
			for (level in levelArray) {
				level!!.doChunkGarbageCollection()
			}
		}
		Timings.fullServerTickTimer.stopTiming()
		//long now = System.currentTimeMillis();
		val nowNano = System.nanoTime()
		//float tick = Math.min(20, 1000 / Math.max(1, now - tickTime));
		//float use = Math.min(1, (now - tickTime) / 50);
		val tick = Math.min(20.0, 1000000000 / Math.max(1000000.0, nowNano.toDouble() - tickTimeNano)).toFloat()
		val use = Math.min(1.0, (nowNano - tickTimeNano) as Double / 50000000).toFloat()
		if (maxTick > tick) {
			maxTick = tick
		}
		if (maxUse < use) {
			maxUse = use
		}
		System.arraycopy(tickAverage, 1, tickAverage, 0, tickAverage.size - 1)
		tickAverage[tickAverage.size - 1] = tick
		System.arraycopy(useAverage, 1, useAverage, 0, useAverage.size - 1)
		useAverage[useAverage.size - 1] = use
		if (nextTick - tickTime < -1000) {
			nextTick = tickTime
		} else {
			nextTick += 50
		}
		return true
	}

	// TODO: Fix title tick
	fun titleTick() {
		if (!Nukkit.ANSI || !Nukkit.TITLE) {
			return
		}
		val runtime = Runtime.getRuntime()
		val used = NukkitMath.round((runtime.totalMemory() - runtime.freeMemory()).toDouble() / 1024 / 1024, 2)
		val max = NukkitMath.round(runtime.maxMemory().toDouble() / 1024 / 1024, 2)
		val usage = Math.round(used / max * 100).toString() + "%"
		var title: String = (0x1b as Char.toString() + "]0;" + name + " "
		+nukkitVersion
		+" | Online " + players.size + "/" + maxPlayers
		+" | Memory " + usage)
		if (!Nukkit.shortTitle) {
			title += (" | U " + NukkitMath.round(network.upload / 1024 * 1000, 2)
					+ " D " + NukkitMath.round(network.download / 1024 * 1000, 2) + " kB/s")
		}
		title += (" | TPS " + ticksPerSecond
				+ " | Load " + tickUsage + "%" + 0x07.toChar())
		print(title)
	}

	val name: String
		get() = "Nukkit"

	fun isRunning(): Boolean {
		return isRunning.get()
	}

	val nukkitVersion: String
		get() = Nukkit.VERSION

	val codename: String
		get() = Nukkit.CODENAME

	val version: String
		get() = ProtocolInfo.MINECRAFT_VERSION

	val apiVersion: String
		get() = Nukkit.API_VERSION

	val port: Int
		get() = this.getPropertyInt("server-port", 19132)

	val viewDistance: Int
		get() = this.getPropertyInt("view-distance", 10)

	val ip: String
		get() = this.getPropertyString("server-ip", "0.0.0.0")

	fun getAutoSave(): Boolean {
		return autoSave
	}

	fun setAutoSave(autoSave: Boolean) {
		this.autoSave = autoSave
		for (level in levelArray) {
			level!!.autoSave = this.autoSave
		}
	}

	val levelType: String
		get() = this.getPropertyString("level-type", "DEFAULT")

	val generateStructures: Boolean
		get() = this.getPropertyBoolean("generate-structures", true)

	val gamemode: Int
		get() = try {
			this.getPropertyInt("gamemode", 0) and 3
		} catch (exception: NumberFormatException) {
			getGamemodeFromString(this.getPropertyString("gamemode")) and 3
		}

	val forceGamemode: Boolean
		get() = this.getPropertyBoolean("force-gamemode", false)

	fun getDifficulty(): Int {
		if (difficulty == Int.MAX_VALUE) {
			difficulty = this.getPropertyInt("difficulty", 1)
		}
		return difficulty
	}

	fun hasWhitelist(): Boolean {
		return this.getPropertyBoolean("white-list", false)
	}

	val spawnRadius: Int
		get() = this.getPropertyInt("spawn-protection", 16)

	val allowFlight: Boolean
		get() {
			if (getAllowFlight == null) {
				getAllowFlight = this.getPropertyBoolean("allow-flight", false)
			}
			return getAllowFlight!!
		}

	val isHardcore: Boolean
		get() = this.getPropertyBoolean("hardcore", false)

	val motd: String
		get() = this.getPropertyString("motd", "A Nukkit Powered Server")

	val subMotd: String
		get() = this.getPropertyString("sub-motd", "https://nukkitx.com")

	val forceResources: Boolean
		get() = this.getPropertyBoolean("force-resources", false)

	val logger: MainLogger
		get() = MainLogger.getLogger()

	val ticksPerSecond: Float
		get() = Math.round(maxTick * 100).toFloat() / 100

	val ticksPerSecondAverage: Float
		get() {
			var sum = 0f
			val count = tickAverage.size
			for (aTickAverage in tickAverage) {
				sum += aTickAverage
			}
			return NukkitMath.round(sum / count.toDouble(), 2).toFloat()
		}

	val tickUsage: Float
		get() = NukkitMath.round(maxUse * 100.toDouble(), 2).toFloat()

	val tickUsageAverage: Float
		get() {
			var sum = 0f
			val count = useAverage.size
			for (aUseAverage in useAverage) {
				sum += aUseAverage
			}
			return Math.round(sum / count * 100).toFloat() / 100
		}

	val onlinePlayers: Map<UUID, Player>
		get() = ImmutableMap.copyOf(playerList)

	fun addRecipe(recipe: Recipe?) {
		craftingManager.registerRecipe(recipe)
	}

	fun getPlayer(uuid: UUID): Optional<Player> {
		Preconditions.checkNotNull(uuid, "uuid")
		return Optional.ofNullable(playerList[uuid])
	}

	fun lookupName(name: String): Optional<UUID> {
		val nameBytes = name.toLowerCase().toByteArray(StandardCharsets.UTF_8)
		val uuidBytes = nameLookup!![nameBytes] ?: return Optional.empty()
		if (uuidBytes.size != 16) {
			log.warn("Invalid uuid in name lookup database detected! Removing")
			nameLookup!!.delete(nameBytes)
			return Optional.empty()
		}
		val buffer = ByteBuffer.wrap(uuidBytes)
		return Optional.of(UUID(buffer.long, buffer.long))
	}

	fun updateName(uuid: UUID, name: String) {
		val nameBytes = name.toLowerCase().toByteArray(StandardCharsets.UTF_8)
		val buffer = ByteBuffer.allocate(16)
		buffer.putLong(uuid.mostSignificantBits)
		buffer.putLong(uuid.leastSignificantBits)
		nameLookup!!.put(nameBytes, buffer.array())
	}

	@Deprecated("")
	fun getOfflinePlayer(name: String): IPlayer {
		val result: IPlayer? = getPlayerExact(name.toLowerCase())
		return result ?: lookupName(name).map { uuid: UUID? -> OfflinePlayer(this, uuid) }
				.orElse(OfflinePlayer(this, name))
	}

	fun getOfflinePlayer(uuid: UUID): IPlayer {
		Preconditions.checkNotNull(uuid, "uuid")
		val onlinePlayer = getPlayer(uuid)
		return if (onlinePlayer.isPresent) {
			onlinePlayer.get()
		} else OfflinePlayer(this, uuid)
	}

	fun getOfflinePlayerData(uuid: UUID): CompoundTag? {
		return getOfflinePlayerData(uuid, false)
	}

	fun getOfflinePlayerData(uuid: UUID, create: Boolean): CompoundTag? {
		return getOfflinePlayerDataInternal(uuid.toString(), true, create)
	}

	@Deprecated("")
	fun getOfflinePlayerData(name: String): CompoundTag? {
		return getOfflinePlayerData(name, false)
	}

	@Deprecated("")
	fun getOfflinePlayerData(name: String, create: Boolean): CompoundTag? {
		val uuid = lookupName(name)
		return getOfflinePlayerDataInternal(uuid.map { obj: UUID -> obj.toString() }.orElse(name), true, create)
	}

	private fun getOfflinePlayerDataInternal(name: String, runEvent: Boolean, create: Boolean): CompoundTag? {
		Preconditions.checkNotNull(name, "name")
		val event = PlayerDataSerializeEvent(name, playerDataSerializer)
		if (runEvent) {
			pluginManager.callEvent(event)
		}
		var dataStream = Optional.empty<InputStream>()
		try {
			dataStream = event.serializer.read(name, event.uuid.orElse(null))
			if (dataStream.isPresent) {
				return NBTIO.readCompressed(dataStream.get())
			}
		} catch (e: IOException) {
			log.warn(language.translateString("nukkit.data.playerCorrupted", name))
			log.throwing(e)
		} finally {
			if (dataStream.isPresent) {
				try {
					dataStream.get().close()
				} catch (e: IOException) {
					log.throwing(e)
				}
			}
		}
		var nbt: CompoundTag? = null
		if (create) {
			if (shouldSavePlayerData()) {
				log.info(language.translateString("nukkit.data.playerNotFound", name))
			}
			val spawn = defaultLevel!!.safeSpawn
			nbt = CompoundTag()
					.putLong("firstPlayed", System.currentTimeMillis() / 1000)
					.putLong("lastPlayed", System.currentTimeMillis() / 1000)
					.putList(ListTag<DoubleTag>("Pos")
							.add(DoubleTag("0", spawn.x))
							.add(DoubleTag("1", spawn.y))
							.add(DoubleTag("2", spawn.z)))
					.putString("Level", defaultLevel!!.name)
					.putList(ListTag("Inventory"))
					.putCompound("Achievements", CompoundTag())
					.putInt("playerGameType", gamemode)
					.putList(ListTag<DoubleTag>("Motion")
							.add(DoubleTag("0", 0))
							.add(DoubleTag("1", 0))
							.add(DoubleTag("2", 0)))
					.putList(ListTag<FloatTag>("Rotation")
							.add(FloatTag("0", 0))
							.add(FloatTag("1", 0)))
					.putFloat("FallDistance", 0f)
					.putShort("Fire", 0)
					.putShort("Air", 300)
					.putBoolean("OnGround", true)
					.putBoolean("Invulnerable", false)
			this.saveOfflinePlayerData(name, nbt, true, runEvent)
		}
		return nbt
	}

	@JvmOverloads
	fun saveOfflinePlayerData(uuid: UUID, tag: CompoundTag?, async: Boolean = false) {
		this.saveOfflinePlayerData(uuid.toString(), tag, async)
	}

	@JvmOverloads
	fun saveOfflinePlayerData(name: String, tag: CompoundTag?, async: Boolean = false) {
		val uuid = lookupName(name)
		saveOfflinePlayerData(uuid.map { obj: UUID -> obj.toString() }.orElse(name), tag, async, true)
	}

	private fun saveOfflinePlayerData(name: String, tag: CompoundTag?, async: Boolean, runEvent: Boolean) {
		val nameLower = name.toLowerCase()
		if (shouldSavePlayerData()) {
			val event = PlayerDataSerializeEvent(nameLower, playerDataSerializer)
			if (runEvent) {
				pluginManager.callEvent(event)
			}
			scheduler.scheduleTask(object : Task() {
				var hasRun = false
				override fun onRun(currentTick: Int) {
					onCancel()
				}

				//doing it like this ensures that the playerdata will be saved in a server shutdown
				override fun onCancel() {
					if (!hasRun) {
						hasRun = true
						saveOfflinePlayerDataInternal(event.serializer, tag, nameLower, event.uuid.orElse(null))
					}
				}
			}, async)
		}
	}

	private fun saveOfflinePlayerDataInternal(serializer: PlayerDataSerializer, tag: CompoundTag?, name: String, uuid: UUID) {
		try {
			serializer.write(name, uuid).use { dataStream -> NBTIO.writeGZIPCompressed(tag, dataStream, ByteOrder.BIG_ENDIAN) }
		} catch (e: Exception) {
			log.error(language.translateString("nukkit.data.saveError", name, e))
		}
	}

	private fun convertLegacyPlayerData() {
		val dataDirectory = File(dataPath, "players/")
		val uuidPattern = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}.dat$")
		val files = dataDirectory.listFiles { file: File ->
			val name = file.name
			!uuidPattern.matcher(name).matches() && name.endsWith(".dat")
		}
				?: return
		for (legacyData in files) {
			var name = legacyData.name
			// Remove file extension
			name = name.substring(0, name.length - 4)
			log.debug("Attempting legacy player data conversion for {}", name)
			val tag = getOfflinePlayerDataInternal(name, false, false)
			if (tag == null || !tag.contains("UUIDLeast") || !tag.contains("UUIDMost")) {
				// No UUID so we cannot convert. Wait until player logs in.
				continue
			}
			val uuid = UUID(tag.getLong("UUIDMost"), tag.getLong("UUIDLeast"))
			if (!tag.contains("NameTag")) {
				tag.putString("NameTag", name)
			}
			if (File(dataPath + "players/" + uuid.toString() + ".dat").exists()) {
				// We don't want to overwrite existing data.
				continue
			}
			saveOfflinePlayerData(uuid.toString(), tag, false, false)

			// Add name to lookup table
			updateName(uuid, name)

			// Delete legacy data
			if (!legacyData.delete()) {
				log.warn("Unable to delete legacy data for {}", name)
			}
		}
	}

	fun getPlayer(name: String): Player? {
		var name = name
		var found: Player? = null
		name = name.toLowerCase()
		var delta = Int.MAX_VALUE
		for (player in onlinePlayers.values) {
			if (player.name.toLowerCase().startsWith(name)) {
				val curDelta = player.name.length - name.length
				if (curDelta < delta) {
					found = player
					delta = curDelta
				}
				if (curDelta == 0) {
					break
				}
			}
		}
		return found
	}

	fun getPlayerExact(name: String): Player? {
		var name = name
		name = name.toLowerCase()
		for (player in onlinePlayers.values) {
			if (player.name.toLowerCase() == name) {
				return player
			}
		}
		return null
	}

	fun matchPlayer(partialName: String): Array<Player> {
		var partialName = partialName
		partialName = partialName.toLowerCase()
		val matchedPlayer: MutableList<Player> = ArrayList()
		for (player in onlinePlayers.values) {
			if (player.name.toLowerCase() == partialName) {
				return arrayOf(player)
			} else if (player.name.toLowerCase().contains(partialName)) {
				matchedPlayer.add(player)
			}
		}
		return matchedPlayer.toTypedArray()
	}

	fun removePlayer(player: Player) {
		if (identifier.containsKey(player.rawHashCode())) {
			val identifier = identifier[player.rawHashCode()]
			players.remove(identifier)
			this.identifier.remove(player.rawHashCode())
			return
		}
		for (identifier in ArrayList(players.keys)) {
			val p = players[identifier]
			if (player === p) {
				players.remove(identifier)
				this.identifier.remove(player.rawHashCode())
				break
			}
		}
	}

	fun getLevels(): Map<Int, Level> {
		return levels
	}

	fun isLevelLoaded(name: String?): Boolean {
		return getLevelByName(name) != null
	}

	fun getLevel(levelId: Int): Level? {
		return if (levels.containsKey(levelId)) {
			levels[levelId]
		} else null
	}

	fun getLevelByName(name: String?): Level? {
		for (level in levelArray) {
			if (level!!.folderName.equals(name, ignoreCase = true)) {
				return level
			}
		}
		return null
	}

	@JvmOverloads
	fun unloadLevel(level: Level?, forceUnload: Boolean = false): Boolean {
		check(!(level === defaultLevel && !forceUnload)) { "The default level cannot be unloaded while running, please switch levels." }
		return level!!.unload(forceUnload)
	}

	fun loadLevel(name: String): Boolean {
		if (name.trim { it <= ' ' } == "") {
			throw LevelException("Invalid empty level name")
		}
		if (isLevelLoaded(name)) {
			return true
		} else if (!isLevelGenerated(name)) {
			log.warn(language.translateString("nukkit.level.notFound", name))
			return false
		}
		val path: String
		path = if (name.contains("/") || name.contains("\\")) {
			name
		} else {
			dataPath + "worlds/" + name + "/"
		}
		val provider = LevelProviderManager.getProvider(path)
		if (provider == null) {
			log.error(language.translateString("nukkit.level.loadError", *arrayOf(name, "Unknown provider")))
			return false
		}
		val level: Level
		level = try {
			Level(this, name, path, provider)
		} catch (e: Exception) {
			log.error(language.translateString("nukkit.level.loadError", *arrayOf(name, e.message)))
			return false
		}
		levels[level.id] = level
		level.initLevel()
		pluginManager.callEvent(LevelLoadEvent(level))
		level.tickRate = baseTickRate
		return true
	}

	@JvmOverloads
	fun generateLevel(name: String, seed: Long = Random().nextLong(), generator: Class<out Generator?>? = null, options: MutableMap<String?, Any?> = HashMap(), provider: Class<out LevelProvider?>? = null): Boolean {
		var generator = generator
		var provider = provider
		if (name.trim { it <= ' ' } == "" || isLevelGenerated(name)) {
			return false
		}
		if (!options.containsKey("preset")) {
			options["preset"] = this.getPropertyString("generator-settings", "")
		}
		if (generator == null) {
			generator = Generator.getGenerator(levelType)
		}
		if (provider == null) {
			provider = LevelProviderManager.getProviderByName(config.get("level-settings.default-format", "anvil"))
		}
		val path: String
		path = if (name.contains("/") || name.contains("\\")) {
			name
		} else {
			dataPath + "worlds/" + name + "/"
		}
		val level: Level
		try {
			provider!!.getMethod("generate", String::class.java, String::class.java, Long::class.javaPrimitiveType, Class::class.java, MutableMap::class.java).invoke(null, path, name, seed, generator, options)
			level = Level(this, name, path, provider)
			levels[level.id] = level
			level.initLevel()
			level.tickRate = baseTickRate
		} catch (e: Exception) {
			log.error(language.translateString("nukkit.level.generationError", *arrayOf(name, Utils.getExceptionMessage(e))))
			return false
		}
		pluginManager.callEvent(LevelInitEvent(level))
		pluginManager.callEvent(LevelLoadEvent(level))

		/*this.getLogger().notice(this.getLanguage().translateString("nukkit.level.backgroundGeneration", name));

        int centerX = (int) level.getSpawnLocation().getX() >> 4;
        int centerZ = (int) level.getSpawnLocation().getZ() >> 4;

        TreeMap<String, Integer> order = new TreeMap<>();

        for (int X = -3; X <= 3; ++X) {
            for (int Z = -3; Z <= 3; ++Z) {
                int distance = X * X + Z * Z;
                int chunkX = X + centerX;
                int chunkZ = Z + centerZ;
                order.put(Level.chunkHash(chunkX, chunkZ), distance);
            }
        }

        List<Map.Entry<String, Integer>> sortList = new ArrayList<>(order.entrySet());

        Collections.sort(sortList, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue() - o1.getValue();
            }
        });

        for (String index : order.keySet()) {
            Chunk.Entry entry = Level.getChunkXZ(index);
            level.populateChunk(entry.chunkX, entry.chunkZ, true);
        }*/return true
	}

	fun isLevelGenerated(name: String): Boolean {
		if (name.trim { it <= ' ' } == "") {
			return false
		}
		val path = dataPath + "worlds/" + name + "/"
		return if (getLevelByName(name) == null) {
			LevelProviderManager.getProvider(path) != null
		} else true
	}

	fun <T> getConfig(variable: String?): T {
		return this.getConfig(variable, null)
	}

	fun <T> getConfig(variable: String?, defaultValue: T?): T? {
		val value = config[variable]
		return if (value == null) defaultValue else value as T
	}

	fun getProperty(variable: String?): Any {
		return this.getProperty(variable, null)
	}

	fun getProperty(variable: String?, defaultValue: Any?): Any {
		return (if (properties.exists(variable)) properties[variable] else defaultValue)!!
	}

	fun setPropertyString(variable: String?, value: String?) {
		properties[variable] = value
		properties.save()
	}

	fun getPropertyString(variable: String?): String {
		return this.getPropertyString(variable, null)
	}

	fun getPropertyString(variable: String?, defaultValue: String?): String {
		return (if (properties.exists(variable)) properties[variable] as String else defaultValue)!!
	}

	fun getPropertyInt(variable: String?): Int {
		return this.getPropertyInt(variable, null)
	}

	fun getPropertyInt(variable: String?, defaultValue: Int?): Int {
		return (if (properties.exists(variable)) if (properties[variable] != "") properties[variable].toString().toInt() else defaultValue else defaultValue)!!
	}

	fun setPropertyInt(variable: String?, value: Int) {
		properties[variable] = value
		properties.save()
	}

	fun getPropertyBoolean(variable: String?): Boolean {
		return this.getPropertyBoolean(variable, null)
	}

	fun getPropertyBoolean(variable: String?, defaultValue: Any?): Boolean {
		val value = (if (properties.exists(variable)) properties[variable] else defaultValue)!!
		if (value is Boolean) {
			return value
		}
		when (value.toString()) {
			"on", "true", "1", "yes" -> return true
		}
		return false
	}

	fun setPropertyBoolean(variable: String?, value: Boolean) {
		properties[variable] = if (value) "1" else "0"
		properties.save()
	}

	fun getPluginCommand(name: String?): PluginIdentifiableCommand? {
		val command = commandMap.getCommand(name)
		return if (command is PluginIdentifiableCommand) {
			command
		} else {
			null
		}
	}

	fun addOp(name: String) {
		ops[name.toLowerCase()] = true
		val player = getPlayerExact(name)
		player?.recalculatePermissions()
		ops.save(true)
	}

	fun removeOp(name: String) {
		ops.remove(name.toLowerCase())
		val player = getPlayerExact(name)
		player?.recalculatePermissions()
		ops.save()
	}

	fun addWhitelist(name: String) {
		whitelist[name.toLowerCase()] = true
		whitelist.save(true)
	}

	fun removeWhitelist(name: String) {
		whitelist.remove(name.toLowerCase())
		whitelist.save(true)
	}

	fun isWhitelisted(name: String?): Boolean {
		return !hasWhitelist() || ops.exists(name, true) || whitelist.exists(name, true)
	}

	fun isOp(name: String?): Boolean {
		return ops.exists(name, true)
	}

	fun reloadWhitelist() {
		whitelist.reload()
	}

	val commandAliases: Map<String, List<String>>
		get() {
			val section = this.getConfig<Any>("aliases")
			val result: MutableMap<String, List<String>> = LinkedHashMap()
			if (section is Map<*, *>) {
				for ((key1, value1) in section.entries) {
					val commands: MutableList<String> = ArrayList()
					val key = key1 as String
					val value = value1!!
					if (value is List<*>) {
						commands.addAll((value as List<String>))
					} else {
						commands.add(value as String)
					}
					result[key] = commands
				}
			}
			return result
		}

	fun shouldSavePlayerData(): Boolean {
		return this.getConfig("player.save-player-data", true)!!
	}

	val playerSkinChangeCooldown: Int
		get() = this.getConfig("player.skin-change-cooldown", 30)!!

	/**
	 * Checks the current thread against the expected primary thread for the
	 * server.
	 *
	 *
	 * **Note:** this method should not be used to indicate the current
	 * synchronized state of the runtime. A current thread matching the main
	 * thread indicates that it is synchronized, but a mismatch does not
	 * preclude the same assumption.
	 *
	 * @return true if the current thread matches the expected primary thread,
	 * false otherwise
	 */
	val isPrimaryThread: Boolean
		get() = Thread.currentThread() === currentThread

	fun getPrimaryThread(): Thread {
		return currentThread
	}

	private fun registerEntities() {
		Entity.registerEntity("Lightning", EntityLightning::class.java)
		Entity.registerEntity("Arrow", EntityArrow::class.java)
		Entity.registerEntity("EnderPearl", EntityEnderPearl::class.java)
		Entity.registerEntity("FallingSand", EntityFallingBlock::class.java)
		Entity.registerEntity("Firework", EntityFirework::class.java)
		Entity.registerEntity("Item", EntityItem::class.java)
		Entity.registerEntity("Painting", EntityPainting::class.java)
		Entity.registerEntity("PrimedTnt", EntityPrimedTNT::class.java)
		Entity.registerEntity("Snowball", EntitySnowball::class.java)
		//Monsters
		Entity.registerEntity("Blaze", EntityBlaze::class.java)
		Entity.registerEntity("CaveSpider", EntityCaveSpider::class.java)
		Entity.registerEntity("Creeper", EntityCreeper::class.java)
		Entity.registerEntity("Drowned", EntityDrowned::class.java)
		Entity.registerEntity("ElderGuardian", EntityElderGuardian::class.java)
		Entity.registerEntity("EnderDragon", EntityEnderDragon::class.java)
		Entity.registerEntity("Enderman", EntityEnderman::class.java)
		Entity.registerEntity("Endermite", EntityEndermite::class.java)
		Entity.registerEntity("Evoker", EntityEvoker::class.java)
		Entity.registerEntity("Ghast", EntityGhast::class.java)
		Entity.registerEntity("Guardian", EntityGuardian::class.java)
		Entity.registerEntity("Husk", EntityHusk::class.java)
		Entity.registerEntity("MagmaCube", EntityMagmaCube::class.java)
		Entity.registerEntity("Phantom", EntityPhantom::class.java)
		Entity.registerEntity("Pillager", EntityPillager::class.java)
		Entity.registerEntity("Ravager", EntityRavager::class.java)
		Entity.registerEntity("Shulker", EntityShulker::class.java)
		Entity.registerEntity("Silverfish", EntitySilverfish::class.java)
		Entity.registerEntity("Skeleton", EntitySkeleton::class.java)
		Entity.registerEntity("Slime", EntitySlime::class.java)
		Entity.registerEntity("Spider", EntitySpider::class.java)
		Entity.registerEntity("Stray", EntityStray::class.java)
		Entity.registerEntity("Vex", EntityVex::class.java)
		Entity.registerEntity("Vindicator", EntityVindicator::class.java)
		Entity.registerEntity("Witch", EntityWitch::class.java)
		Entity.registerEntity("Wither", EntityWither::class.java)
		Entity.registerEntity("WitherSkeleton", EntityWitherSkeleton::class.java)
		Entity.registerEntity("Zombie", EntityZombie::class.java)
		Entity.registerEntity("ZombiePigman", EntityZombiePigman::class.java)
		Entity.registerEntity("ZombieVillager", EntityZombieVillager::class.java)
		Entity.registerEntity("ZombieVillagerV1", EntityZombieVillagerV1::class.java)
		//Passive
		Entity.registerEntity("Bat", EntityBat::class.java)
		Entity.registerEntity("Cat", EntityCat::class.java)
		Entity.registerEntity("Chicken", EntityChicken::class.java)
		Entity.registerEntity("Cod", EntityCod::class.java)
		Entity.registerEntity("Cow", EntityCow::class.java)
		Entity.registerEntity("Dolphin", EntityDolphin::class.java)
		Entity.registerEntity("Donkey", EntityDonkey::class.java)
		Entity.registerEntity("Horse", EntityHorse::class.java)
		Entity.registerEntity("Llama", EntityLlama::class.java)
		Entity.registerEntity("Mooshroom", EntityMooshroom::class.java)
		Entity.registerEntity("Mule", EntityMule::class.java)
		Entity.registerEntity("Ocelot", EntityOcelot::class.java)
		Entity.registerEntity("Panda", EntityPanda::class.java)
		Entity.registerEntity("Parrot", EntityParrot::class.java)
		Entity.registerEntity("Pig", EntityPig::class.java)
		Entity.registerEntity("PolarBear", EntityPolarBear::class.java)
		Entity.registerEntity("Pufferfish", EntityPufferfish::class.java)
		Entity.registerEntity("Rabbit", EntityRabbit::class.java)
		Entity.registerEntity("Salmon", EntitySalmon::class.java)
		Entity.registerEntity("Sheep", EntitySheep::class.java)
		Entity.registerEntity("SkeletonHorse", EntitySkeletonHorse::class.java)
		Entity.registerEntity("Squid", EntitySquid::class.java)
		Entity.registerEntity("TropicalFish", EntityTropicalFish::class.java)
		Entity.registerEntity("Turtle", EntityTurtle::class.java)
		Entity.registerEntity("Villager", EntityVillager::class.java)
		Entity.registerEntity("VillagerV1", EntityVillagerV1::class.java)
		Entity.registerEntity("WanderingTrader", EntityWanderingTrader::class.java)
		Entity.registerEntity("Wolf", EntityWolf::class.java)
		Entity.registerEntity("ZombieHorse", EntityZombieHorse::class.java)
		//Projectile
		Entity.registerEntity("Egg", EntityEgg::class.java)
		Entity.registerEntity("ThrownExpBottle", EntityExpBottle::class.java)
		Entity.registerEntity("ThrownPotion", EntityPotion::class.java)
		Entity.registerEntity("ThrownTrident", EntityThrownTrident::class.java)
		Entity.registerEntity("XpOrb", EntityXPOrb::class.java)
		Entity.registerEntity("Human", EntityHuman::class.java, true)
		//Vehicle
		Entity.registerEntity("Boat", EntityBoat::class.java)
		Entity.registerEntity("MinecartChest", EntityMinecartChest::class.java)
		Entity.registerEntity("MinecartHopper", EntityMinecartHopper::class.java)
		Entity.registerEntity("MinecartRideable", EntityMinecartEmpty::class.java)
		Entity.registerEntity("MinecartTnt", EntityMinecartTNT::class.java)
		Entity.registerEntity("EndCrystal", EntityEndCrystal::class.java)
		Entity.registerEntity("FishingHook", EntityFishingHook::class.java)
	}

	private fun registerBlockEntities() {
		BlockEntity.registerBlockEntity(BlockEntity.FURNACE, BlockEntityFurnace::class.java)
		BlockEntity.registerBlockEntity(BlockEntity.CHEST, BlockEntityChest::class.java)
		BlockEntity.registerBlockEntity(BlockEntity.SIGN, BlockEntitySign::class.java)
		BlockEntity.registerBlockEntity(BlockEntity.ENCHANT_TABLE, BlockEntityEnchantTable::class.java)
		BlockEntity.registerBlockEntity(BlockEntity.SKULL, BlockEntitySkull::class.java)
		BlockEntity.registerBlockEntity(BlockEntity.FLOWER_POT, BlockEntityFlowerPot::class.java)
		BlockEntity.registerBlockEntity(BlockEntity.BREWING_STAND, BlockEntityBrewingStand::class.java)
		BlockEntity.registerBlockEntity(BlockEntity.ITEM_FRAME, BlockEntityItemFrame::class.java)
		BlockEntity.registerBlockEntity(BlockEntity.CAULDRON, BlockEntityCauldron::class.java)
		BlockEntity.registerBlockEntity(BlockEntity.ENDER_CHEST, BlockEntityEnderChest::class.java)
		BlockEntity.registerBlockEntity(BlockEntity.BEACON, BlockEntityBeacon::class.java)
		BlockEntity.registerBlockEntity(BlockEntity.PISTON_ARM, BlockEntityPistonArm::class.java)
		BlockEntity.registerBlockEntity(BlockEntity.COMPARATOR, BlockEntityComparator::class.java)
		BlockEntity.registerBlockEntity(BlockEntity.HOPPER, BlockEntityHopper::class.java)
		BlockEntity.registerBlockEntity(BlockEntity.BED, BlockEntityBed::class.java)
		BlockEntity.registerBlockEntity(BlockEntity.JUKEBOX, BlockEntityJukebox::class.java)
		BlockEntity.registerBlockEntity(BlockEntity.SHULKER_BOX, BlockEntityShulkerBox::class.java)
		BlockEntity.registerBlockEntity(BlockEntity.BANNER, BlockEntityBanner::class.java)
		BlockEntity.registerBlockEntity(BlockEntity.MUSIC, BlockEntityMusic::class.java)
	}

	fun getPlayerDataSerializer(): PlayerDataSerializer {
		return playerDataSerializer
	}

	fun setPlayerDataSerializer(playerDataSerializer: PlayerDataSerializer) {
		this.playerDataSerializer = Preconditions.checkNotNull(playerDataSerializer, "playerDataSerializer")
	}

	private inner class ConsoleThread : Thread(), InterruptibleThread {
		override fun run() {
			console.start()
		}
	}

	companion object {
		const val BROADCAST_CHANNEL_ADMINISTRATIVE = "nukkit.broadcast.admin"
		const val BROADCAST_CHANNEL_USERS = "nukkit.broadcast.user"
		@JvmStatic
        lateinit var instance: Server
		fun broadcastPacket(players: Collection<Player>, packet: DataPacket) {
			broadcastPacket(players.toTypedArray(), packet)
		}

		@JvmStatic
        fun broadcastPacket(players: Array<Player>, packet: DataPacket) {
			packet.encode()
			packet.isEncoded = true
			if (packet.pid() == ProtocolInfo.BATCH_PACKET) {
				for (player in players) {
					player.dataPacket(packet)
				}
			} else {
				instance!!.batchPackets(players, arrayOf(packet), true)
			}
			if (packet.encapsulatedPacket != null) {
				packet.encapsulatedPacket = null
			}
		}

		fun getGamemodeString(mode: Int): String {
			return getGamemodeString(mode, false)
		}

		@JvmStatic
        fun getGamemodeString(mode: Int, direct: Boolean): String {
			when (mode) {
				Player.SURVIVAL -> return if (direct) "Survival" else "%gameMode.survival"
				Player.CREATIVE -> return if (direct) "Creative" else "%gameMode.creative"
				Player.ADVENTURE -> return if (direct) "Adventure" else "%gameMode.adventure"
				Player.SPECTATOR -> return if (direct) "Spectator" else "%gameMode.spectator"
			}
			return "UNKNOWN"
		}

		@JvmStatic
        fun getGamemodeFromString(str: String): Int {
			when (str.trim { it <= ' ' }.toLowerCase()) {
				"0", "survival", "s" -> return Player.SURVIVAL
				"1", "creative", "c" -> return Player.CREATIVE
				"2", "adventure", "a" -> return Player.ADVENTURE
				"3", "spectator", "spc", "view", "v" -> return Player.SPECTATOR
			}
			return -1
		}

		@JvmStatic
        fun getDifficultyFromString(str: String): Int {
			when (str.trim { it <= ' ' }.toLowerCase()) {
				"0", "peaceful", "p" -> return 0
				"1", "easy", "e" -> return 1
				"2", "normal", "n" -> return 2
				"3", "hard", "h" -> return 3
			}
			return -1
		}

	}
}