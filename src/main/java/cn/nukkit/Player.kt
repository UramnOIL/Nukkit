package cn.nukkit

import cn.nukkit.Server.Companion.broadcastPacket
import cn.nukkit.Server.Companion.getGamemodeString
import cn.nukkit.block.*
import cn.nukkit.blockentity.BlockEntityItemFrame
import cn.nukkit.blockentity.BlockEntitySpawnable
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandDataVersions
import cn.nukkit.entity.*
import cn.nukkit.entity.data.*
import cn.nukkit.entity.item.*
import cn.nukkit.entity.projectile.EntityArrow
import cn.nukkit.entity.projectile.EntityThrownTrident
import cn.nukkit.event.block.ItemFrameDropItemEvent
import cn.nukkit.event.entity.EntityDamageByBlockEvent
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.event.entity.EntityDamageEvent.DamageModifier
import cn.nukkit.event.entity.ProjectileLaunchEvent
import cn.nukkit.event.inventory.InventoryCloseEvent
import cn.nukkit.event.inventory.InventoryPickupArrowEvent
import cn.nukkit.event.inventory.InventoryPickupItemEvent
import cn.nukkit.event.player.*
import cn.nukkit.event.player.PlayerAsyncPreLoginEvent.LoginResult
import cn.nukkit.event.player.PlayerTeleportEvent.TeleportCause
import cn.nukkit.event.server.DataPacketReceiveEvent
import cn.nukkit.event.server.DataPacketSendEvent
import cn.nukkit.form.window.FormWindow
import cn.nukkit.form.window.FormWindowCustom
import cn.nukkit.inventory.*
import cn.nukkit.inventory.transaction.CraftingTransaction
import cn.nukkit.inventory.transaction.InventoryTransaction
import cn.nukkit.inventory.transaction.action.InventoryAction
import cn.nukkit.inventory.transaction.data.ReleaseItemData
import cn.nukkit.inventory.transaction.data.UseItemData
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData
import cn.nukkit.item.*
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.lang.TextContainer
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.level.*
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.particle.PunchBlockParticle
import cn.nukkit.math.*
import cn.nukkit.metadata.MetadataValue
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.*
import cn.nukkit.network.SourceInterface
import cn.nukkit.network.protocol.*
import cn.nukkit.network.protocol.types.ContainerIds
import cn.nukkit.permission.PermissibleBase
import cn.nukkit.permission.Permission
import cn.nukkit.permission.PermissionAttachment
import cn.nukkit.permission.PermissionAttachmentInfo
import cn.nukkit.plugin.Plugin
import cn.nukkit.potion.Effect
import cn.nukkit.scheduler.AsyncTask
import cn.nukkit.scheduler.Task
import cn.nukkit.utils.*
import co.aikar.timings.Timings
import com.google.common.base.Strings
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectIterator
import lombok.extern.log4j.Log4j2
import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteOrder
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

/**
 * @author MagicDroidX &amp; Box
 * Nukkit Project
 */
@Log4j2
class Player(protected val interfaz: SourceInterface, clientID: Long?, ip: String, port: Int) : EntityHuman(null, CompoundTag()), CommandSender, InventoryHolder, ChunkLoader, IPlayer {
	var playedBefore = false
	var spawned = false
	var loggedIn = false
	var gamemode: Int
	var lastBreak: Long
	private var lastBreakPosition = BlockVector3()
	protected var windowCnt = 4
	protected val windows: BiMap<Inventory?, Int> = HashBiMap.create()
	protected val windowIndex = windows.inverse()!!
	protected val permanentWindows: MutableSet<Int> = IntOpenHashSet()
	protected var messageCounter = 2
	val clientSecret: String? = null
	var speed: Vector3? = null
	val achievements = HashSet<String>()
	var craftingType = CRAFTING_SMALL
	var uIInventory: PlayerUIInventory? = null
		protected set
	protected var craftingGrid: CraftingGrid? = null
	protected var craftingTransaction: CraftingTransaction? = null
	var creationTime: Long = 0

	/**
	 * This might disappear in the future.
	 * Please use getUniqueId() instead (IP + clientId + name combo, in the future it'll change to real UUID for online auth)
	 * @return random client id
	 */
	@get:Deprecated("")
	var clientId: Long = 0
		protected set
	protected var forceMovement: Vector3? = null
	protected var teleportPosition: Vector3? = null
	var isConnected = true
		protected set
	val address: String
	var removeFormat = true
	val port: Int
	protected var username: String? = null
	protected var iusername: String? = null
	protected var displayName: String? = null
	var startActionTick = -1
		protected set
	protected var sleeping: Vector3? = null
	protected var clientID: Long? = null
	private val loaderId: Int
	protected var stepHeight = 0.6f
	@JvmField
	val usedChunks: MutableMap<Long, Boolean> = Long2ObjectOpenHashMap()
	protected var chunkLoadCount = 0
	protected val loadQueue = Long2ObjectLinkedOpenHashMap<Boolean>()
	protected var nextChunkOrderRun = 1
	protected val hiddenPlayers: MutableMap<UUID, Player> = HashMap()
	protected var newPosition: Vector3? = null
	protected var chunkRadius: Int
	protected var viewDistance: Int
	protected val chunksPerTick: Int
	protected val spawnThreshold: Int
	protected var spawnPosition: Position? = null
	var inAirTicks = 0
		protected set
	protected var startAirTicks = 5
	var adventureSettings: AdventureSettings? = null
		protected set
	protected var checkMovement = true
	private val needACK = Int2ObjectOpenHashMap<Boolean>()
	private val batchedPackets: MutableMap<Int, MutableList<DataPacket>> = TreeMap()
	private var perm: PermissibleBase? = null
	private var exp = 0
	var experienceLevel = 0
		private set
	var foodData: PlayerFood? = null
		protected set
	var killer: Entity? = null
		private set

	@get:Synchronized
	@set:Synchronized
	var locale = AtomicReference<Locale?>(null)
		get() = field.get()
		set(locale) {
			this.locale.set(locale)
		}
	private var hash = 0
	var buttonText = "Button"
		set(text) {
			field = text
			this.setDataProperty(StringEntityData(Entity.DATA_INTERACTIVE_TAG, buttonText))
		}
	protected var enableClientCommand = true
	var viewingEnderChest: BlockEnderChest? = null
		set(chest) {
			if (chest == null && viewingEnderChest != null) {
				viewingEnderChest!!.viewers.remove(this)
			} else chest?.viewers?.add(this)
			field = chest
		}
	var lastEnderPearlThrowingTick = 20
		protected set
	var lastChorusFruitTeleport = 20
		protected set
	var loginChainData: LoginChainData? = null
		private set
	var breakingBlock: Block? = null
	var pickedXPOrb = 0
	protected var formWindowCount = 0
	protected var formWindows: MutableMap<Int, FormWindow> = Int2ObjectOpenHashMap()
	protected var serverSettings: MutableMap<Int, FormWindow> = Int2ObjectOpenHashMap()
	protected var dummyBossBars: MutableMap<Long, DummyBossBar?> = Long2ObjectLinkedOpenHashMap()
	private var preLoginEventTask: AsyncTask? = null
	protected var shouldLogin = false
	@JvmField
	var fishing: EntityFishingHook? = null
	var lastSkinChange: Long
	protected var lastRightClickTime = 0.0
	protected var lastRightClickPos: Vector3? = null

	fun startAction() {
		startActionTick = server.tick
	}

	fun stopAction() {
		startActionTick = -1
	}

	fun onThrowEnderPearl() {
		lastEnderPearlThrowingTick = server.tick
	}

	fun onChorusFruitTeleport() {
		lastChorusFruitTeleport = server.tick
	}

	val leaveMessage: TranslationContainer
		get() = TranslationContainer(TextFormat.YELLOW.toString() + "%multiplayer.player.left", getDisplayName())

	override fun isBanned(): Boolean {
		return server.nameBans.isBanned(this.name)
	}

	override fun setBanned(value: Boolean) {
		if (value) {
			server.nameBans.addBan(this.name, null, null, null)
			this.kick(PlayerKickEvent.Reason.NAME_BANNED, "Banned by admin")
		} else {
			server.nameBans.remove(this.name)
		}
	}

	override fun isWhitelisted(): Boolean {
		return server.isWhitelisted(this.name.toLowerCase())
	}

	override fun setWhitelisted(value: Boolean) {
		if (value) {
			server.addWhitelist(this.name.toLowerCase())
		} else {
			server.removeWhitelist(this.name.toLowerCase())
		}
	}

	override fun getPlayer(): Player {
		return this
	}

	override fun getFirstPlayed(): Long {
		return (if (namedTag != null) namedTag.getLong("firstPlayed") else null)!!
	}

	override fun getLastPlayed(): Long {
		return (if (namedTag != null) namedTag.getLong("lastPlayed") else null)!!
	}

	override fun hasPlayedBefore(): Boolean {
		return playedBefore
	}

	fun setAdventureSettings(adventureSettings: AdventureSettings) {
		this.adventureSettings = adventureSettings.clone(this)
		this.adventureSettings.update()
	}

	fun resetInAirTicks() {
		inAirTicks = 0
	}

	@get:Deprecated("")
	@set:Deprecated("")
	var allowFlight: Boolean
		get() = adventureSettings!![AdventureSettings.Type.ALLOW_FLIGHT]
		set(value) {
			adventureSettings!![AdventureSettings.Type.ALLOW_FLIGHT] = value
			adventureSettings!!.update()
		}

	fun setAllowModifyWorld(value: Boolean) {
		adventureSettings!![AdventureSettings.Type.WORLD_IMMUTABLE] = !value
		adventureSettings!![AdventureSettings.Type.BUILD_AND_MINE] = value
		adventureSettings!![AdventureSettings.Type.WORLD_BUILDER] = value
		adventureSettings!!.update()
	}

	fun setAllowInteract(value: Boolean) {
		setAllowInteract(value, value)
	}

	fun setAllowInteract(value: Boolean, containers: Boolean) {
		adventureSettings!![AdventureSettings.Type.WORLD_IMMUTABLE] = !value
		adventureSettings!![AdventureSettings.Type.DOORS_AND_SWITCHED] = value
		adventureSettings!![AdventureSettings.Type.OPEN_CONTAINERS] = containers
		adventureSettings!!.update()
	}

	@Deprecated("")
	fun setAutoJump(value: Boolean) {
		adventureSettings!![AdventureSettings.Type.AUTO_JUMP] = value
		adventureSettings!!.update()
	}

	@Deprecated("")
	fun hasAutoJump(): Boolean {
		return adventureSettings!![AdventureSettings.Type.AUTO_JUMP]
	}

	override fun spawnTo(player: Player) {
		if (spawned && player.spawned && this.isAlive && player.level === level && player.canSee(this) && !isSpectator) {
			super.spawnTo(player)
		}
	}

	override fun getServer(): Server {
		return server
	}

	fun setRemoveFormat() {
		removeFormat = true
	}

	fun canSee(player: Player): Boolean {
		return !hiddenPlayers.containsKey(player.uniqueId)
	}

	fun hidePlayer(player: Player) {
		if (this === player) {
			return
		}
		hiddenPlayers[player.uniqueId] = player
		player.despawnFrom(this)
	}

	fun showPlayer(player: Player) {
		if (this === player) {
			return
		}
		hiddenPlayers.remove(player.uniqueId)
		if (player.isOnline) {
			player.spawnTo(this)
		}
	}

	override fun canCollideWith(entity: Entity): Boolean {
		return false
	}

	override fun resetFallDistance() {
		super.resetFallDistance()
		if (inAirTicks != 0) {
			startAirTicks = 5
		}
		inAirTicks = 0
		highestPosition = y
	}

	override fun isOnline(): Boolean {
		return isConnected && loggedIn
	}

	override fun isOp(): Boolean {
		return server.isOp(this.name)
	}

	override fun setOp(value: Boolean) {
		if (value == this.isOp) {
			return
		}
		if (value) {
			server.addOp(this.name)
		} else {
			server.removeOp(this.name)
		}
		recalculatePermissions()
		adventureSettings!!.update()
		sendCommandData()
	}

	override fun isPermissionSet(name: String): Boolean {
		return perm!!.isPermissionSet(name)
	}

	override fun isPermissionSet(permission: Permission): Boolean {
		return perm!!.isPermissionSet(permission)
	}

	override fun hasPermission(name: String): Boolean {
		return perm != null && perm!!.hasPermission(name)
	}

	override fun hasPermission(permission: Permission): Boolean {
		return perm!!.hasPermission(permission)
	}

	override fun addAttachment(plugin: Plugin): PermissionAttachment {
		return this.addAttachment(plugin, null)
	}

	override fun addAttachment(plugin: Plugin, name: String): PermissionAttachment {
		return this.addAttachment(plugin, name, null)
	}

	override fun addAttachment(plugin: Plugin, name: String, value: Boolean): PermissionAttachment {
		return perm!!.addAttachment(plugin, name, value)
	}

	override fun removeAttachment(attachment: PermissionAttachment) {
		perm!!.removeAttachment(attachment)
	}

	override fun recalculatePermissions() {
		server.pluginManager.unsubscribeFromPermission(Server.BROADCAST_CHANNEL_USERS, this)
		server.pluginManager.unsubscribeFromPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE, this)
		if (perm == null) {
			return
		}
		perm!!.recalculatePermissions()
		if (this.hasPermission(Server.BROADCAST_CHANNEL_USERS)) {
			server.pluginManager.subscribeToPermission(Server.BROADCAST_CHANNEL_USERS, this)
		}
		if (this.hasPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE)) {
			server.pluginManager.subscribeToPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE, this)
		}
		if (isEnableClientCommand() && spawned) sendCommandData()
	}

	fun isEnableClientCommand(): Boolean {
		return enableClientCommand
	}

	fun setEnableClientCommand(enable: Boolean) {
		enableClientCommand = enable
		val pk = SetCommandsEnabledPacket()
		pk.enabled = enable
		this.dataPacket(pk)
		if (enable) sendCommandData()
	}

	fun sendCommandData() {
		if (!spawned) {
			return
		}
		val pk = AvailableCommandsPacket()
		val data: MutableMap<String, CommandDataVersions> = HashMap()
		var count = 0
		for (command in server.commandMap.commands.values) {
			if (!command.testPermissionSilent(this)) {
				continue
			}
			++count
			val data0 = command.generateCustomCommandData(this)
			data[command.name] = data0
		}
		if (count > 0) {
			//TODO: structure checking
			pk.commands = data
			val identifier = this.dataPacket(pk, true) // We *need* ACK so we can be sure that the client received the packet or not
			Server.instance!!.scheduler.scheduleDelayedTask(object : Task() {
				override fun onRun(currentTick: Int) {
					val status = needACK[identifier]
					if ((status == null || !status) && isOnline) {
						sendCommandData()
					}
				}
			}, 60, true)
		}
	}

	override fun getEffectivePermissions(): Map<String, PermissionAttachmentInfo> {
		return perm!!.effectivePermissions
	}

	override fun initEntity() {
		super.initEntity()
		addDefaultWindows()
	}

	override fun isPlayer(): Boolean {
		return true
	}

	fun removeAchievement(achievementId: String) {
		achievements.remove(achievementId)
	}

	fun hasAchievement(achievementId: String): Boolean {
		return achievements.contains(achievementId)
	}

	fun getDisplayName(): String? {
		return displayName
	}

	fun setDisplayName(displayName: String?) {
		this.displayName = displayName
		if (spawned) {
			server.updatePlayerListData(this.uniqueId, getId(), getDisplayName(), getSkin(), loginChainData!!.xuid)
		}
	}

	override fun setSkin(skin: Skin) {
		super.setSkin(skin)
		if (spawned) {
			server.updatePlayerListData(this.uniqueId, getId(), getDisplayName(), skin, loginChainData!!.xuid)
		}
	}

	val nextPosition: Position
		get() = if (newPosition != null) Position(newPosition!!.x, newPosition!!.y, newPosition!!.z, level) else this.position

	fun isSleeping(): Boolean {
		return sleeping != null
	}

	/**
	 * Returns whether the player is currently using an item (right-click and hold).
	 *
	 * @return bool
	 */
	var isUsingItem: Boolean
		get() = getDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_ACTION) && startActionTick > -1
		set(value) {
			startActionTick = if (value) server.tick else -1
			this.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_ACTION, value)
		}

	@JvmOverloads
	fun unloadChunk(x: Int, z: Int, level: Level? = null) {
		var level = level
		level = level ?: this.level
		val index = Level.chunkHash(x, z)
		if (usedChunks.containsKey(index)) {
			for (entity in level.getChunkEntities(x, z).values) {
				if (entity !== this) {
					entity.despawnFrom(this)
				}
			}
			usedChunks.remove(index)
		}
		level.unregisterChunkLoader(this, x, z)
		loadQueue.remove(index)
	}

	val spawn: Position?
		get() = if (spawnPosition != null && spawnPosition!!.getLevel() != null) {
			spawnPosition
		} else {
			server.defaultLevel!!.safeSpawn
		}

	fun sendChunk(x: Int, z: Int, packet: DataPacket?) {
		if (!isConnected) {
			return
		}
		usedChunks[Level.chunkHash(x, z)] = java.lang.Boolean.TRUE
		chunkLoadCount++
		this.dataPacket(packet)
		if (spawned) {
			for (entity in level.getChunkEntities(x, z).values) {
				if (this !== entity && !entity.closed && entity.isAlive) {
					entity.spawnTo(this)
				}
			}
		}
	}

	fun sendChunk(x: Int, z: Int, subChunkCount: Int, payload: ByteArray?) {
		if (!isConnected) {
			return
		}
		usedChunks[Level.chunkHash(x, z)] = true
		chunkLoadCount++
		val pk = LevelChunkPacket()
		pk.chunkX = x
		pk.chunkZ = z
		pk.subChunkCount = subChunkCount
		pk.data = payload
		batchDataPacket(pk)
		if (spawned) {
			for (entity in level.getChunkEntities(x, z).values) {
				if (this !== entity && !entity.closed && entity.isAlive) {
					entity.spawnTo(this)
				}
			}
		}
	}

	protected fun sendNextChunk() {
		if (!isConnected) {
			return
		}
		Timings.playerChunkSendTimer.startTiming()
		if (!loadQueue.isEmpty()) {
			var count = 0
			val iter: ObjectIterator<Long2ObjectMap.Entry<Boolean>> = loadQueue.long2ObjectEntrySet().fastIterator()
			while (iter.hasNext()) {
				val entry = iter.next()
				val index = entry.longKey
				if (count >= chunksPerTick) {
					break
				}
				val chunkX = Level.getHashX(index)
				val chunkZ = Level.getHashZ(index)
				++count
				usedChunks[index] = false
				level.registerChunkLoader(this, chunkX, chunkZ, false)
				if (!level.populateChunk(chunkX, chunkZ)) {
					if (spawned && teleportPosition == null) {
						continue
					} else {
						break
					}
				}
				iter.remove()
				val ev = PlayerChunkRequestEvent(this, chunkX, chunkZ)
				server.pluginManager.callEvent(ev)
				if (!ev.isCancelled) {
					level.requestChunk(chunkX, chunkZ, this)
				}
			}
		}
		if (chunkLoadCount >= spawnThreshold && !spawned && teleportPosition == null) {
			doFirstSpawn()
		}
		Timings.playerChunkSendTimer.stopTiming()
	}

	protected fun doFirstSpawn() {
		spawned = true
		setEnableClientCommand(true)
		adventureSettings!!.update()
		sendAttributes()
		sendPotionEffects(this)
		this.sendData(this)
		inventory.sendContents(this)
		inventory.sendArmorContents(this)
		offhandInventory.sendContents(this)
		val setTimePacket = SetTimePacket()
		setTimePacket.time = level.time
		this.dataPacket(setTimePacket)
		var pos = level.getSafeSpawn(this)
		val respawnEvent = PlayerRespawnEvent(this, pos, true)
		server.pluginManager.callEvent(respawnEvent)
		pos = respawnEvent.respawnPosition
		sendPlayStatus(PlayStatusPacket.PLAYER_SPAWN)
		val playerJoinEvent = PlayerJoinEvent(this,
				TranslationContainer(TextFormat.YELLOW.toString() + "%multiplayer.player.joined", *arrayOf(
						getDisplayName()
				))
		)
		server.pluginManager.callEvent(playerJoinEvent)
		if (playerJoinEvent.joinMessage.toString().trim { it <= ' ' }.length > 0) {
			server.broadcastMessage(playerJoinEvent.joinMessage)
		}
		noDamageTicks = 60
		getServer().sendRecipeList(this)
		if (gamemode == SPECTATOR) {
			val inventoryContentPacket = InventoryContentPacket()
			inventoryContentPacket.inventoryId = ContainerIds.CREATIVE
			this.dataPacket(inventoryContentPacket)
		} else {
			inventory.sendCreativeContents()
		}
		for (index in usedChunks.keys) {
			val chunkX = Level.getHashX(index)
			val chunkZ = Level.getHashZ(index)
			for (entity in level.getChunkEntities(chunkX, chunkZ).values) {
				if (this !== entity && !entity.closed && entity.isAlive) {
					entity.spawnTo(this)
				}
			}
		}
		val experience = experience
		if (experience != 0) {
			sendExperience(experience)
		}
		val level = experienceLevel
		if (level != 0) {
			sendExperienceLevel(experienceLevel)
		}
		this.teleport(pos, null) // Prevent PlayerTeleportEvent during player spawn
		if (!isSpectator) {
			spawnToAll()
		}

		//todo Updater

		//Weather
		if (this.level.isRaining || this.level.isThundering) {
			level.sendWeather(this)
		}
		level.sendWeather(this)

		//FoodLevel
		val food = foodData
		if (food!!.level != food.maxLevel) {
			food.sendFoodLevel()
		}
		if (getHealth() < 1) {
			respawn()
		}
	}

	protected fun orderChunks(): Boolean {
		if (!isConnected) {
			return false
		}
		Timings.playerChunkOrderTimer.startTiming()
		nextChunkOrderRun = 200
		loadQueue.clear()
		val lastChunk = Long2ObjectOpenHashMap(usedChunks)
		val centerX = x.toInt() shr 4
		val centerZ = z.toInt() shr 4
		val radius = if (spawned) chunkRadius else Math.ceil(Math.sqrt(spawnThreshold.toDouble())).toInt()
		val radiusSqr = radius * radius
		var index: Long
		for (x in 0..radius) {
			val xx = x * x
			for (z in 0..x) {
				val distanceSqr = xx + z * z
				if (distanceSqr > radiusSqr) continue

				/* Top right quadrant */if (usedChunks[Level.chunkHash(centerX + x, centerZ + z).also { index = it }] !== java.lang.Boolean.TRUE) {
					loadQueue[index] = java.lang.Boolean.TRUE
				}
				lastChunk.remove(index)
				/* Top left quadrant */if (usedChunks[Level.chunkHash(centerX - x - 1, centerZ + z).also { index = it }] !== java.lang.Boolean.TRUE) {
					loadQueue[index] = java.lang.Boolean.TRUE
				}
				lastChunk.remove(index)
				/* Bottom right quadrant */if (usedChunks[Level.chunkHash(centerX + x, centerZ - z - 1).also { index = it }] !== java.lang.Boolean.TRUE) {
					loadQueue[index] = java.lang.Boolean.TRUE
				}
				lastChunk.remove(index)
				/* Bottom left quadrant */if (usedChunks[Level.chunkHash(centerX - x - 1, centerZ - z - 1).also { index = it }] !== java.lang.Boolean.TRUE) {
					loadQueue[index] = java.lang.Boolean.TRUE
				}
				lastChunk.remove(index)
				if (x != z) {
					/* Top right quadrant mirror */
					if (usedChunks[Level.chunkHash(centerX + z, centerZ + x).also { index = it }] !== java.lang.Boolean.TRUE) {
						loadQueue[index] = java.lang.Boolean.TRUE
					}
					lastChunk.remove(index)
					/* Top left quadrant mirror */if (usedChunks[Level.chunkHash(centerX - z - 1, centerZ + x).also { index = it }] !== java.lang.Boolean.TRUE) {
						loadQueue[index] = java.lang.Boolean.TRUE
					}
					lastChunk.remove(index)
					/* Bottom right quadrant mirror */if (usedChunks[Level.chunkHash(centerX + z, centerZ - x - 1).also { index = it }] !== java.lang.Boolean.TRUE) {
						loadQueue[index] = java.lang.Boolean.TRUE
					}
					lastChunk.remove(index)
					/* Bottom left quadrant mirror */if (usedChunks[Level.chunkHash(centerX - z - 1, centerZ - x - 1).also { index = it }] !== java.lang.Boolean.TRUE) {
						loadQueue[index] = java.lang.Boolean.TRUE
					}
					lastChunk.remove(index)
				}
			}
		}
		val keys = lastChunk.keys.iterator()
		while (keys.hasNext()) {
			index = keys.nextLong()
			unloadChunk(Level.getHashX(index), Level.getHashZ(index))
		}
		if (!loadQueue.isEmpty()) {
			val packet = NetworkChunkPublisherUpdatePacket()
			packet.position = asBlockVector3()
			packet.radius = viewDistance shl 4
			this.dataPacket(packet)
		}
		Timings.playerChunkOrderTimer.stopTiming()
		return true
	}

	fun batchDataPacket(packet: DataPacket): Boolean {
		if (!isConnected) {
			return false
		}
		Timings.getSendDataPacketTiming(packet).use { timing ->
			val event = DataPacketSendEvent(this, packet)
			server.pluginManager.callEvent(event)
			if (event.isCancelled) {
				return false
			}
			if (!batchedPackets.containsKey(packet.channel)) {
				batchedPackets[packet.channel] = ArrayList()
			}
			batchedPackets[packet.channel]!!.add(packet.clone())
		}
		return true
	}

	/**
	 * 0 is true
	 * -1 is false
	 * other is identifer
	 * @param packet packet to send
	 * @return packet successfully sent
	 */
	fun dataPacket(packet: DataPacket?): Boolean {
		return this.dataPacket(packet, false) != -1
	}

	fun dataPacket(packet: DataPacket?, needACK: Boolean): Int {
		if (!isConnected) {
			return -1
		}
		Timings.getSendDataPacketTiming(packet).use { timing ->
			val ev = DataPacketSendEvent(this, packet)
			server.pluginManager.callEvent(ev)
			if (ev.isCancelled) {
				return -1
			}
			if (log.isTraceEnabled() && packet !is BatchPacket) {
				log.trace("Outbound {}: {}", this.name, packet)
			}
			val identifier = interfaz.putPacket(this, packet, needACK, false)
			if (needACK && identifier != null) {
				this.needACK[identifier] = java.lang.Boolean.FALSE
				return identifier
			}
		}
		return 0
	}

	/**
	 * 0 is true
	 * -1 is false
	 * other is identifer
	 * @param packet packet to send
	 * @return packet successfully sent
	 */
	fun directDataPacket(packet: DataPacket?): Boolean {
		return this.directDataPacket(packet, false) != -1
	}

	fun directDataPacket(packet: DataPacket?, needACK: Boolean): Int {
		if (!isConnected) {
			return -1
		}
		Timings.getSendDataPacketTiming(packet).use { timing ->
			val ev = DataPacketSendEvent(this, packet)
			server.pluginManager.callEvent(ev)
			if (ev.isCancelled) {
				return -1
			}
			val identifier = interfaz.putPacket(this, packet, needACK, true)
			if (needACK && identifier != null) {
				this.needACK[identifier] = java.lang.Boolean.FALSE
				return identifier
			}
		}
		return 0
	}

	val ping: Int
		get() = interfaz.getNetworkLatency(this)

	fun sleepOn(pos: Vector3): Boolean {
		if (!this.isOnline) {
			return false
		}
		for (p in level.getNearbyEntities(boundingBox.grow(2.0, 1.0, 2.0), this)) {
			if (p is Player) {
				if (p.sleeping != null && pos.distance(p.sleeping) <= 0.1) {
					return false
				}
			}
		}
		var ev: PlayerBedEnterEvent
		server.pluginManager.callEvent(PlayerBedEnterEvent(this, level.getBlock(pos)).also { ev = it })
		if (ev.isCancelled) {
			return false
		}
		sleeping = pos.clone()
		this.teleport(Location(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, yaw, pitch, level), null)
		this.setDataProperty(IntPositionEntityData(DATA_PLAYER_BED_POSITION, pos.x.toInt(), pos.y.toInt(), pos.z.toInt()))
		this.setDataFlag(DATA_PLAYER_FLAGS, DATA_PLAYER_FLAG_SLEEP, true)
		setSpawn(pos)
		level.sleepTicks = 60
		return true
	}

	fun setSpawn(pos: Vector3) {
		val level: Level
		level = if (pos !is Position) {
			this.level
		} else {
			pos.getLevel()
		}
		spawnPosition = Position(pos.x, pos.y, pos.z, level)
		val pk = SetSpawnPositionPacket()
		pk.spawnType = SetSpawnPositionPacket.TYPE_PLAYER_SPAWN
		pk.x = spawnPosition!!.x.toInt()
		pk.y = spawnPosition!!.y.toInt()
		pk.z = spawnPosition!!.z.toInt()
		this.dataPacket(pk)
	}

	fun stopSleep() {
		if (sleeping != null) {
			server.pluginManager.callEvent(PlayerBedLeaveEvent(this, level.getBlock(sleeping)))
			sleeping = null
			this.setDataProperty(IntPositionEntityData(DATA_PLAYER_BED_POSITION, 0, 0, 0))
			this.setDataFlag(DATA_PLAYER_FLAGS, DATA_PLAYER_FLAG_SLEEP, false)
			level.sleepTicks = 0
			val pk = AnimatePacket()
			pk.eid = id
			pk.action = AnimatePacket.Action.WAKE_UP
			this.dataPacket(pk)
		}
	}

	fun awardAchievement(achievementId: String): Boolean {
		if (!Server.instance!!.getPropertyBoolean("achievements", true)) {
			return false
		}
		val achievement = Achievement.achievements[achievementId]
		if (achievement == null || hasAchievement(achievementId)) {
			return false
		}
		for (id in achievement.requires) {
			if (!hasAchievement(id)) {
				return false
			}
		}
		val event = PlayerAchievementAwardedEvent(this, achievementId)
		server.pluginManager.callEvent(event)
		if (event.isCancelled) {
			return false
		}
		achievements.add(achievementId)
		achievement.broadcast(this)
		return true
	}

	fun setGamemode(gamemode: Int): Boolean {
		return this.setGamemode(gamemode, false, null)
	}

	fun setGamemode(gamemode: Int, clientSide: Boolean): Boolean {
		return this.setGamemode(gamemode, clientSide, null)
	}

	fun setGamemode(gamemode: Int, clientSide: Boolean, newSettings: AdventureSettings?): Boolean {
		var newSettings = newSettings
		if (gamemode < 0 || gamemode > 3 || this.gamemode == gamemode) {
			return false
		}
		if (newSettings == null) {
			newSettings = adventureSettings!!.clone(this)
			newSettings[AdventureSettings.Type.WORLD_IMMUTABLE] = gamemode and 0x02 > 0
			newSettings[AdventureSettings.Type.BUILD_AND_MINE] = gamemode and 0x02 <= 0
			newSettings[AdventureSettings.Type.WORLD_BUILDER] = gamemode and 0x02 <= 0
			newSettings[AdventureSettings.Type.ALLOW_FLIGHT] = gamemode and 0x01 > 0
			newSettings[AdventureSettings.Type.NO_CLIP] = gamemode == 0x03
			newSettings[AdventureSettings.Type.FLYING] = gamemode == 0x03
		}
		var ev: PlayerGameModeChangeEvent
		server.pluginManager.callEvent(PlayerGameModeChangeEvent(this, gamemode, newSettings).also { ev = it })
		if (ev.isCancelled) {
			return false
		}
		this.gamemode = gamemode
		if (isSpectator) {
			keepMovement = true
			despawnFromAll()
		} else {
			keepMovement = false
			spawnToAll()
		}
		namedTag.putInt("playerGameType", this.gamemode)
		if (!clientSide) {
			val pk = SetPlayerGameTypePacket()
			pk.gamemode = getClientFriendlyGamemode(gamemode)
			this.dataPacket(pk)
		}
		setAdventureSettings(ev.newAdventureSettings)
		if (isSpectator) {
			adventureSettings!![AdventureSettings.Type.FLYING] = true
			this.teleport(temporalVector.setComponents(x, y + 0.1, z))
			val inventoryContentPacket = InventoryContentPacket()
			inventoryContentPacket.inventoryId = InventoryContentPacket.SPECIAL_CREATIVE
			this.dataPacket(inventoryContentPacket)
		} else {
			if (isSurvival) {
				adventureSettings!![AdventureSettings.Type.FLYING] = false
			}
			val inventoryContentPacket = InventoryContentPacket()
			inventoryContentPacket.inventoryId = InventoryContentPacket.SPECIAL_CREATIVE
			inventoryContentPacket.slots = Item.getCreativeItems().toTypedArray()
			this.dataPacket(inventoryContentPacket)
		}
		resetFallDistance()
		inventory.sendContents(this)
		inventory.sendContents(this.viewers.values)
		inventory.sendHeldItem(hasSpawned.values)
		offhandInventory.sendContents(this)
		offhandInventory.sendContents(this.viewers.values)
		inventory.sendCreativeContents()
		return true
	}

	@Deprecated("")
	fun sendSettings() {
		adventureSettings!!.update()
	}

	val isSurvival: Boolean
		get() = gamemode == SURVIVAL

	val isCreative: Boolean
		get() = gamemode == CREATIVE

	val isSpectator: Boolean
		get() = gamemode == SPECTATOR

	val isAdventure: Boolean
		get() = gamemode == ADVENTURE

	override fun getDrops(): Array<Item> {
		return if (!isCreative && !isSpectator) {
			super.getDrops()
		} else arrayOfNulls(0)
	}

	override fun setDataProperty(data: EntityData<*>): Boolean {
		return setDataProperty(data, true)
	}

	override fun setDataProperty(data: EntityData<*>, send: Boolean): Boolean {
		if (super.setDataProperty(data, send)) {
			if (send) this.sendData(this, EntityMetadata().put(getDataProperty(data.id)))
			return true
		}
		return false
	}

	override fun checkGroundState(movX: Double, movY: Double, movZ: Double, dx: Double, dy: Double, dz: Double) {
		if (!onGround || movX != 0.0 || movY != 0.0 || movZ != 0.0) {
			var onGround = false
			val bb = boundingBox.clone()
			bb.maxY = bb.minY + 0.5
			bb.minY = bb.minY - 1
			val realBB = boundingBox.clone()
			realBB.maxY = realBB.minY + 0.1
			realBB.minY = realBB.minY - 0.2
			val minX = NukkitMath.floorDouble(bb.minX)
			val minY = NukkitMath.floorDouble(bb.minY)
			val minZ = NukkitMath.floorDouble(bb.minZ)
			val maxX = NukkitMath.ceilDouble(bb.maxX)
			val maxY = NukkitMath.ceilDouble(bb.maxY)
			val maxZ = NukkitMath.ceilDouble(bb.maxZ)
			for (z in minZ..maxZ) {
				for (x in minX..maxX) {
					for (y in minY..maxY) {
						val block = level.getBlock(temporalVector.setComponents(x.toDouble(), y.toDouble(), z.toDouble()))
						if (!block.canPassThrough() && block.collidesWithBB(realBB)) {
							onGround = true
							break
						}
					}
				}
			}
			this.onGround = onGround
		}
		isCollided = onGround
	}

	override fun checkBlockCollision() {
		var portal = false
		for (block in getCollisionBlocks()) {
			if (block.id == Block.NETHER_PORTAL) {
				portal = true
				continue
			}
			block.onEntityCollide(this)
		}
		if (portal) {
			if (isCreative && inPortalTicks < 80) {
				inPortalTicks = 80
			} else {
				inPortalTicks++
			}
		} else {
			inPortalTicks = 0
		}
	}

	protected fun checkNearEntities() {
		for (entity in level.getNearbyEntities(boundingBox.grow(1.0, 0.5, 1.0), this)) {
			entity.scheduleUpdate()
			if (!entity.isAlive || !this.isAlive) {
				continue
			}
			pickupEntity(entity, true)
		}
	}

	protected fun processMovement(tickDiff: Int) {
		if (!this.isAlive || !spawned || newPosition == null || teleportPosition != null || isSleeping()) {
			return
		}
		val newPos = newPosition
		val distanceSquared = newPos!!.distanceSquared(this)
		var revert = false
		if (distanceSquared / (tickDiff * tickDiff).toDouble() > 100 && newPos.y - y > -5) {
			revert = true
		} else {
			if (chunk == null || !chunk.isGenerated) {
				val chunk = level.getChunk(newPos.x.toInt() shr 4, newPos.z.toInt() shr 4, false)
				if (chunk == null || !chunk.isGenerated) {
					revert = true
					nextChunkOrderRun = 0
				} else {
					if (this.chunk != null) {
						this.chunk.removeEntity(this)
					}
					this.chunk = chunk
				}
			}
		}
		val tdx = newPos.x - x
		val tdz = newPos.z - z
		var distance = Math.sqrt(tdx * tdx + tdz * tdz)
		if (!revert && distanceSquared != 0.0) {
			val dx = newPos.x - x
			val dy = newPos.y - y
			val dz = newPos.z - z
			fastMove(dx, dy, dz)
			if (newPosition == null) {
				return  //maybe solve that in better way
			}
			val diffX = x - newPos.x
			var diffY = y - newPos.y
			val diffZ = z - newPos.z
			val yS = 0.5 + ySize
			if (diffY >= -yS || diffY <= yS) {
				diffY = 0.0
			}
			if (diffX != 0.0 || diffY != 0.0 || diffZ != 0.0) {
				if (checkMovement && !server.allowFlight && (isSurvival || isAdventure)) {
					// Some say: I cant move my head when riding because the server
					// blocked my movement
					if (!isSleeping() && riding == null && !hasEffect(Effect.LEVITATION)) {
						val diffHorizontalSqr = (diffX * diffX + diffZ * diffZ) / (tickDiff * tickDiff).toDouble()
						if (diffHorizontalSqr > 0.5) {
							var ev: PlayerInvalidMoveEvent
							getServer().pluginManager.callEvent(PlayerInvalidMoveEvent(this, true).also { ev = it })
							if (!ev.isCancelled) {
								revert = ev.isRevert
								if (revert) {
									server.logger.warning(getServer().language.translateString("nukkit.player.invalidMove", this.name))
								}
							}
						}
					}
				}
				x = newPos.x
				y = newPos.y
				z = newPos.z
				val radius = this.width / 2.toDouble()
				boundingBox.setBounds(x - radius, y, z - radius, x + radius, y + this.height, z + radius)
			}
		}
		val from = Location(
				lastX,
				lastY,
				lastZ,
				lastYaw,
				lastPitch,
				level)
		val to = this.location
		val delta = Math.pow(lastX - to.x, 2.0) + Math.pow(lastY - to.y, 2.0) + Math.pow(z - to.z, 2.0)
		val deltaAngle = Math.abs(lastYaw - to.yaw) + Math.abs(lastPitch - to.pitch)
		if (!revert && (delta > 0.0001 || deltaAngle > 1.0)) {
			val isFirst = firstMove
			firstMove = false
			lastX = to.x
			lastY = to.y
			lastZ = to.z
			lastYaw = to.yaw
			lastPitch = to.pitch
			if (!isFirst) {
				val blocksAround: List<Block> = ArrayList(blocksAround)
				val collidingBlocks: List<Block> = ArrayList(collisionBlocks)
				val ev = PlayerMoveEvent(this, from, to)
				this.blocksAround = null
				collisionBlocks = null
				server.pluginManager.callEvent(ev)
				if (!ev.isCancelled.also { revert = it }) { //Yes, this is intended
					if (to != ev.to) { //If plugins modify the destination
						this.teleport(ev.to, null)
					} else {
						addMovement(x, y, z, yaw, pitch, yaw)
					}
					//Biome biome = Biome.biomes[level.getBiomeId(this.getFloorX(), this.getFloorZ())];
					//sendTip(biome.getName() + " (" + biome.doesOverhang() + " " + biome.getBaseHeight() + "-" + biome.getHeightVariation() + ")");
				} else {
					this.blocksAround = blocksAround
					collisionBlocks = collidingBlocks
				}
			}
			if (speed == null) speed = Vector3(from.x - to.x, from.y - to.y, from.z - to.z) else speed!!.setComponents(from.x - to.x, from.y - to.y, from.z - to.z)
		} else {
			if (speed == null) speed = Vector3(0, 0, 0) else speed!!.setComponents(0.0, 0.0, 0.0)
		}
		if (!revert && (isFoodEnabled || getServer().getDifficulty() == 0)) {
			if (isSurvival || isAdventure /* && !this.getRiddingOn() instanceof Entity*/) {

				//UpdateFoodExpLevel
				if (distance >= 0.05) {
					var jump = 0.0
					val swimming: Double = if (this.isInsideOfWater) 0.015 * distance else 0
					if (swimming != 0.0) distance = 0.0
					if (this.isSprinting) {  //Running
						if (inAirTicks == 3 && swimming == 0.0) {
							jump = 0.7
						}
						foodData!!.updateFoodExpLevel(0.06 * distance + jump + swimming)
					} else {
						if (inAirTicks == 3 && swimming == 0.0) {
							jump = 0.2
						}
						foodData!!.updateFoodExpLevel(0.01 * distance + jump + swimming)
					}
				}
			}
		}
		if (revert) {
			lastX = from.x
			lastY = from.y
			lastZ = from.z
			lastYaw = from.yaw
			lastPitch = from.pitch

			// We have to send slightly above otherwise the player will fall into the ground.
			sendPosition(from.add(0.0, 0.00001, 0.0), from.yaw, from.pitch, MovePlayerPacket.MODE_RESET)
			//this.sendSettings();
			forceMovement = Vector3(from.x, from.y + 0.00001, from.z)
		} else {
			forceMovement = null
			if (distanceSquared != 0.0 && nextChunkOrderRun > 20) {
				nextChunkOrderRun = 20
			}
		}
		newPosition = null
	}

	override fun addMovement(x: Double, y: Double, z: Double, yaw: Double, pitch: Double, headYaw: Double) {
		sendPosition(Vector3(x, y, z), yaw, pitch, MovePlayerPacket.MODE_NORMAL, this.viewers.values.toTypedArray())
	}

	override fun setMotion(motion: Vector3): Boolean {
		if (super.setMotion(motion)) {
			if (chunk != null) {
				addMotion(motionX, motionY, motionZ) //Send to others
				val pk = SetEntityMotionPacket()
				pk.eid = id
				pk.motionX = motion.x.toFloat()
				pk.motionY = motion.y.toFloat()
				pk.motionZ = motion.z.toFloat()
				this.dataPacket(pk) //Send to self
			}
			if (motionY > 0) {
				//todo: check this
				startAirTicks = (-Math.log(this.gravity / (this.gravity + this.drag * motionY)) / this.drag * 2 + 5).toInt()
			}
			return true
		}
		return false
	}

	fun sendAttributes() {
		val pk = UpdateAttributesPacket()
		pk.entityId = getId()
		pk.entries = arrayOf(
				Attribute.getAttribute(Attribute.MAX_HEALTH).setMaxValue(maxHealth.toFloat()).setValue(if (health > 0) if (health < maxHealth) health else maxHealth else 0),
				Attribute.getAttribute(Attribute.MAX_HUNGER).setValue(foodData!!.level.toFloat()),
				Attribute.getAttribute(Attribute.MOVEMENT_SPEED).setValue(getMovementSpeed()),
				Attribute.getAttribute(Attribute.EXPERIENCE_LEVEL).setValue(experienceLevel.toFloat()),
				Attribute.getAttribute(Attribute.EXPERIENCE).setValue(experience.toFloat() / calculateRequireExperience(experienceLevel))
		)
		this.dataPacket(pk)
	}

	override fun onUpdate(currentTick: Int): Boolean {
		if (!loggedIn) {
			return false
		}
		val tickDiff = currentTick - lastUpdate
		if (tickDiff <= 0) {
			return true
		}
		messageCounter = 2
		lastUpdate = currentTick
		if (fishing != null && server.tick % 20 == 0) {
			if (distance(fishing) > 33) {
				stopFishing(false)
			}
		}
		if (!this.isAlive && spawned) {
			++deadTicks
			if (deadTicks >= 10) {
				despawnFromAll()
			}
			return true
		}
		if (spawned) {
			processMovement(tickDiff)
			if (!isSpectator) {
				checkNearEntities()
			}
			this.entityBaseTick(tickDiff)
			if (getServer().getDifficulty() == 0 && level.getGameRules().getBoolean(GameRule.NATURAL_REGENERATION)) {
				if (getHealth() < maxHealth && ticksLived % 20 == 0) {
					this.heal(1f)
				}
				val foodData = foodData
				if (foodData!!.level < 20 && ticksLived % 10 == 0) {
					foodData.addFoodLevel(1, 0f)
				}
			}
			if (this.isOnFire && lastUpdate % 10 == 0) {
				if (isCreative && !this.isInsideOfFire) {
					extinguish()
				} else if (level.isRaining) {
					if (level.canBlockSeeSky(this)) {
						extinguish()
					}
				}
			}
			if (!isSpectator && speed != null) {
				if (onGround) {
					if (inAirTicks != 0) {
						startAirTicks = 5
					}
					inAirTicks = 0
					highestPosition = y
				} else {
					if (checkMovement && !this.isGliding && !server.allowFlight && !adventureSettings!![AdventureSettings.Type.ALLOW_FLIGHT] && inAirTicks > 20 && !isSleeping() && !this.isImmobile && !this.isSwimming && riding == null && !hasEffect(Effect.LEVITATION)) {
						val expectedVelocity = -this.gravity / this.drag.toDouble() - -this.gravity / this.drag.toDouble() * Math.exp(-this.drag.toDouble() * (inAirTicks - startAirTicks).toDouble())
						val diff = (speed!!.y - expectedVelocity) * (speed!!.y - expectedVelocity)
						val block = level.getBlock(this).id
						val ignore = block == Block.LADDER || block == Block.VINES || block == Block.COBWEB
						if (!hasEffect(Effect.JUMP) && diff > 0.6 && expectedVelocity < speed!!.y && !ignore) {
							if (inAirTicks < 150) {
								setMotion(Vector3(0, expectedVelocity, 0))
							} else if (this.kick(PlayerKickEvent.Reason.FLYING_DISABLED, "Flying is not enabled on this server")) {
								return false
							}
						}
						if (ignore) {
							resetFallDistance()
						}
					}
					if (y > highestPosition) {
						highestPosition = y
					}
					if (this.isGliding) resetFallDistance()
					++inAirTicks
				}
				if (isSurvival || isAdventure) {
					if (foodData != null) foodData!!.update(tickDiff)
				}
			}
		}
		checkTeleportPosition()
		checkInteractNearby()
		if (spawned && dummyBossBars.size > 0 && currentTick % 100 == 0) {
			dummyBossBars.values.forEach(Consumer { obj: DummyBossBar? -> obj!!.updateBossEntityPosition() })
		}
		return true
	}

	fun checkInteractNearby() {
		val interactDistance = if (isCreative) 5 else 3
		buttonText = if (canInteract(this, interactDistance.toDouble())) {
			if (getEntityPlayerLookingAt(interactDistance) != null) {
				val onInteract = getEntityPlayerLookingAt(interactDistance)
				onInteract!!.interactButtonText
			} else {
				""
			}
		} else {
			""
		}
	}

	/**
	 * Returns the Entity the player is looking at currently
	 *
	 * @param maxDistance the maximum distance to check for entities
	 * @return Entity|null    either NULL if no entity is found or an instance of the entity
	 */
	fun getEntityPlayerLookingAt(maxDistance: Int): EntityInteractable? {
		timing.startTiming()
		var entity: EntityInteractable? = null

		// just a fix because player MAY not be fully initialized
		if (temporalVector != null) {
			val nearbyEntities = level.getNearbyEntities(boundingBox.grow(maxDistance.toDouble(), maxDistance.toDouble(), maxDistance.toDouble()), this)

			// get all blocks in looking direction until the max interact distance is reached (it's possible that startblock isn't found!)
			try {
				val itr = BlockIterator(level, position, directionVector, eyeHeight.toDouble(), maxDistance)
				if (itr.hasNext()) {
					var block: Block
					while (itr.hasNext()) {
						block = itr.next()
						entity = getEntityAtPosition(nearbyEntities, block.floorX, block.floorY, block.floorZ)
						if (entity != null) {
							break
						}
					}
				}
			} catch (ex: Exception) {
				// nothing to log here!
			}
		}
		timing.stopTiming()
		return entity
	}

	private fun getEntityAtPosition(nearbyEntities: Array<Entity>, x: Int, y: Int, z: Int): EntityInteractable? {
		for (nearestEntity in nearbyEntities) {
			if (nearestEntity.floorX == x && nearestEntity.floorY == y && nearestEntity.floorZ == z && nearestEntity is EntityInteractable
					&& (nearestEntity as EntityInteractable).canDoInteraction()) {
				return nearestEntity
			}
		}
		return null
	}

	fun checkNetwork() {
		if (!this.isOnline) {
			return
		}
		if (!batchedPackets.isEmpty()) {
			val pArr = arrayOf(this)
			val iter: Iterator<Map.Entry<Int, MutableList<DataPacket>>> = batchedPackets.entries.iterator()
			while (iter.hasNext()) {
				val entry = iter.next()
				val packets = entry.value
				val arr: Array<DataPacket?> = packets.toTypedArray()
				packets.clear()
				server.batchPackets(pArr, arr, false)
			}
			batchedPackets.clear()
		}
		if (nextChunkOrderRun-- <= 0 || chunk == null) {
			orderChunks()
		}
		if (!loadQueue.isEmpty() || !spawned) {
			sendNextChunk()
		}
	}

	@JvmOverloads
	fun canInteract(pos: Vector3, maxDistance: Double, maxDiff: Double = 6.0): Boolean {
		if (distanceSquared(pos) > maxDistance * maxDistance) {
			return false
		}
		val dV = this.directionPlane
		val dot = dV.dot(Vector2(x, z))
		val dot1 = dV.dot(Vector2(pos.x, pos.z))
		return dot1 - dot >= -maxDiff
	}

	protected fun processLogin() {
		if (!server.isWhitelisted(this.name.toLowerCase())) {
			this.kick(PlayerKickEvent.Reason.NOT_WHITELISTED, "Server is white-listed")
			return
		} else if (this.isBanned) {
			this.kick(PlayerKickEvent.Reason.NAME_BANNED, "You are banned")
			return
		} else if (server.iPBans.isBanned(address)) {
			this.kick(PlayerKickEvent.Reason.IP_BANNED, "You are banned")
			return
		}
		if (this.hasPermission(Server.BROADCAST_CHANNEL_USERS)) {
			server.pluginManager.subscribeToPermission(Server.BROADCAST_CHANNEL_USERS, this)
		}
		if (this.hasPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE)) {
			server.pluginManager.subscribeToPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE, this)
		}
		var oldPlayer: Player? = null
		for (p in ArrayList(server.onlinePlayers.values)) {
			if (p !== this && p.name != null && p.name.equals(this.name, ignoreCase = true) || this.uniqueId == p.uniqueId) {
				oldPlayer = p
				break
			}
		}
		val nbt: CompoundTag?
		if (oldPlayer != null) {
			oldPlayer.saveNBT()
			nbt = oldPlayer.namedTag
			oldPlayer.close("", "disconnectionScreen.loggedinOtherLocation")
		} else {
			val legacyDataFile = File(server.dataPath + "players/" + username!!.toLowerCase() + ".dat")
			val dataFile = File(server.dataPath + "players/" + uuid.toString() + ".dat")
			if (legacyDataFile.exists() && !dataFile.exists()) {
				nbt = server.getOfflinePlayerData(username!!, false)
				if (!legacyDataFile.delete()) {
					log.warn("Could not delete legacy player data for {}", username)
				}
			} else {
				nbt = server.getOfflinePlayerData(uuid, true)
			}
		}
		if (nbt == null) {
			this.close(leaveMessage, "Invalid data")
			return
		}
		if (loginChainData!!.isXboxAuthed && server.getPropertyBoolean("xbox-auth") || !server.getPropertyBoolean("xbox-auth")) {
			server.updateName(uuid, username!!)
		}
		playedBefore = nbt.getLong("lastPlayed") - nbt.getLong("firstPlayed") > 1
		nbt.putString("NameTag", username)
		val exp = nbt.getInt("EXP")
		val expLevel = nbt.getInt("expLevel")
		setExperience(exp, expLevel)
		gamemode = nbt.getInt("playerGameType") and 0x03
		if (server.forceGamemode) {
			gamemode = server.gamemode
			nbt.putInt("playerGameType", gamemode)
		}
		adventureSettings = AdventureSettings(this)
				.set(AdventureSettings.Type.WORLD_IMMUTABLE, isAdventure || isSpectator)
				.set(AdventureSettings.Type.WORLD_BUILDER, !isAdventure && !isSpectator)
				.set(AdventureSettings.Type.AUTO_JUMP, true)
				.set(AdventureSettings.Type.ALLOW_FLIGHT, isCreative)
				.set(AdventureSettings.Type.NO_CLIP, isSpectator)
		var level: Level?
		if (server.getLevelByName(nbt.getString("Level")).also { level = it } == null) {
			setLevel(server.defaultLevel)
			nbt.putString("Level", this.level.name)
			nbt.getList("Pos", DoubleTag::class.java)
					.add(DoubleTag("0", this.level.spawnLocation.x))
					.add(DoubleTag("1", this.level.spawnLocation.y))
					.add(DoubleTag("2", this.level.spawnLocation.z))
		} else {
			setLevel(level)
		}
		for (achievement in nbt.getCompound("Achievements").allTags) {
			if (achievement !is ByteTag) {
				continue
			}
			if (achievement.getData() > 0) {
				achievements.add(achievement.getName())
			}
		}
		nbt.putLong("lastPlayed", System.currentTimeMillis() / 1000)
		val uuid = uniqueId
		nbt.putLong("UUIDLeast", uuid.leastSignificantBits)
		nbt.putLong("UUIDMost", uuid.mostSignificantBits)
		if (server.getAutoSave()) {
			server.saveOfflinePlayerData(this.uuid, nbt, true)
		}
		sendPlayStatus(PlayStatusPacket.LOGIN_SUCCESS)
		server.onPlayerLogin(this)
		val posList = nbt.getList("Pos", DoubleTag::class.java)
		super.init(this.level.getChunk(posList[0].data.toInt() shr 4, posList[2].data.toInt() shr 4, true), nbt)
		if (!namedTag.contains("foodLevel")) {
			namedTag.putInt("foodLevel", 20)
		}
		val foodLevel = namedTag.getInt("foodLevel")
		if (!namedTag.contains("FoodSaturationLevel")) {
			namedTag.putFloat("FoodSaturationLevel", 20f)
		}
		val foodSaturationLevel = namedTag.getFloat("foodSaturationLevel")
		foodData = PlayerFood(this, foodLevel, foodSaturationLevel)
		if (isSpectator) keepMovement = true
		teleportPosition = this.position
		forceMovement = teleportPosition
		val infoPacket = ResourcePacksInfoPacket()
		infoPacket.resourcePackEntries = server.resourcePackManager.resourceStack
		infoPacket.mustAccept = server.forceResources
		this.dataPacket(infoPacket)
	}

	protected fun completeLoginSequence() {
		var ev: PlayerLoginEvent
		server.pluginManager.callEvent(PlayerLoginEvent(this, "Plugin reason").also { ev = it })
		if (ev.isCancelled) {
			this.close(leaveMessage, ev.kickMessage)
			return
		}
		val level = server.getLevelByName(namedTag.getString("SpawnLevel"))
		if (level != null) {
			spawnPosition = Position(namedTag.getInt("SpawnX").toDouble(), namedTag.getInt("SpawnY").toDouble(), namedTag.getInt("SpawnZ").toDouble(), level)
		} else {
			spawnPosition = this.level.safeSpawn
		}
		spawnPosition = spawn
		val startGamePacket = StartGamePacket()
		startGamePacket.entityUniqueId = id
		startGamePacket.entityRuntimeId = id
		startGamePacket.playerGamemode = getClientFriendlyGamemode(gamemode)
		startGamePacket.x = x.toFloat()
		startGamePacket.y = y.toFloat()
		startGamePacket.z = z.toFloat()
		startGamePacket.yaw = yaw.toFloat()
		startGamePacket.pitch = pitch.toFloat()
		startGamePacket.seed = -1
		startGamePacket.dimension =  /*(byte) (this.level.getDimension() & 0xff)*/0
		startGamePacket.worldGamemode = getClientFriendlyGamemode(gamemode)
		startGamePacket.difficulty = server.getDifficulty()
		startGamePacket.spawnX = spawnPosition!!.floorX
		startGamePacket.spawnY = spawnPosition!!.floorY
		startGamePacket.spawnZ = spawnPosition!!.floorZ
		startGamePacket.hasAchievementsDisabled = true
		startGamePacket.dayCycleStopTime = -1
		startGamePacket.rainLevel = 0f
		startGamePacket.lightningLevel = 0f
		startGamePacket.commandsEnabled = isEnableClientCommand()
		startGamePacket.gameRules = level.getGameRules()
		startGamePacket.levelId = ""
		startGamePacket.worldName = getServer().network.name
		startGamePacket.generator = 1 //0 old, 1 infinite, 2 flat
		this.dataPacket(startGamePacket)
		this.dataPacket(BiomeDefinitionListPacket())
		this.dataPacket(AvailableEntityIdentifiersPacket())
		loggedIn = true
		this.level.sendTime(this)
		sendAttributes()
		this.isNameTagVisible = true
		this.isNameTagAlwaysVisible = true
		this.setCanClimb(true)
		server.logger.info(getServer().language.translateString("nukkit.player.logIn",
				TextFormat.AQUA.toString() + username + TextFormat.WHITE,
				address, port.toString(), id.toString(),
				this.level.name, NukkitMath.round(x, 4).toString(), NukkitMath.round(y, 4).toString(), NukkitMath.round(z, 4).toString()))
		if (this.isOp || this.hasPermission("nukkit.textcolor")) {
			removeFormat = false
		}
		server.addOnlinePlayer(this)
		server.onPlayerCompleteLoginSequence(this)
	}

	fun handleDataPacket(packet: DataPacket) {
		if (!isConnected) {
			return
		}
		Timings.getReceiveDataPacketTiming(packet).use { timing ->
			val ev = DataPacketReceiveEvent(this, packet)
			server.pluginManager.callEvent(ev)
			if (ev.isCancelled) {
				return
			}
			if (packet.pid() == ProtocolInfo.BATCH_PACKET) {
				server.network.processBatch(packet as BatchPacket, this)
				return
			}
			if (log.isTraceEnabled()) {
				log.trace("Inbound {}: {}", this.name, packet)
			}
			packetswitch@ when (packet.pid()) {
				ProtocolInfo.LOGIN_PACKET -> {
					if (loggedIn) {
						break
					}
					val loginPacket = packet as LoginPacket
					val message: String
					if (!ProtocolInfo.SUPPORTED_PROTOCOLS.contains(loginPacket.getProtocol())) {
						if (loginPacket.getProtocol() < ProtocolInfo.CURRENT_PROTOCOL) {
							message = "disconnectionScreen.outdatedClient"
							sendPlayStatus(PlayStatusPacket.LOGIN_FAILED_CLIENT)
						} else {
							message = "disconnectionScreen.outdatedServer"
							sendPlayStatus(PlayStatusPacket.LOGIN_FAILED_SERVER)
						}
						if (packet.protocol < 137) {
							val disconnectPacket = DisconnectPacket()
							disconnectPacket.message = message
							disconnectPacket.encode()
							val batch = BatchPacket()
							batch.payload = disconnectPacket.buffer
							this.directDataPacket(batch)
							// Still want to run close() to allow the player to be removed properly
						}
						this.close("", message, false)
						break
					}
					username = TextFormat.clean(loginPacket.username)
					displayName = username
					iusername = username.toLowerCase()
					this.setDataProperty(StringEntityData(Entity.DATA_NAMETAG, username), false)
					loginChainData = ClientChainData.read(loginPacket)
					if (!loginChainData.isXboxAuthed() && server.getPropertyBoolean("xbox-auth")) {
						this.close("", "disconnectionScreen.notAuthenticated")
						break
					}
					if (server.onlinePlayers.size >= server.maxPlayers && this.kick(PlayerKickEvent.Reason.SERVER_FULL, "disconnectionScreen.serverFull", false)) {
						break
					}
					clientId = loginPacket.clientId
					uuid = loginPacket.clientUUID
					rawUUID = Binary.writeUUID(uuid)
					var valid = true
					val len = loginPacket.username.length
					if (len > 16 || len < 3) {
						valid = false
					}
					var i = 0
					while (i < len && valid) {
						val c = loginPacket.username[i]
						if (c >= 'a' && c <= 'z' ||
								c >= 'A' && c <= 'Z' ||
								c >= '0' && c <= '9' || c == '_' || c == ' ') {
							i++
							continue
						}
						valid = false
						break
						i++
					}
					if (!valid || iusername == "rcon" || iusername == "console") {
						this.close("", "disconnectionScreen.invalidName")
						break
					}
					if (!loginPacket.skin.isValid) {
						this.close("", "disconnectionScreen.invalidSkin")
						break
					} else {
						setSkin(loginPacket.skin)
					}
					var playerPreLoginEvent: PlayerPreLoginEvent
					server.pluginManager.callEvent(PlayerPreLoginEvent(this, "Plugin reason").also { playerPreLoginEvent = it })
					if (playerPreLoginEvent.isCancelled) {
						this.close("", playerPreLoginEvent.kickMessage)
						break
					}
					val playerInstance = this
					preLoginEventTask = object : AsyncTask() {
						private var e: PlayerAsyncPreLoginEvent? = null
						override fun onRun() {
							e = PlayerAsyncPreLoginEvent(username, uuid, address, port)
							server.pluginManager.callEvent(e)
						}

						override fun onCompletion(server: Server) {
							if (!playerInstance.closed) {
								if (e!!.loginResult == LoginResult.KICK) {
									playerInstance.close(e!!.kickMessage, e!!.kickMessage)
								} else if (playerInstance.shouldLogin) {
									playerInstance.completeLoginSequence()
								}
								for (action in e!!.scheduledActions) {
									action.accept(server)
								}
							}
						}
					}
					server.scheduler.scheduleAsyncTask(preLoginEventTask)
					processLogin()
				}
				ProtocolInfo.RESOURCE_PACK_CLIENT_RESPONSE_PACKET -> {
					val responsePacket = packet as ResourcePackClientResponsePacket
					when (responsePacket.responseStatus) {
						ResourcePackClientResponsePacket.STATUS_REFUSED -> this.close("", "disconnectionScreen.noReason")
						ResourcePackClientResponsePacket.STATUS_SEND_PACKS -> for (entry in responsePacket.packEntries) {
							val resourcePack = server.resourcePackManager.getPackById(entry.uuid)
							if (resourcePack == null) {
								this.close("", "disconnectionScreen.resourcePack")
								break
							}
							val dataInfoPacket = ResourcePackDataInfoPacket()
							dataInfoPacket.packId = resourcePack.packId
							dataInfoPacket.maxChunkSize = 1048576 //megabyte
							dataInfoPacket.chunkCount = resourcePack.packSize / dataInfoPacket.maxChunkSize
							dataInfoPacket.compressedPackSize = resourcePack.packSize.toLong()
							dataInfoPacket.sha256 = resourcePack.sha256
							this.dataPacket(dataInfoPacket)
						}
						ResourcePackClientResponsePacket.STATUS_HAVE_ALL_PACKS -> {
							val stackPacket = ResourcePackStackPacket()
							stackPacket.mustAccept = server.forceResources
							stackPacket.resourcePackStack = server.resourcePackManager.resourceStack
							this.dataPacket(stackPacket)
						}
						ResourcePackClientResponsePacket.STATUS_COMPLETED -> if (preLoginEventTask!!.isFinished) {
							completeLoginSequence()
						} else {
							shouldLogin = true
						}
					}
				}
				ProtocolInfo.RESOURCE_PACK_CHUNK_REQUEST_PACKET -> {
					val requestPacket = packet as ResourcePackChunkRequestPacket
					val resourcePack = server.resourcePackManager.getPackById(requestPacket.packId)
					if (resourcePack == null) {
						this.close("", "disconnectionScreen.resourcePack")
						break
					}
					val dataPacket = ResourcePackChunkDataPacket()
					dataPacket.packId = resourcePack.packId
					dataPacket.chunkIndex = requestPacket.chunkIndex
					dataPacket.data = resourcePack.getPackChunk(1048576 * requestPacket.chunkIndex, 1048576)
					dataPacket.progress = 1048576 * requestPacket.chunkIndex.toLong()
					this.dataPacket(dataPacket)
				}
				ProtocolInfo.PLAYER_SKIN_PACKET -> {
					val skinPacket = packet as PlayerSkinPacket
					val skin = skinPacket.skin
					if (!skin.isValid) {
						break
					}
					val playerChangeSkinEvent = PlayerChangeSkinEvent(this, skin)
					playerChangeSkinEvent.isCancelled = TimeUnit.SECONDS.toMillis(server.playerSkinChangeCooldown.toLong()) > System.currentTimeMillis() - lastSkinChange
					server.pluginManager.callEvent(playerChangeSkinEvent)
					if (!playerChangeSkinEvent.isCancelled) {
						lastSkinChange = System.currentTimeMillis()
						setSkin(skin)
					}
				}
				ProtocolInfo.PLAYER_INPUT_PACKET -> {
					if (!this.isAlive || !spawned) {
						break
					}
					val ipk = packet as PlayerInputPacket
					if (riding is EntityMinecartAbstract) {
						(riding as EntityMinecartAbstract).setCurrentSpeed(ipk.motionY.toDouble())
					}
				}
				ProtocolInfo.MOVE_PLAYER_PACKET -> {
					if (teleportPosition != null) {
						break
					}
					val movePlayerPacket = packet as MovePlayerPacket
					val newPos = Vector3(movePlayerPacket.x.toDouble(), (movePlayerPacket.y - this.eyeHeight).toDouble(), movePlayerPacket.z.toDouble())
					if (newPos.distanceSquared(this) < 0.01 && movePlayerPacket.yaw % 360.toDouble() == yaw && movePlayerPacket.pitch % 360.toDouble() == pitch) {
						break
					}
					if (newPos.distanceSquared(this) > 100) {
						sendPosition(this, movePlayerPacket.yaw.toDouble(), movePlayerPacket.pitch.toDouble(), MovePlayerPacket.MODE_RESET)
						break
					}
					var revert = false
					if (!this.isAlive || !spawned) {
						revert = true
						forceMovement = Vector3(x, y, z)
					}
					if (forceMovement != null && (newPos.distanceSquared(forceMovement) > 0.1 || revert)) {
						sendPosition(forceMovement, movePlayerPacket.yaw.toDouble(), movePlayerPacket.pitch.toDouble(), MovePlayerPacket.MODE_RESET)
					} else {
						movePlayerPacket.yaw %= 360f
						movePlayerPacket.pitch %= 360f
						if (movePlayerPacket.yaw < 0) {
							movePlayerPacket.yaw += 360f
						}
						setRotation(movePlayerPacket.yaw.toDouble(), movePlayerPacket.pitch.toDouble())
						newPosition = newPos
						forceMovement = null
					}
					if (riding != null) {
						if (riding is EntityBoat) {
							riding.setPositionAndRotation(temporalVector.setComponents(movePlayerPacket.x.toDouble(), movePlayerPacket.y - 1.toDouble(), movePlayerPacket.z.toDouble()), (movePlayerPacket.headYaw + 90) % 360.toDouble(), 0.0)
						}
					}
				}
				ProtocolInfo.ADVENTURE_SETTINGS_PACKET -> {
					//TODO: player abilities, check for other changes
					val adventureSettingsPacket = packet as AdventureSettingsPacket
					if (!server.allowFlight && adventureSettingsPacket.getFlag(AdventureSettingsPacket.FLYING) && !adventureSettings!![AdventureSettings.Type.ALLOW_FLIGHT]) {
						this.kick(PlayerKickEvent.Reason.FLYING_DISABLED, "Flying is not enabled on this server")
						break
					}
					val playerToggleFlightEvent = PlayerToggleFlightEvent(this, adventureSettingsPacket.getFlag(AdventureSettingsPacket.FLYING))
					server.pluginManager.callEvent(playerToggleFlightEvent)
					if (playerToggleFlightEvent.isCancelled) {
						adventureSettings!!.update()
					} else {
						adventureSettings!!.set(AdventureSettings.Type.FLYING, playerToggleFlightEvent.isFlying)
					}
				}
				ProtocolInfo.MOB_EQUIPMENT_PACKET -> {
					if (!spawned || !this.isAlive) {
						break
					}
					val mobEquipmentPacket = packet as MobEquipmentPacket
					val inv = getWindowById(mobEquipmentPacket.windowId)
					if (inv == null) {
						server.logger.debug("Player " + this.name + " has no open container with window ID " + mobEquipmentPacket.windowId)
						return
					}
					val item = inv.getItem(mobEquipmentPacket.hotbarSlot)
					if (item != mobEquipmentPacket.item) {
						server.logger.debug("Tried to equip " + mobEquipmentPacket.item + " but have " + item + " in target slot")
						inv.sendContents(this)
						return
					}
					if (inv is PlayerInventory) {
						inv.equipItem(mobEquipmentPacket.hotbarSlot)
					}
					this.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_ACTION, false)
				}
				ProtocolInfo.PLAYER_ACTION_PACKET -> {
					val playerActionPacket = packet as PlayerActionPacket
					if (!spawned || !this.isAlive && playerActionPacket.action != PlayerActionPacket.ACTION_RESPAWN && playerActionPacket.action != PlayerActionPacket.ACTION_DIMENSION_CHANGE_REQUEST) {
						break
					}
					playerActionPacket.entityId = id
					val pos = Vector3(playerActionPacket.x.toDouble(), playerActionPacket.y.toDouble(), playerActionPacket.z.toDouble())
					val face = BlockFace.fromIndex(playerActionPacket.face)
					actionswitch@ when (playerActionPacket.action) {
						PlayerActionPacket.ACTION_START_BREAK -> {
							val currentBreak = System.currentTimeMillis()
							val currentBreakPosition = BlockVector3(playerActionPacket.x, playerActionPacket.y, playerActionPacket.z)
							// HACK: Client spams multiple left clicks so we need to skip them.
							if (lastBreakPosition == currentBreakPosition && currentBreak - lastBreak < 10 || pos.distanceSquared(this) > 100) {
								break
							}
							val target = level.getBlock(pos)
							val playerInteractEvent = PlayerInteractEvent(this, inventory.itemInHand, target, face, if (target.id == 0) PlayerInteractEvent.Action.LEFT_CLICK_AIR else PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)
							getServer().pluginManager.callEvent(playerInteractEvent)
							if (playerInteractEvent.isCancelled) {
								inventory.sendHeldItem(this)
								break
							}
							when (target.id) {
								Block.NOTEBLOCK -> {
									(target as BlockNoteblock).emitSound()
									break@actionswitch
								}
								Block.DRAGON_EGG -> {
									(target as BlockDragonEgg).teleport()
									break@actionswitch
								}
							}
							val block = target.getSide(face)
							if (block.id == Block.FIRE) {
								level.setBlock(block, Block.get(BlockID.AIR), true)
								level.addLevelSoundEvent(block, LevelSoundEventPacket.SOUND_EXTINGUISH_FIRE)
								break
							}
							if (!isCreative) {
								//improved this to take stuff like swimming, ladders, enchanted tools into account, fix wrong tool break time calculations for bad tools (pmmp/PocketMine-MP#211)
								//Done by lmlstarqaq
								val breakTime = Math.ceil(target.getBreakTime(inventory.itemInHand, this) * 20)
								if (breakTime > 0) {
									val pk = LevelEventPacket()
									pk.evid = LevelEventPacket.EVENT_BLOCK_START_BREAK
									pk.x = pos.x.toFloat()
									pk.y = pos.y.toFloat()
									pk.z = pos.z.toFloat()
									pk.data = (65535 / breakTime).toInt()
									level.addChunkPacket(pos.floorX shr 4, pos.floorZ shr 4, pk)
								}
							}
							breakingBlock = target
							lastBreak = currentBreak
							lastBreakPosition = currentBreakPosition
						}
						PlayerActionPacket.ACTION_ABORT_BREAK, PlayerActionPacket.ACTION_STOP_BREAK -> {
							val pk = LevelEventPacket()
							pk.evid = LevelEventPacket.EVENT_BLOCK_STOP_BREAK
							pk.x = pos.x.toFloat()
							pk.y = pos.y.toFloat()
							pk.z = pos.z.toFloat()
							pk.data = 0
							level.addChunkPacket(pos.floorX shr 4, pos.floorZ shr 4, pk)
							breakingBlock = null
						}
						PlayerActionPacket.ACTION_GET_UPDATED_BLOCK -> {
						}
						PlayerActionPacket.ACTION_DROP_ITEM -> {
						}
						PlayerActionPacket.ACTION_STOP_SLEEPING -> stopSleep()
						PlayerActionPacket.ACTION_RESPAWN -> {
							if (!spawned || this.isAlive || !this.isOnline) {
								break
							}
							respawn()
						}
						PlayerActionPacket.ACTION_JUMP -> break@packetswitch
						PlayerActionPacket.ACTION_START_SPRINT -> {
							val playerToggleSprintEvent = PlayerToggleSprintEvent(this, true)
							server.pluginManager.callEvent(playerToggleSprintEvent)
							if (playerToggleSprintEvent.isCancelled) {
								this.sendData(this)
							} else {
								this.isSprinting = true
							}
							break@packetswitch
						}
						PlayerActionPacket.ACTION_STOP_SPRINT -> {
							playerToggleSprintEvent = PlayerToggleSprintEvent(this, false)
							server.pluginManager.callEvent(playerToggleSprintEvent)
							if (playerToggleSprintEvent.isCancelled()) {
								this.sendData(this)
							} else {
								this.isSprinting = false
							}
							break@packetswitch
						}
						PlayerActionPacket.ACTION_START_SNEAK -> {
							val playerToggleSneakEvent = PlayerToggleSneakEvent(this, true)
							server.pluginManager.callEvent(playerToggleSneakEvent)
							if (playerToggleSneakEvent.isCancelled) {
								this.sendData(this)
							} else {
								this.isSneaking = true
							}
							break@packetswitch
						}
						PlayerActionPacket.ACTION_STOP_SNEAK -> {
							playerToggleSneakEvent = PlayerToggleSneakEvent(this, false)
							server.pluginManager.callEvent(playerToggleSneakEvent)
							if (playerToggleSneakEvent.isCancelled()) {
								this.sendData(this)
							} else {
								this.isSneaking = false
							}
							break@packetswitch
						}
						PlayerActionPacket.ACTION_DIMENSION_CHANGE_ACK -> sendPosition(this, yaw, pitch, MovePlayerPacket.MODE_NORMAL)
						PlayerActionPacket.ACTION_START_GLIDE -> {
							val playerToggleGlideEvent = PlayerToggleGlideEvent(this, true)
							server.pluginManager.callEvent(playerToggleGlideEvent)
							if (playerToggleGlideEvent.isCancelled) {
								this.sendData(this)
							} else {
								this.isGliding = true
							}
							break@packetswitch
						}
						PlayerActionPacket.ACTION_STOP_GLIDE -> {
							playerToggleGlideEvent = PlayerToggleGlideEvent(this, false)
							server.pluginManager.callEvent(playerToggleGlideEvent)
							if (playerToggleGlideEvent.isCancelled()) {
								this.sendData(this)
							} else {
								this.isGliding = false
							}
							break@packetswitch
						}
						PlayerActionPacket.ACTION_CONTINUE_BREAK -> if (isBreakingBlock()) {
							block = level.getBlock(pos)
							level.addParticle(PunchBlockParticle(pos, block, face))
						}
						PlayerActionPacket.ACTION_START_SWIMMING -> {
							val ptse = PlayerToggleSwimEvent(this, true)
							server.pluginManager.callEvent(ptse)
							if (ptse.isCancelled) {
								this.sendData(this)
							} else {
								this.isSwimming = true
							}
						}
						PlayerActionPacket.ACTION_STOP_SWIMMING -> {
							ptse = PlayerToggleSwimEvent(this, false)
							server.pluginManager.callEvent(ptse)
							if (ptse.isCancelled()) {
								this.sendData(this)
							} else {
								this.isSwimming = false
							}
						}
					}
					this.usingItem = false
				}
				ProtocolInfo.MOB_ARMOR_EQUIPMENT_PACKET -> {
				}
				ProtocolInfo.MODAL_FORM_RESPONSE_PACKET -> {
					if (!spawned || !this.isAlive) {
						break
					}
					val modalFormPacket = packet as ModalFormResponsePacket
					if (formWindows.containsKey(modalFormPacket.formId)) {
						val window = formWindows.remove(modalFormPacket.formId)
						window!!.setResponse(modalFormPacket.data.trim { it <= ' ' })
						val event = PlayerFormRespondedEvent(this, modalFormPacket.formId, window)
						getServer().pluginManager.callEvent(event)
					} else if (serverSettings.containsKey(modalFormPacket.formId)) {
						val window = serverSettings[modalFormPacket.formId]
						window!!.setResponse(modalFormPacket.data.trim { it <= ' ' })
						val event = PlayerSettingsRespondedEvent(this, modalFormPacket.formId, window)
						getServer().pluginManager.callEvent(event)

						//Set back new settings if not been cancelled
						if (!event.isCancelled && window is FormWindowCustom) window.setElementsFromResponse()
					}
				}
				ProtocolInfo.INTERACT_PACKET -> {
					if (!spawned || !this.isAlive) {
						break
					}
					craftingType = CRAFTING_SMALL
					//this.resetCraftingGridType();
					val interactPacket = packet as InteractPacket
					val targetEntity = level.getEntity(interactPacket.target)
					if (targetEntity == null || !this.isAlive || !targetEntity.isAlive) {
						break
					}
					if (targetEntity is EntityItem || targetEntity is EntityArrow || targetEntity is EntityXPOrb) {
						this.kick(PlayerKickEvent.Reason.INVALID_PVE, "Attempting to interact with an invalid entity")
						server.logger.warning(getServer().language.translateString("nukkit.player.invalidEntity", this.name))
						break
					}
					item = inventory.itemInHand
					when (interactPacket.action) {
						InteractPacket.ACTION_MOUSEOVER -> {
							if (interactPacket.target == 0L) {
								break@packetswitch
							}
							getServer().pluginManager.callEvent(PlayerMouseOverEntityEvent(this, targetEntity))
						}
						InteractPacket.ACTION_VEHICLE_EXIT -> {
							if (targetEntity !is EntityRideable || riding == null) {
								break
							}
							(riding as EntityRideable).mountEntity(this)
						}
					}
				}
				ProtocolInfo.BLOCK_PICK_REQUEST_PACKET -> {
					val pickRequestPacket = packet as BlockPickRequestPacket
					val block = level.getBlock(temporalVector.setComponents(pickRequestPacket.x.toDouble(), pickRequestPacket.y.toDouble(), pickRequestPacket.z.toDouble()))
					item = block.toItem()
					if (pickRequestPacket.addUserData) {
						val blockEntity = level.getBlockEntity(Vector3(pickRequestPacket.x.toDouble(), pickRequestPacket.y.toDouble(), pickRequestPacket.z.toDouble()))
						if (blockEntity != null) {
							val nbt = blockEntity.cleanedNBT
							if (nbt != null) {
								item.setCustomBlockData(nbt)
								item.setLore("+(DATA)")
							}
						}
					}
					val pickEvent = PlayerBlockPickEvent(this, block, item)
					if (isSpectator) {
						log.debug("Got block-pick request from " + this.name + " when in spectator mode")
						pickEvent.setCancelled()
					}
					server.pluginManager.callEvent(pickEvent)
					if (!pickEvent.isCancelled) {
						var itemExists = false
						var itemSlot = -1
						run {
							var slot = 0
							while (slot < this.inventory.size) {
								if (this.inventory.getItem(slot) == pickEvent.item) {
									if (slot < this.inventory.hotbarSize) {
										this.inventory.heldItemSlot = slot
									} else {
										itemSlot = slot
									}
									itemExists = true
									break
								}
								slot++
							}
						}
						var slot = 0
						while (slot < inventory.hotbarSize) {
							if (inventory.getItem(slot).isNull) {
								if (!itemExists && isCreative) {
									inventory.heldItemSlot = slot
									inventory.itemInHand = pickEvent.item
									break@packetswitch
								} else if (itemSlot > -1) {
									inventory.heldItemSlot = slot
									inventory.itemInHand = inventory.getItem(itemSlot)
									inventory.clear(itemSlot, true)
									break@packetswitch
								}
							}
							slot++
						}
						if (!itemExists && isCreative) {
							val itemInHand = inventory.itemInHand
							inventory.itemInHand = pickEvent.item
							if (!inventory.isFull) {
								var slot = 0
								while (slot < inventory.size) {
									if (inventory.getItem(slot).isNull) {
										inventory.setItem(slot, itemInHand)
										break
									}
									slot++
								}
							}
						} else if (itemSlot > -1) {
							val itemInHand = inventory.itemInHand
							inventory.itemInHand = inventory.getItem(itemSlot)
							inventory.setItem(itemSlot, itemInHand)
						}
					}
				}
				ProtocolInfo.ANIMATE_PACKET -> {
					if (!spawned || !this.isAlive) {
						break
					}
					val animationEvent = PlayerAnimationEvent(this, (packet as AnimatePacket).action)
					server.pluginManager.callEvent(animationEvent)
					if (animationEvent.isCancelled) {
						break
					}
					val animation = animationEvent.animationType
					when (animation) {
						AnimatePacket.Action.ROW_RIGHT, AnimatePacket.Action.ROW_LEFT -> if (riding is EntityBoat) {
							(riding as EntityBoat).onPaddle(animation, packet.rowingTime)
						}
					}
					val animatePacket = AnimatePacket()
					animatePacket.eid = getId()
					animatePacket.action = animationEvent.animationType
					broadcastPacket(this.viewers.values, animatePacket)
				}
				ProtocolInfo.SET_HEALTH_PACKET -> {
				}
				ProtocolInfo.ENTITY_EVENT_PACKET -> {
					if (!spawned || !this.isAlive) {
						break
					}
					craftingType = CRAFTING_SMALL
					//this.resetCraftingGridType();
					val entityEventPacket = packet as EntityEventPacket
					when (entityEventPacket.event) {
						EntityEventPacket.EATING_ITEM -> {
							if (entityEventPacket.data == 0 || entityEventPacket.eid != id) {
								break
							}
							entityEventPacket.eid = id
							entityEventPacket.isEncoded = false
							this.dataPacket(entityEventPacket)
							broadcastPacket(this.viewers.values, entityEventPacket)
						}
					}
				}
				ProtocolInfo.COMMAND_REQUEST_PACKET -> {
					if (!spawned || !this.isAlive) {
						break
					}
					craftingType = CRAFTING_SMALL
					val commandRequestPacket = packet as CommandRequestPacket
					val playerCommandPreprocessEvent = PlayerCommandPreprocessEvent(this, commandRequestPacket.command)
					server.pluginManager.callEvent(playerCommandPreprocessEvent)
					if (playerCommandPreprocessEvent.isCancelled) {
						break
					}
					Timings.playerCommandTimer.startTiming()
					server.dispatchCommand(playerCommandPreprocessEvent.player, playerCommandPreprocessEvent.message.substring(1))
					Timings.playerCommandTimer.stopTiming()
				}
				ProtocolInfo.TEXT_PACKET -> {
					if (!spawned || !this.isAlive) {
						break
					}
					val textPacket = packet as TextPacket
					if (textPacket.type == TextPacket.TYPE_CHAT) {
						var chatMessage = textPacket.message
						val breakLine = chatMessage.indexOf('\n')
						// Chat messages shouldn't contain break lines so ignore text afterwards
						if (breakLine != -1) {
							chatMessage = chatMessage.substring(0, breakLine)
						}
						chat(chatMessage)
					}
				}
				ProtocolInfo.CONTAINER_CLOSE_PACKET -> {
					val containerClosePacket = packet as ContainerClosePacket
					if (!spawned) {
						break
					}
					if (windowIndex.containsKey(containerClosePacket.windowId)) {
						server.pluginManager.callEvent(InventoryCloseEvent(windowIndex[containerClosePacket.windowId], this))
						removeWindow(windowIndex[containerClosePacket.windowId])
					} else {
						windowIndex.remove(containerClosePacket.windowId)
					}
					if (containerClosePacket.windowId == -1) {
						craftingType = CRAFTING_SMALL
						resetCraftingGridType()
						addWindow(craftingGrid, ContainerIds.NONE)
					}
				}
				ProtocolInfo.CRAFTING_EVENT_PACKET -> {
				}
				ProtocolInfo.BLOCK_ENTITY_DATA_PACKET -> {
					if (!spawned || !this.isAlive) {
						break
					}
					val blockEntityDataPacket = packet as BlockEntityDataPacket
					craftingType = CRAFTING_SMALL
					resetCraftingGridType()
					pos = Vector3(blockEntityDataPacket.x.toDouble(), blockEntityDataPacket.y.toDouble(), blockEntityDataPacket.z.toDouble())
					if (pos.distanceSquared(this) > 10000) {
						break
					}
					val t = level.getBlockEntity(pos)
					if (t is BlockEntitySpawnable) {
						val nbt: CompoundTag
						nbt = try {
							NBTIO.read(blockEntityDataPacket.namedTag, ByteOrder.LITTLE_ENDIAN, true)
						} catch (e: IOException) {
							throw RuntimeException(e)
						}
						if (!t.updateCompoundTag(nbt, this)) {
							t.spawnTo(this)
						}
					}
				}
				ProtocolInfo.REQUEST_CHUNK_RADIUS_PACKET -> {
					val requestChunkRadiusPacket = packet as RequestChunkRadiusPacket
					val chunkRadiusUpdatePacket = ChunkRadiusUpdatedPacket()
					chunkRadius = Math.max(3, Math.min(requestChunkRadiusPacket.radius, viewDistance))
					chunkRadiusUpdatePacket.radius = chunkRadius
					this.dataPacket(chunkRadiusUpdatePacket)
				}
				ProtocolInfo.SET_PLAYER_GAME_TYPE_PACKET -> {
					val setPlayerGameTypePacket = packet as SetPlayerGameTypePacket
					if (setPlayerGameTypePacket.gamemode != gamemode) {
						if (!this.hasPermission("nukkit.command.gamemode")) {
							val setPlayerGameTypePacket1 = SetPlayerGameTypePacket()
							setPlayerGameTypePacket1.gamemode = gamemode and 0x01
							this.dataPacket(setPlayerGameTypePacket1)
							adventureSettings!!.update()
							break
						}
						this.setGamemode(setPlayerGameTypePacket.gamemode, true)
						Command.broadcastCommandMessage(this, TranslationContainer("commands.gamemode.success.self", getGamemodeString(gamemode)))
					}
				}
				ProtocolInfo.ITEM_FRAME_DROP_ITEM_PACKET -> {
					val itemFrameDropItemPacket = packet as ItemFrameDropItemPacket
					var vector3 = temporalVector.setComponents(itemFrameDropItemPacket.x.toDouble(), itemFrameDropItemPacket.y.toDouble(), itemFrameDropItemPacket.z.toDouble())
					val blockEntityItemFrame = level.getBlockEntity(vector3)
					val itemFrame = blockEntityItemFrame as BlockEntityItemFrame
					if (itemFrame != null) {
						block = itemFrame.block
						val itemDrop = itemFrame.item
						val itemFrameDropItemEvent = ItemFrameDropItemEvent(this, block, itemFrame, itemDrop)
						server.pluginManager.callEvent(itemFrameDropItemEvent)
						if (!itemFrameDropItemEvent.isCancelled) {
							if (itemDrop.id != Item.AIR) {
								vector3 = temporalVector.setComponents(itemFrame.x + 0.5, itemFrame.y, itemFrame.z + 0.5)
								level.dropItem(vector3, itemDrop)
								itemFrame.item = ItemBlock(Block.get(BlockID.AIR))
								itemFrame.itemRotation = 0
								level.addSound(this, Sound.BLOCK_ITEMFRAME_REMOVE_ITEM)
							}
						} else {
							itemFrame.spawnTo(this)
						}
					}
				}
				ProtocolInfo.MAP_INFO_REQUEST_PACKET -> {
					val pk = packet as MapInfoRequestPacket
					var mapItem: Item? = null
					for (item1 in inventory.contents.values) {
						if (item1 is ItemMap && item1.mapId == pk.mapId) {
							mapItem = item1
						}
					}
					if (mapItem == null) {
						for (be in level.blockEntities.values) {
							if (be is BlockEntityItemFrame) {
								val itemFrame1 = be
								if (itemFrame1.item is ItemMap && (itemFrame1.item as ItemMap).mapId == pk.mapId) {
									(itemFrame1.item as ItemMap).sendImage(this)
									break
								}
							}
						}
					}
					if (mapItem != null) {
						var event: PlayerMapInfoRequestEvent
						getServer().pluginManager.callEvent(PlayerMapInfoRequestEvent(this, mapItem).also { event = it })
						if (!event.isCancelled) {
							(mapItem as ItemMap).sendImage(this)
						}
					}
				}
				ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V1, ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V2, ProtocolInfo.LEVEL_SOUND_EVENT_PACKET -> if (!isSpectator || (packet as LevelSoundEventPacket).sound != LevelSoundEventPacket.SOUND_HIT && packet.sound != LevelSoundEventPacket.SOUND_ATTACK_NODAMAGE) {
					level.addChunkPacket(this.chunkX, this.chunkZ, packet)
				}
				ProtocolInfo.INVENTORY_TRANSACTION_PACKET -> {
					if (isSpectator) {
						sendAllInventories()
						break
					}
					val transactionPacket = packet as InventoryTransactionPacket
					val actions: MutableList<InventoryAction> = ArrayList()
					for (networkInventoryAction in transactionPacket.actions) {
						val a = networkInventoryAction.createInventoryAction(this)
						if (a == null) {
							getServer().logger.debug("Unmatched inventory action from " + this.name + ": " + networkInventoryAction)
							sendAllInventories()
							break@packetswitch
						}
						actions.add(a)
					}
					if (transactionPacket.isCraftingPart) {
						if (craftingTransaction == null) {
							craftingTransaction = CraftingTransaction(this, actions)
						} else {
							for (action in actions) {
								craftingTransaction!!.addAction(action)
							}
						}
						if (craftingTransaction!!.primaryOutput != null) {
							//we get the actions for this in several packets, so we can't execute it until we get the result
							craftingTransaction!!.execute()
							craftingTransaction = null
						}
						return
					} else if (craftingTransaction != null) {
						server.logger.debug("Got unexpected normal inventory action with incomplete crafting transaction from " + this.name + ", refusing to execute crafting")
						craftingTransaction = null
					}
					when (transactionPacket.transactionType) {
						InventoryTransactionPacket.TYPE_NORMAL -> {
							val transaction = InventoryTransaction(this, actions)
							if (!transaction.execute()) {
								server.logger.debug("Failed to execute inventory transaction from " + this.name + " with actions: " + Arrays.toString(transactionPacket.actions))
								break@packetswitch  //oops!
							}

							//TODO: fix achievement for getting iron from furnace
							break@packetswitch
						}
						InventoryTransactionPacket.TYPE_MISMATCH -> {
							if (transactionPacket.actions.size > 0) {
								server.logger.debug("Expected 0 actions for mismatch, got " + transactionPacket.actions.size + ", " + Arrays.toString(transactionPacket.actions))
							}
							sendAllInventories()
							break@packetswitch
						}
						InventoryTransactionPacket.TYPE_USE_ITEM -> {
							val useItemData = transactionPacket.transactionData as UseItemData
							val blockVector = useItemData.blockPos
							face = useItemData.face
							val type = useItemData.actionType
							when (type) {
								InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_BLOCK -> {
									// Remove if client bug is ever fixed
									val spamBug = lastRightClickPos != null && System.currentTimeMillis() - lastRightClickTime < 100.0 && blockVector.distanceSquared(lastRightClickPos) < 0.00001
									lastRightClickPos = blockVector.asVector3()
									lastRightClickTime = System.currentTimeMillis().toDouble()
									if (spamBug) {
										return
									}
									this.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_ACTION, false)
									if (canInteract(blockVector.add(0.5, 0.5, 0.5), if (isCreative) 13 else 7.toDouble())) {
										if (isCreative) {
											val i = inventory.itemInHand
											if (level.useItemOn(blockVector.asVector3(), i, face, useItemData.clickPos.x, useItemData.clickPos.y, useItemData.clickPos.z, this) != null) {
												break@packetswitch
											}
										} else if (inventory.itemInHand == useItemData.itemInHand) {
											var i = inventory.itemInHand
											val oldItem = i.clone()
											//TODO: Implement adventure mode checks
											if (level.useItemOn(blockVector.asVector3(), i, face, useItemData.clickPos.x, useItemData.clickPos.y, useItemData.clickPos.z, this).also { i = it } != null) {
												if (i != oldItem || i.getCount() != oldItem.getCount()) {
													inventory.itemInHand = i
													inventory.sendHeldItem(this.viewers.values)
												}
												break@packetswitch
											}
										}
									}
									inventory.sendHeldItem(this)
									if (blockVector.distanceSquared(this) > 10000) {
										break@packetswitch
									}
									val target = level.getBlock(blockVector.asVector3())
									block = target.getSide(face)
									level.sendBlocks(arrayOf(this), arrayOf(target, block), UpdateBlockPacket.FLAG_ALL_PRIORITY)
									break@packetswitch
								}
								InventoryTransactionPacket.USE_ITEM_ACTION_BREAK_BLOCK -> {
									if (!spawned || !this.isAlive) {
										break@packetswitch
									}
									resetCraftingGridType()
									var i = inventory.itemInHand
									val oldItem = i.clone()
									if (canInteract(blockVector.add(0.5, 0.5, 0.5), if (isCreative) 13 else 7.toDouble()) && level.useBreakOn(blockVector.asVector3(), face, i, this, true).also { i = it } != null) {
										if (isSurvival) {
											foodData!!.updateFoodExpLevel(0.025)
											if (i != oldItem || i.getCount() != oldItem.getCount()) {
												inventory.itemInHand = i
												inventory.sendHeldItem(this.viewers.values)
											}
										}
										break@packetswitch
									}
									inventory.sendContents(this)
									target = level.getBlock(blockVector.asVector3())
									val blockEntity = level.getBlockEntity(blockVector.asVector3())
									level.sendBlocks(arrayOf(this), arrayOf<Block>(target), UpdateBlockPacket.FLAG_ALL_PRIORITY)
									inventory.sendHeldItem(this)
									if (blockEntity is BlockEntitySpawnable) {
										blockEntity.spawnTo(this)
									}
									break@packetswitch
								}
								InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_AIR -> {
									val directionVector = this.directionVector
									if (isCreative) {
										item = inventory.itemInHand
									} else if (inventory.itemInHand != useItemData.itemInHand) {
										inventory.sendHeldItem(this)
										break@packetswitch
									} else {
										item = inventory.itemInHand
									}
									val interactEvent = PlayerInteractEvent(this, item, directionVector, face, PlayerInteractEvent.Action.RIGHT_CLICK_AIR)
									server.pluginManager.callEvent(interactEvent)
									if (interactEvent.isCancelled) {
										inventory.sendHeldItem(this)
										break@packetswitch
									}
									if (item.onClickAir(this, directionVector)) {
										if (isSurvival) {
											inventory.itemInHand = item
										}
										if (!isUsingItem) {
											this.usingItem = true
											break@packetswitch
										}

										// Used item
										val ticksUsed = server.tick - startActionTick
										this.usingItem = false
										if (!item.onUse(this, ticksUsed)) {
											inventory.sendContents(this)
										}
									}
									break@packetswitch
								}
								else -> {
								}
							}
						}
						InventoryTransactionPacket.TYPE_USE_ITEM_ON_ENTITY -> {
							val useItemOnEntityData = transactionPacket.transactionData as UseItemOnEntityData
							val target = level.getEntity(useItemOnEntityData.entityRuntimeId) ?: return
							type = useItemOnEntityData.actionType
							if (!useItemOnEntityData.itemInHand.equalsExact(inventory.itemInHand)) {
								inventory.sendHeldItem(this)
							}
							item = inventory.itemInHand
							when (type) {
								InventoryTransactionPacket.USE_ITEM_ON_ENTITY_ACTION_INTERACT -> {
									val playerInteractEntityEvent = PlayerInteractEntityEvent(this, target, item, useItemOnEntityData.clickPos)
									if (isSpectator) playerInteractEntityEvent.setCancelled()
									getServer().pluginManager.callEvent(playerInteractEntityEvent)
									if (playerInteractEntityEvent.isCancelled) {
										break
									}
									if (target.onInteract(this, item, useItemOnEntityData.clickPos) && isSurvival) {
										if (item.isTool()) {
											if (item.useOn(target) && item.getDamage() >= item.getMaxDurability()) {
												item = ItemBlock(Block.get(BlockID.AIR))
											}
										} else {
											if (item.count > 1) {
												item.count--
											} else {
												item = ItemBlock(Block.get(BlockID.AIR))
											}
										}
										inventory.setItemInHand(item)
									}
								}
								InventoryTransactionPacket.USE_ITEM_ON_ENTITY_ACTION_ATTACK -> {
									var itemDamage: Float = item.getAttackDamage().toFloat()
									for (enchantment in item.getEnchantments()) {
										itemDamage += enchantment.getDamageBonus(target).toFloat()
									}
									val damage: MutableMap<DamageModifier, Float> = EnumMap(DamageModifier::class.java)
									damage[DamageModifier.BASE] = itemDamage
									if (!canInteract(target, if (isCreative) 8 else 5.toDouble())) {
										break
									} else if (target is Player) {
										if (target.gamemode and 0x01 > 0) {
											break
										} else if (!server.getPropertyBoolean("pvp")) {
											break
										}
									}
									val entityDamageByEntityEvent = EntityDamageByEntityEvent(this, target, DamageCause.ENTITY_ATTACK, damage)
									if (isSpectator) entityDamageByEntityEvent.setCancelled()
									if (target is Player && !level.getGameRules().getBoolean(GameRule.PVP)) {
										entityDamageByEntityEvent.setCancelled()
									}
									if (!target.attack(entityDamageByEntityEvent)) {
										if (item.isTool() && isSurvival) {
											inventory.sendContents(this)
										}
										break
									}
									for (enchantment in item.getEnchantments()) {
										enchantment.doPostAttack(this, target)
									}
									if (item.isTool() && isSurvival) {
										if (item.useOn(target) && item.getDamage() >= item.getMaxDurability()) {
											inventory.itemInHand = ItemBlock(Block.get(BlockID.AIR))
										} else {
											inventory.itemInHand = item
										}
									}
									return
								}
								else -> {
								}
							}
						}
						InventoryTransactionPacket.TYPE_RELEASE_ITEM -> {
							if (isSpectator) {
								sendAllInventories()
								break@packetswitch
							}
							val releaseItemData = transactionPacket.transactionData as ReleaseItemData
							try {
								type = releaseItemData.actionType
								when (type) {
									InventoryTransactionPacket.RELEASE_ITEM_ACTION_RELEASE -> {
										if (isUsingItem) {
											item = inventory.itemInHand
											val ticksUsed = server.tick - startActionTick
											if (!item.onRelease(this, ticksUsed)) {
												inventory.sendContents(this)
											}
											this.usingItem = false
										} else {
											inventory.sendContents(this)
										}
										return
									}
									InventoryTransactionPacket.RELEASE_ITEM_ACTION_CONSUME -> {
										log.debug("Unexpected release item action consume from {}", ::getName)
										return
									}
									else -> {
									}
								}
							} finally {
								this.usingItem = false
							}
						}
						else -> inventory.sendContents(this)
					}
				}
				ProtocolInfo.PLAYER_HOTBAR_PACKET -> {
					val hotbarPacket = packet as PlayerHotbarPacket
					if (hotbarPacket.windowId != ContainerIds.INVENTORY) {
						return  //In PE this should never happen
					}
					inventory.equipItem(hotbarPacket.selectedHotbarSlot)
				}
				ProtocolInfo.SERVER_SETTINGS_REQUEST_PACKET -> {
					val settingsRequestEvent = PlayerServerSettingsRequestEvent(this, HashMap(serverSettings))
					getServer().pluginManager.callEvent(settingsRequestEvent)
					if (!settingsRequestEvent.isCancelled) {
						settingsRequestEvent.settings.forEach { (id: Int?, window: FormWindow) ->
							val re = ServerSettingsResponsePacket()
							re.formId = id!!
							re.data = window.jsonData
							this.dataPacket(re)
						}
					}
				}
				ProtocolInfo.RESPAWN_PACKET -> {
					if (this.isAlive) {
						break
					}
					val respawnPacket = packet as RespawnPacket
					if (respawnPacket.respawnState == RespawnPacket.STATE_CLIENT_READY_TO_SPAWN) {
						val respawn1 = RespawnPacket()
						respawn1.x = x.toFloat()
						respawn1.y = getY().toFloat()
						respawn1.z = z.toFloat()
						respawn1.respawnState = RespawnPacket.STATE_READY_TO_SPAWN
						this.dataPacket(respawn1)
					}
				}
				ProtocolInfo.BOOK_EDIT_PACKET -> {
					val bookEditPacket = packet as BookEditPacket
					val oldBook = inventory.getItem(bookEditPacket.inventorySlot)
					if (oldBook.id != Item.BOOK_AND_QUILL) {
						return
					}
					var newBook = oldBook.clone()
					val success: Boolean
					when (bookEditPacket.action) {
						BookEditPacket.Action.REPLACE_PAGE -> success = (newBook as ItemBookAndQuill).setPageText(bookEditPacket.pageNumber, bookEditPacket.text)
						BookEditPacket.Action.ADD_PAGE -> success = (newBook as ItemBookAndQuill).insertPage(bookEditPacket.pageNumber, bookEditPacket.text)
						BookEditPacket.Action.DELETE_PAGE -> success = (newBook as ItemBookAndQuill).deletePage(bookEditPacket.pageNumber)
						BookEditPacket.Action.SWAP_PAGES -> success = (newBook as ItemBookAndQuill).swapPages(bookEditPacket.pageNumber, bookEditPacket.secondaryPageNumber)
						BookEditPacket.Action.SIGN_BOOK -> {
							newBook = Item.get(Item.WRITTEN_BOOK, 0, 1, oldBook.compoundTag)
							success = (newBook as ItemBookWritten).signBook(bookEditPacket.title, bookEditPacket.author, bookEditPacket.xuid, ItemBookWritten.GENERATION_ORIGINAL)
						}
						else -> return
					}
					if (success) {
						val editBookEvent = PlayerEditBookEvent(this, oldBook, newBook, bookEditPacket.action)
						server.pluginManager.callEvent(editBookEvent)
						if (!editBookEvent.isCancelled) {
							inventory.setItem(bookEditPacket.inventorySlot, editBookEvent.newBook)
						}
					}
				}
				else -> {
				}
			}
		}
	}

	/**
	 * Sends a chat message as this player. If the message begins with a / (forward-slash) it will be treated
	 * as a command.
	 * @param message message to send
	 * @return successful
	 */
	fun chat(message: String): Boolean {
		var message = message
		if (!spawned || !this.isAlive) {
			return false
		}
		resetCraftingGridType()
		craftingType = CRAFTING_SMALL
		if (removeFormat) {
			message = TextFormat.clean(message, true)
		}
		for (msg in message.split("\n").toTypedArray()) {
			if (!msg.trim { it <= ' ' }.isEmpty() && msg.length <= 255 && messageCounter-- > 0) {
				val chatEvent = PlayerChatEvent(this, msg)
				server.pluginManager.callEvent(chatEvent)
				if (!chatEvent.isCancelled) {
					server.broadcastMessage(getServer().language.translateString(chatEvent.format, *arrayOf(chatEvent.player.getDisplayName(), chatEvent.message)), chatEvent.recipients)
				}
			}
		}
		return true
	}

	fun kick(reason: String, isAdmin: Boolean): Boolean {
		return this.kick(PlayerKickEvent.Reason.UNKNOWN, reason, isAdmin)
	}

	@JvmOverloads
	fun kick(reason: String = ""): Boolean {
		return kick(PlayerKickEvent.Reason.UNKNOWN, reason)
	}

	@JvmOverloads
	fun kick(reason: PlayerKickEvent.Reason, isAdmin: Boolean = true): Boolean {
		return this.kick(reason, reason.toString(), isAdmin)
	}

	@JvmOverloads
	fun kick(reason: PlayerKickEvent.Reason?, reasonString: String, isAdmin: Boolean = true): Boolean {
		var ev: PlayerKickEvent
		server.pluginManager.callEvent(PlayerKickEvent(this, reason, leaveMessage).also { ev = it })
		if (!ev.isCancelled) {
			val message: String
			message = if (isAdmin) {
				if (!this.isBanned) {
					"Kicked by admin." + if (!reasonString.isEmpty()) " Reason: $reasonString" else ""
				} else {
					reasonString
				}
			} else {
				if (reasonString.isEmpty()) {
					"disconnectionScreen.noReason"
				} else {
					reasonString
				}
			}
			this.close(ev.quitMessage, message)
			return true
		}
		return false
	}

	fun setViewDistance(distance: Int) {
		chunkRadius = distance
		val pk = ChunkRadiusUpdatedPacket()
		pk.radius = distance
		this.dataPacket(pk)
	}

	fun getViewDistance(): Int {
		return chunkRadius
	}

	override fun sendMessage(message: String) {
		val pk = TextPacket()
		pk.type = TextPacket.TYPE_RAW
		pk.message = server.language.translateString(message)
		this.dataPacket(pk)
	}

	override fun sendMessage(message: TextContainer) {
		if (message is TranslationContainer) {
			sendTranslation(message.text, message.parameters)
			return
		}
		this.sendMessage(message.text)
	}

	@JvmOverloads
	fun sendTranslation(message: String?, parameters: Array<String?> = arrayOfNulls(0)) {
		val pk = TextPacket()
		if (!server.isLanguageForced) {
			pk.type = TextPacket.TYPE_TRANSLATION
			pk.message = server.language.translateString(message, parameters, "nukkit.")
			for (i in parameters.indices) {
				parameters[i] = server.language.translateString(parameters[i], parameters, "nukkit.")
			}
			pk.parameters = parameters
		} else {
			pk.type = TextPacket.TYPE_RAW
			pk.message = server.language.translateString(message, *parameters)
		}
		this.dataPacket(pk)
	}

	fun sendChat(message: String?) {
		this.sendChat("", message)
	}

	fun sendChat(source: String?, message: String?) {
		val pk = TextPacket()
		pk.type = TextPacket.TYPE_CHAT
		pk.source = source
		pk.message = server.language.translateString(message)
		this.dataPacket(pk)
	}

	@JvmOverloads
	fun sendPopup(message: String?, subtitle: String? = "") {
		val pk = TextPacket()
		pk.type = TextPacket.TYPE_POPUP
		pk.message = message
		this.dataPacket(pk)
	}

	fun sendTip(message: String?) {
		val pk = TextPacket()
		pk.type = TextPacket.TYPE_TIP
		pk.message = message
		this.dataPacket(pk)
	}

	fun clearTitle() {
		val pk = SetTitlePacket()
		pk.type = SetTitlePacket.TYPE_CLEAR
		this.dataPacket(pk)
	}

	/**
	 * Resets both title animation times and subtitle for the next shown title
	 */
	fun resetTitleSettings() {
		val pk = SetTitlePacket()
		pk.type = SetTitlePacket.TYPE_RESET
		this.dataPacket(pk)
	}

	fun setSubtitle(subtitle: String?) {
		val pk = SetTitlePacket()
		pk.type = SetTitlePacket.TYPE_SUBTITLE
		pk.text = subtitle
		this.dataPacket(pk)
	}

	fun setTitleAnimationTimes(fadein: Int, duration: Int, fadeout: Int) {
		val pk = SetTitlePacket()
		pk.type = SetTitlePacket.TYPE_ANIMATION_TIMES
		pk.fadeInTime = fadein
		pk.stayTime = duration
		pk.fadeOutTime = fadeout
		this.dataPacket(pk)
	}

	private fun setTitle(text: String) {
		val packet = SetTitlePacket()
		packet.text = text
		packet.type = SetTitlePacket.TYPE_TITLE
		this.dataPacket(packet)
	}

	@JvmOverloads
	fun sendTitle(title: String?, subtitle: String? = null, fadeIn: Int = 20, stay: Int = 20, fadeOut: Int = 5) {
		setTitleAnimationTimes(fadeIn, stay, fadeOut)
		if (!Strings.isNullOrEmpty(subtitle)) {
			setSubtitle(subtitle)
		}
		// title won't send if an empty string is used.
		setTitle((if (Strings.isNullOrEmpty(title)) " " else title)!!)
	}

	@JvmOverloads
	fun sendActionBar(title: String?, fadein: Int = 1, duration: Int = 0, fadeout: Int = 1) {
		val pk = SetTitlePacket()
		pk.type = SetTitlePacket.TYPE_ACTION_BAR
		pk.text = title
		pk.fadeInTime = fadein
		pk.stayTime = duration
		pk.fadeOutTime = fadeout
		this.dataPacket(pk)
	}

	override fun close() {
		this.close("")
	}

	@JvmOverloads
	fun close(message: String?, reason: String = "generic", notify: Boolean = true) {
		this.close(TextContainer(message), reason, notify)
	}

	@JvmOverloads
	fun close(message: TextContainer?, reason: String = "generic", notify: Boolean = true) {
		if (isConnected && !closed) {
			if (notify && reason.length > 0) {
				val pk = DisconnectPacket()
				pk.message = reason
				this.directDataPacket(pk)
			}
			isConnected = false
			var ev: PlayerQuitEvent? = null
			if (this.name != null && this.name.length > 0) {
				server.pluginManager.callEvent(PlayerQuitEvent(this, message, true, reason).also { ev = it })
				if (loggedIn && ev!!.autoSave) {
					save()
				}
				if (fishing != null) {
					stopFishing(false)
				}
			}
			for (player in ArrayList(server.onlinePlayers.values)) {
				if (!player.canSee(this)) {
					player.showPlayer(this)
				}
			}
			hiddenPlayers.clear()
			removeAllWindows(true)
			for (index in ArrayList(usedChunks.keys)) {
				val chunkX = Level.getHashX(index)
				val chunkZ = Level.getHashZ(index)
				level.unregisterChunkLoader(this, chunkX, chunkZ)
				usedChunks.remove(index)
				for (entity in level.getChunkEntities(chunkX, chunkZ).values) {
					if (entity !== this) {
						entity.viewers.remove(getLoaderId())
					}
				}
			}
			super.close()
			interfaz.close(this, if (notify) reason else "")
			if (loggedIn) {
				server.removeOnlinePlayer(this)
			}
			loggedIn = false
			if (ev != null && username != "" && spawned && ev!!.quitMessage.toString() != "") {
				server.broadcastMessage(ev!!.quitMessage)
			}
			server.pluginManager.unsubscribeFromPermission(Server.BROADCAST_CHANNEL_USERS, this)
			spawned = false
			server.logger.info(getServer().language.translateString("nukkit.player.logOut",
					TextFormat.AQUA.toString() + (if (this.name == null) "" else this.name) + TextFormat.WHITE,
					address, port.toString(),
					getServer().language.translateString(reason)))
			windows.clear()
			usedChunks.clear()
			loadQueue.clear()
			hasSpawned.clear()
			spawnPosition = null
			if (riding is EntityRideable) {
				riding.passengers.remove(this)
			}
			riding = null
		}
		if (perm != null) {
			perm!!.clearPermissions()
			perm = null
		}
		if (inventory != null) {
			inventory = null
		}
		chunk = null
		server.removePlayer(this)
	}

	@JvmOverloads
	fun save(async: Boolean = false) {
		check(!closed) { "Tried to save closed player" }
		super.saveNBT()
		if (level != null) {
			namedTag.putString("Level", level.folderName)
			if (spawnPosition != null && spawnPosition!!.getLevel() != null) {
				namedTag.putString("SpawnLevel", spawnPosition!!.getLevel().folderName)
				namedTag.putInt("SpawnX", spawnPosition!!.x.toInt())
				namedTag.putInt("SpawnY", spawnPosition!!.y.toInt())
				namedTag.putInt("SpawnZ", spawnPosition!!.z.toInt())
			}
			val achievements = CompoundTag()
			for (achievement in this.achievements) {
				achievements.putByte(achievement, 1)
			}
			namedTag.putCompound("Achievements", achievements)
			namedTag.putInt("playerGameType", gamemode)
			namedTag.putLong("lastPlayed", System.currentTimeMillis() / 1000)
			namedTag.putString("lastIP", address)
			namedTag.putInt("EXP", experience)
			namedTag.putInt("expLevel", experienceLevel)
			namedTag.putInt("foodLevel", foodData!!.level)
			namedTag.putFloat("foodSaturationLevel", foodData!!.foodSaturationLevel)
			if (!username!!.isEmpty() && namedTag != null) {
				server.saveOfflinePlayerData(uuid, namedTag, async)
			}
		}
	}

	override fun getName(): String {
		return username!!
	}

	override fun kill() {
		if (!spawned) {
			return
		}
		val showMessages = level.getGameRules().getBoolean(GameRule.SHOW_DEATH_MESSAGE)
		var message = ""
		val params: MutableList<String?> = ArrayList()
		val cause = getLastDamageCause()
		if (showMessages) {
			params.add(getDisplayName())
			when (if (cause == null) DamageCause.CUSTOM else cause.cause) {
				DamageCause.ENTITY_ATTACK -> if (cause is EntityDamageByEntityEvent) {
					val e = cause.damager
					killer = e
					if (e is Player) {
						message = "death.attack.player"
						params.add(e.getDisplayName())
						break
					} else if (e is EntityLiving) {
						message = "death.attack.mob"
						params.add(if (e.getNameTag() != "") e.getNameTag() else e.getName())
						break
					} else {
						params.add("Unknown")
					}
				}
				DamageCause.PROJECTILE -> if (cause is EntityDamageByEntityEvent) {
					val e = cause.damager
					killer = e
					if (e is Player) {
						message = "death.attack.arrow"
						params.add(e.getDisplayName())
					} else if (e is EntityLiving) {
						message = "death.attack.arrow"
						params.add(if (e.getNameTag() != "") e.getNameTag() else e.getName())
						break
					} else {
						params.add("Unknown")
					}
				}
				DamageCause.VOID -> message = "death.attack.outOfWorld"
				DamageCause.FALL -> {
					if (cause!!.finalDamage > 2) {
						message = "death.fell.accident.generic"
						break
					}
					message = "death.attack.fall"
				}
				DamageCause.SUFFOCATION -> message = "death.attack.inWall"
				DamageCause.LAVA -> {
					val block = level.getBlock(Vector3(x, y - 1, z))
					if (block.id == Block.MAGMA) {
						message = "death.attack.lava.magma"
						break
					}
					message = "death.attack.lava"
				}
				DamageCause.FIRE -> message = "death.attack.onFire"
				DamageCause.FIRE_TICK -> message = "death.attack.inFire"
				DamageCause.DROWNING -> message = "death.attack.drown"
				DamageCause.CONTACT -> if (cause is EntityDamageByBlockEvent) {
					if (cause.damager.id == Block.CACTUS) {
						message = "death.attack.cactus"
					}
				}
				DamageCause.BLOCK_EXPLOSION, DamageCause.ENTITY_EXPLOSION -> if (cause is EntityDamageByEntityEvent) {
					val e = cause.damager
					killer = e
					if (e is Player) {
						message = "death.attack.explosion.player"
						params.add(e.getDisplayName())
					} else if (e is EntityLiving) {
						message = "death.attack.explosion.player"
						params.add(if (e.getNameTag() != "") e.getNameTag() else e.getName())
						break
					} else {
						message = "death.attack.explosion"
					}
				} else {
					message = "death.attack.explosion"
				}
				DamageCause.MAGIC -> message = "death.attack.magic"
				DamageCause.LIGHTNING -> message = "death.attack.lightningBolt"
				DamageCause.HUNGER -> message = "death.attack.starve"
				else -> message = "death.attack.generic"
			}
		}
		val ev = PlayerDeathEvent(this, this.drops, TranslationContainer(message, *params.toTypedArray()), experienceLevel)
		ev.keepExperience = level.gameRules.getBoolean(GameRule.KEEP_INVENTORY)
		ev.keepInventory = ev.keepExperience
		if (cause != null && cause.cause != DamageCause.VOID) {
			val offhandInventory = getOffhandInventory()
			val playerInventory = getInventory()
			if (offhandInventory.getItem(0).id == Item.TOTEM || playerInventory.itemInHand.id == Item.TOTEM) {
				level.addLevelEvent(this, LevelEventPacket.EVENT_SOUND_TOTEM)
				extinguish()
				removeAllEffects()
				setHealth(1f)
				addEffect(Effect.getEffect(Effect.REGENERATION).setDuration(800).setAmplifier(1))
				addEffect(Effect.getEffect(Effect.FIRE_RESISTANCE).setDuration(800).setAmplifier(1))
				addEffect(Effect.getEffect(Effect.ABSORPTION).setDuration(100).setAmplifier(1))
				val pk = EntityEventPacket()
				pk.eid = getId()
				pk.event = EntityEventPacket.CONSUME_TOTEM
				this.dataPacket(pk)
				if (offhandInventory.getItem(0).id == Item.TOTEM) {
					offhandInventory.clear(0)
				} else {
					playerInventory.clear(playerInventory.heldItemIndex)
				}
				ev.isCancelled = true
			}
		}
		server.pluginManager.callEvent(ev)
		if (!ev.isCancelled) {
			if (fishing != null) {
				stopFishing(false)
			}
			health = 0f
			extinguish()
			scheduleUpdate()
			if (!ev.keepInventory && level.getGameRules().getBoolean(GameRule.DO_ENTITY_DROPS)) {
				for (item in ev.drops) {
					level.dropItem(this, item, null, true, 40)
				}
				if (inventory != null) {
					inventory.clearAll()
				}
				if (offhandInventory != null) {
					offhandInventory.clearAll()
				}
			}
			if (!ev.keepExperience && level.getGameRules().getBoolean(GameRule.DO_ENTITY_DROPS)) {
				if (isSurvival || isAdventure) {
					var exp = ev.experience * 7
					if (exp > 100) exp = 100
					level.dropExpOrb(this, exp)
				}
				setExperience(0, 0)
			}
			if (showMessages && !ev.deathMessage.toString().isEmpty()) {
				server.broadcast(ev.deathMessage, Server.BROADCAST_CHANNEL_USERS)
			}
			val pk = RespawnPacket()
			val pos = spawn
			pk.x = pos!!.x.toFloat()
			pk.y = pos.y.toFloat()
			pk.z = pos.z.toFloat()
			pk.respawnState = RespawnPacket.STATE_SEARCHING_FOR_SPAWN
			this.dataPacket(pk)
		}
	}

	protected fun respawn() {
		if (server.isHardcore) {
			this.isBanned = true
			return
		}
		craftingType = CRAFTING_SMALL
		resetCraftingGridType()
		val playerRespawnEvent = PlayerRespawnEvent(this, spawn)
		server.pluginManager.callEvent(playerRespawnEvent)
		val respawnPos = playerRespawnEvent.respawnPosition
		sendExperience()
		sendExperienceLevel()
		this.isSprinting = false
		this.isSneaking = false
		this.setDataProperty(ShortEntityData(Entity.DATA_AIR, 400), false)
		deadTicks = 0
		noDamageTicks = 60
		removeAllEffects()
		setHealth(maxHealth.toFloat())
		foodData!!.setLevel(20, 20f)
		this.sendData(this)
		this.setMovementSpeed(DEFAULT_SPEED)
		adventureSettings!!.update()
		inventory.sendContents(this)
		inventory.sendArmorContents(this)
		offhandInventory.sendContents(this)
		this.teleport(respawnPos, null)
		spawnToAll()
		scheduleUpdate()
	}

	override fun setHealth(health: Float) {
		var health = health
		if (health < 1) {
			health = 0f
		}
		super.setHealth(health)
		//TODO: Remove it in future! This a hack to solve the client-side absorption bug! WFT Mojang (Half a yellow heart cannot be shown, we can test it in local gaming)
		val attr = Attribute.getAttribute(Attribute.MAX_HEALTH).setMaxValue(if (getAbsorption() % 2 != 0f) maxHealth + 1 else maxHealth.toFloat()).setValue(if (health > 0) if (health < maxHealth) health else maxHealth else 0)
		if (spawned) {
			val pk = UpdateAttributesPacket()
			pk.entries = arrayOf(attr)
			pk.entityId = id
			this.dataPacket(pk)
		}
	}

	override fun setMaxHealth(maxHealth: Int) {
		super.setMaxHealth(maxHealth)
		val attr = Attribute.getAttribute(Attribute.MAX_HEALTH).setMaxValue(if (getAbsorption() % 2 != 0f) this.maxHealth + 1 else this.maxHealth.toFloat()).setValue(if (health > 0) if (health < getMaxHealth()) health else getMaxHealth() else 0)
		if (spawned) {
			val pk = UpdateAttributesPacket()
			pk.entries = arrayOf(attr)
			pk.entityId = id
			this.dataPacket(pk)
		}
	}

	var experience: Int
		get() = exp
		set(exp) {
			setExperience(exp, experienceLevel)
		}

	fun addExperience(add: Int) {
		if (add == 0) return
		val now = experience
		var added = now + add
		var level = experienceLevel
		var most = calculateRequireExperience(level)
		while (added >= most) {  //Level Up!
			added = added - most
			level++
			most = calculateRequireExperience(level)
		}
		setExperience(added, level)
	}

	//todo something on performance, lots of exp orbs then lots of packets, could crash client
	fun setExperience(exp: Int, level: Int) {
		this.exp = exp
		experienceLevel = level
		sendExperienceLevel(level)
		sendExperience(exp)
	}

	@JvmOverloads
	fun sendExperience(exp: Int = experience) {
		if (spawned) {
			var percent = exp.toFloat() / calculateRequireExperience(experienceLevel)
			percent = Math.max(0f, Math.min(1f, percent))
			setAttribute(Attribute.getAttribute(Attribute.EXPERIENCE).setValue(percent))
		}
	}

	@JvmOverloads
	fun sendExperienceLevel(level: Int = experienceLevel) {
		if (spawned) {
			setAttribute(Attribute.getAttribute(Attribute.EXPERIENCE_LEVEL).setValue(level.toFloat()))
		}
	}

	fun setAttribute(attribute: Attribute) {
		val pk = UpdateAttributesPacket()
		pk.entries = arrayOf(attribute)
		pk.entityId = id
		this.dataPacket(pk)
	}

	override fun setMovementSpeed(speed: Float) {
		setMovementSpeed(speed, true)
	}

	fun setMovementSpeed(speed: Float, send: Boolean) {
		super.setMovementSpeed(speed)
		if (spawned && send) {
			val attribute = Attribute.getAttribute(Attribute.MOVEMENT_SPEED).setValue(speed)
			setAttribute(attribute)
		}
	}

	override fun attack(source: EntityDamageEvent): Boolean {
		if (!this.isAlive) {
			return false
		}
		if (isSpectator || isCreative && source.cause != DamageCause.SUICIDE) {
			//source.setCancelled();
			return false
		} else if (adventureSettings!![AdventureSettings.Type.ALLOW_FLIGHT] && source.cause == DamageCause.FALL) {
			//source.setCancelled();
			return false
		} else if (source.cause == DamageCause.FALL) {
			if (level.getBlock(this.position.floor().add(0.5, -1.0, 0.5)).id == Block.SLIME_BLOCK) {
				if (!this.isSneaking) {
					//source.setCancelled();
					resetFallDistance()
					return false
				}
			}
		}
		return if (super.attack(source)) { //!source.isCancelled()
			if (getLastDamageCause() === source && spawned) {
				if (source is EntityDamageByEntityEvent) {
					val damager = source.damager
					if (damager is Player) {
						damager.foodData!!.updateFoodExpLevel(0.3)
					}
				}
				val pk = EntityEventPacket()
				pk.eid = id
				pk.event = EntityEventPacket.HURT_ANIMATION
				this.dataPacket(pk)
			}
			true
		} else {
			false
		}
	}

	/**
	 * Drops an item on the ground in front of the player. Returns if the item drop was successful.
	 *
	 * @param item to drop
	 * @return bool if the item was dropped or if the item was null
	 */
	fun dropItem(item: Item): Boolean {
		if (!spawned || !this.isAlive) {
			return false
		}
		if (item.isNull) {
			server.logger.debug(this.name + " attempted to drop a null item (" + item + ")")
			return true
		}
		val motion = this.directionVector.multiply(0.4)
		level.dropItem(this.add(0.0, 1.3, 0.0), item, motion, 40)
		this.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_ACTION, false)
		return true
	}

	@JvmOverloads
	fun sendPosition(pos: Vector3?, yaw: Double = this.yaw, pitch: Double = this.pitch, mode: Int = MovePlayerPacket.MODE_NORMAL, targets: Array<Player?>? = null) {
		val pk = MovePlayerPacket()
		pk.eid = getId()
		pk.x = pos!!.x.toFloat()
		pk.y = (pos.y + this.eyeHeight).toFloat()
		pk.z = pos.z.toFloat()
		pk.headYaw = yaw.toFloat()
		pk.pitch = pitch.toFloat()
		pk.yaw = yaw.toFloat()
		pk.mode = mode
		if (targets != null) {
			broadcastPacket(targets, pk)
		} else {
			pk.eid = id
			this.dataPacket(pk)
		}
	}

	override fun checkChunks() {
		if (chunk == null || chunk.x != x.toInt() shr 4 || chunk.z != z.toInt() shr 4) {
			if (chunk != null) {
				chunk.removeEntity(this)
			}
			chunk = level.getChunk(x.toInt() shr 4, z.toInt() shr 4, true)
			if (!justCreated) {
				val newChunk = level.getChunkPlayers(x.toInt() shr 4, z.toInt() shr 4)
				newChunk.remove(getLoaderId())

				//List<Player> reload = new ArrayList<>();
				for (player in ArrayList(hasSpawned.values)) {
					if (!newChunk.containsKey(player.getLoaderId())) {
						despawnFrom(player)
					} else {
						newChunk.remove(player.getLoaderId())
						//reload.add(player);
					}
				}
				for (player in newChunk.values) {
					spawnTo(player)
				}
			}
			if (chunk == null) {
				return
			}
			chunk.addEntity(this)
		}
	}

	protected fun checkTeleportPosition(): Boolean {
		if (teleportPosition != null) {
			val chunkX = teleportPosition!!.x.toInt() shr 4
			val chunkZ = teleportPosition!!.z.toInt() shr 4
			for (X in -1..1) {
				for (Z in -1..1) {
					val index = Level.chunkHash(chunkX + X, chunkZ + Z)
					if (!usedChunks.containsKey(index) || !usedChunks[index]!!) {
						return false
					}
				}
			}
			spawnToAll()
			forceMovement = teleportPosition
			teleportPosition = null
			return true
		}
		return false
	}

	protected fun sendPlayStatus(status: Int, immediate: Boolean = false) {
		val pk = PlayStatusPacket()
		pk.status = status
		if (immediate) {
			this.directDataPacket(pk)
		} else {
			this.dataPacket(pk)
		}
	}

	override fun teleport(location: Location, cause: TeleportCause): Boolean {
		if (!this.isOnline) {
			return false
		}
		val from = location
		var to = location
		if (cause != null) {
			val event = PlayerTeleportEvent(this, from, to, cause)
			server.pluginManager.callEvent(event)
			if (event.isCancelled) return false
			to = event.to
		}

		//TODO Remove it! A hack to solve the client-side teleporting bug! (inside into the block)
		if (super.teleport(if (to.getY() == to.floorY.toDouble()) to.add(0.0, 0.00001, 0.0) else to, null)) { // null to prevent fire of duplicate EntityTeleportEvent
			removeAllWindows()
			teleportPosition = Vector3(x, y, z)
			forceMovement = teleportPosition
			sendPosition(this, yaw, pitch, MovePlayerPacket.MODE_TELEPORT)
			checkTeleportPosition()
			resetFallDistance()
			nextChunkOrderRun = 0
			newPosition = null

			//DummyBossBar
			getDummyBossBars().values.forEach(Consumer { obj: DummyBossBar? -> obj!!.reshow() })
			//Weather
			level.sendWeather(this)
			//Update time
			level.sendTime(this)
			return true
		}
		return false
	}

	protected fun forceSendEmptyChunks() {
		val chunkPositionX = this.floorX shr 4
		val chunkPositionZ = this.floorZ shr 4
		for (x in -chunkRadius until chunkRadius) {
			for (z in -chunkRadius until chunkRadius) {
				val chunk = LevelChunkPacket()
				chunk.chunkX = chunkPositionX + x
				chunk.chunkZ = chunkPositionZ + z
				chunk.data = ByteArray(0)
				this.dataPacket(chunk)
			}
		}
	}

	@JvmOverloads
	fun teleportImmediate(location: Location, cause: TeleportCause? = TeleportCause.PLUGIN) {
		val from = location
		if (super.teleport(location, cause)) {
			for (window in ArrayList(windows.keys)) {
				if (window === inventory) {
					continue
				}
				removeWindow(window)
			}
			if (from.getLevel().id != location.getLevel().id) { //Different level, update compass position
				val pk = SetSpawnPositionPacket()
				pk.spawnType = SetSpawnPositionPacket.TYPE_WORLD_SPAWN
				val spawn = location.getLevel().spawnLocation
				pk.x = spawn.floorX
				pk.y = spawn.floorY
				pk.z = spawn.floorZ
				dataPacket(pk)
			}
			forceMovement = Vector3(x, y, z)
			sendPosition(this, yaw, pitch, MovePlayerPacket.MODE_RESET)
			resetFallDistance()
			orderChunks()
			nextChunkOrderRun = 0
			newPosition = null

			//Weather
			level.sendWeather(this)
			//Update time
			level.sendTime(this)
		}
	}
	/**
	 * Shows a new FormWindow to the player
	 * You can find out FormWindow result by listening to PlayerFormRespondedEvent
	 *
	 * @param window to show
	 * @param id form id
	 * @return form id to use in [PlayerFormRespondedEvent]
	 */
	/**
	 * Shows a new FormWindow to the player
	 * You can find out FormWindow result by listening to PlayerFormRespondedEvent
	 *
	 * @param window to show
	 * @return form id to use in [PlayerFormRespondedEvent]
	 */
	@JvmOverloads
	fun showFormWindow(window: FormWindow, id: Int = formWindowCount++): Int {
		val packet = ModalFormRequestPacket()
		packet.formId = id
		packet.data = window.jsonData
		formWindows[packet.formId] = window
		this.dataPacket(packet)
		return id
	}

	/**
	 * Shows a new setting page in game settings
	 * You can find out settings result by listening to PlayerFormRespondedEvent
	 *
	 * @param window to show on settings page
	 * @return form id to use in [PlayerFormRespondedEvent]
	 */
	fun addServerSettings(window: FormWindow): Int {
		val id = formWindowCount++
		serverSettings[id] = window
		return id
	}

	/**
	 * Creates and sends a BossBar to the player
	 *
	 * @param text   The BossBar message
	 * @param length The BossBar percentage
	 * @return bossBarId  The BossBar ID, you should store it if you want to remove or update the BossBar later
	 */
	@Deprecated("")
	fun createBossBar(text: String?, length: Int): Long {
		val bossBar = DummyBossBar.Builder(this).text(text).length(length.toFloat()).build()
		return this.createBossBar(bossBar)
	}

	/**
	 * Creates and sends a BossBar to the player
	 *
	 * @param dummyBossBar DummyBossBar Object (Instantiate it by the Class Builder)
	 * @return bossBarId  The BossBar ID, you should store it if you want to remove or update the BossBar later
	 * @see DummyBossBar.Builder
	 */
	fun createBossBar(dummyBossBar: DummyBossBar): Long {
		dummyBossBars[dummyBossBar.bossBarId] = dummyBossBar
		dummyBossBar.create()
		return dummyBossBar.bossBarId
	}

	/**
	 * Get a DummyBossBar object
	 *
	 * @param bossBarId The BossBar ID
	 * @return DummyBossBar object
	 * @see DummyBossBar.setText
	 * @see DummyBossBar.setLength
	 * @see DummyBossBar.setColor
	 */
	fun getDummyBossBar(bossBarId: Long): DummyBossBar? {
		return dummyBossBars.getOrDefault(bossBarId, null)
	}

	/**
	 * Get all DummyBossBar objects
	 *
	 * @return DummyBossBars Map
	 */
	fun getDummyBossBars(): Map<Long, DummyBossBar?> {
		return dummyBossBars
	}

	/**
	 * Updates a BossBar
	 *
	 * @param text      The new BossBar message
	 * @param length    The new BossBar length
	 * @param bossBarId The BossBar ID
	 */
	@Deprecated("")
	fun updateBossBar(text: String?, length: Int, bossBarId: Long) {
		if (dummyBossBars.containsKey(bossBarId)) {
			val bossBar = dummyBossBars[bossBarId]
			bossBar!!.text = text
			bossBar.length = length.toFloat()
		}
	}

	/**
	 * Removes a BossBar
	 *
	 * @param bossBarId The BossBar ID
	 */
	fun removeBossBar(bossBarId: Long) {
		if (dummyBossBars.containsKey(bossBarId)) {
			dummyBossBars[bossBarId]!!.destroy()
			dummyBossBars.remove(bossBarId)
		}
	}

	fun getWindowId(inventory: Inventory?): Int {
		return if (windows.containsKey(inventory)) {
			windows[inventory]!!
		} else -1
	}

	fun getWindowById(id: Int): Inventory? {
		return windowIndex[id]
	}

	@JvmOverloads
	fun addWindow(inventory: Inventory?, forceId: Int? = null, isPermanent: Boolean = false): Int {
		if (windows.containsKey(inventory)) {
			return windows[inventory]!!
		}
		val cnt: Int
		if (forceId == null) {
			cnt = Math.max(4, ++windowCnt % 99)
			windowCnt = cnt
		} else {
			cnt = forceId
		}
		windows.forcePut(inventory, cnt)
		if (isPermanent) {
			permanentWindows.add(cnt)
		}
		return if (inventory!!.open(this)) {
			cnt
		} else {
			removeWindow(inventory)
			-1
		}
	}

	val topWindow: Optional<Inventory>
		get() {
			for ((key, value) in windows) {
				if (!permanentWindows.contains(value)) {
					return Optional.of(key)!!
				}
			}
			return Optional.empty()
		}

	fun removeWindow(inventory: Inventory?) {
		inventory!!.close(this)
		windows.remove(inventory)
	}

	fun sendAllInventories() {
		for (inv in windows.keys) {
			inv!!.sendContents(this)
			if (inv is PlayerInventory) {
				inv.sendArmorContents(this)
			}
		}
	}

	protected fun addDefaultWindows() {
		addWindow(getInventory(), ContainerIds.INVENTORY, true)
		uIInventory = PlayerUIInventory(this)
		addWindow(uIInventory, ContainerIds.UI, true)
		addWindow(offhandInventory, ContainerIds.OFFHAND, true)
		craftingGrid = uIInventory!!.craftingGrid
		addWindow(craftingGrid, ContainerIds.NONE)

		//TODO: more windows
	}

	val cursorInventory: PlayerCursorInventory
		get() = uIInventory!!.cursorInventory

	fun getCraftingGrid(): CraftingGrid? {
		return craftingGrid
	}

	fun setCraftingGrid(grid: CraftingGrid?) {
		craftingGrid = grid
		addWindow(grid, ContainerIds.NONE)
	}

	fun resetCraftingGridType() {
		if (craftingGrid != null) {
			var drops = inventory.addItem(*craftingGrid!!.contents.values.toTypedArray())
			if (drops.size > 0) {
				for (drop in drops) {
					dropItem(drop)
				}
			}
			drops = inventory.addItem(cursorInventory.getItem(0))
			if (drops.size > 0) {
				for (drop in drops) {
					dropItem(drop)
				}
			}
			uIInventory!!.clearAll()
			if (craftingGrid is BigCraftingGrid) {
				craftingGrid = uIInventory!!.craftingGrid
				addWindow(craftingGrid, ContainerIds.NONE)
				//
//                ContainerClosePacket pk = new ContainerClosePacket(); //be sure, big crafting is really closed
//                pk.windowId = ContainerIds.NONE;
//                this.dataPacket(pk);
			}
			craftingType = CRAFTING_SMALL
		}
	}

	@JvmOverloads
	fun removeAllWindows(permanent: Boolean = false) {
		for ((key, value) in ArrayList<Map.Entry<Int, Inventory?>>(windowIndex.entries)) {
			if (!permanent && permanentWindows.contains(key)) {
				continue
			}
			removeWindow(value)
		}
	}

	override fun setMetadata(metadataKey: String, newMetadataValue: MetadataValue) {
		server.playerMetadata.setMetadata(this, metadataKey, newMetadataValue)
	}

	override fun getMetadata(metadataKey: String): List<MetadataValue> {
		return server.playerMetadata.getMetadata(this, metadataKey)
	}

	override fun hasMetadata(metadataKey: String): Boolean {
		return server.playerMetadata.hasMetadata(this, metadataKey)
	}

	override fun removeMetadata(metadataKey: String, owningPlugin: Plugin) {
		server.playerMetadata.removeMetadata(this, metadataKey, owningPlugin)
	}

	override fun onChunkChanged(chunk: FullChunk) {
		usedChunks.remove(Level.chunkHash(chunk.x, chunk.z))
	}

	override fun onChunkLoaded(chunk: FullChunk) {}
	override fun onChunkPopulated(chunk: FullChunk) {}
	override fun onChunkUnloaded(chunk: FullChunk) {}
	override fun onBlockChanged(block: Vector3) {}
	override fun getLoaderId(): Int {
		return loaderId
	}

	override fun isLoaderActive(): Boolean {
		return isConnected
	}

	var isFoodEnabled = true
		get() = !(isCreative || isSpectator) && field

	//todo a lot on dimension
	private fun setDimension(dimension: Int) {
		val pk = ChangeDimensionPacket()
		pk.dimension = dimension
		pk.x = x.toFloat()
		pk.y = y.toFloat()
		pk.z = z.toFloat()
		this.directDataPacket(pk)
	}

	public override fun switchLevel(level: Level): Boolean {
		val oldLevel = this.level
		if (super.switchLevel(level)) {
			val spawnPosition = SetSpawnPositionPacket()
			spawnPosition.spawnType = SetSpawnPositionPacket.TYPE_WORLD_SPAWN
			val spawn = level.spawnLocation
			spawnPosition.x = spawn.floorX
			spawnPosition.y = spawn.floorY
			spawnPosition.z = spawn.floorZ
			this.dataPacket(spawnPosition)

			// Remove old chunks
			for (index in ArrayList(usedChunks.keys)) {
				val chunkX = Level.getHashX(index)
				val chunkZ = Level.getHashZ(index)
				unloadChunk(chunkX, chunkZ, oldLevel)
			}
			usedChunks.clear()
			val setTime = SetTimePacket()
			setTime.time = level.time
			this.dataPacket(setTime)
			val gameRulesChanged = GameRulesChangedPacket()
			gameRulesChanged.gameRules = level.getGameRules()
			this.dataPacket(gameRulesChanged)
			return true
		}
		return false
	}

	fun setCheckMovement(checkMovement: Boolean) {
		this.checkMovement = checkMovement
	}

	override fun setSprinting(value: Boolean) {
		if (isSprinting != value) {
			super.setSprinting(value)
			this.setMovementSpeed(if (value) getMovementSpeed() * 1.3f else getMovementSpeed() / 1.3f)
		}
	}

	fun transfer(address: InetSocketAddress) {
		val hostName = address.address.hostAddress
		val port = address.port
		val pk = TransferPacket()
		pk.address = hostName
		pk.port = port
		this.dataPacket(pk)
		val message = "Transferred to $hostName:$port"
		this.close("", message, false)
	}

	fun pickupEntity(entity: Entity, near: Boolean): Boolean {
		if (!spawned || !this.isAlive || !this.isOnline || gamemode == SPECTATOR || entity.isClosed) {
			return false
		}
		if (near) {
			if (entity is EntityArrow && entity.hadCollision) {
				val item = ItemArrow()
				if (isSurvival && !inventory.canAddItem(item)) {
					return false
				}
				val ev = InventoryPickupArrowEvent(inventory, entity)
				val pickupMode = entity.pickupMode
				if (pickupMode == EntityArrow.PICKUP_NONE || pickupMode == EntityArrow.PICKUP_CREATIVE && !isCreative) {
					ev.setCancelled()
				}
				server.pluginManager.callEvent(ev)
				if (ev.isCancelled) {
					return false
				}
				val pk = TakeItemEntityPacket()
				pk.entityId = getId()
				pk.target = entity.getId()
				broadcastPacket(entity.getViewers().values, pk)
				this.dataPacket(pk)
				if (!isCreative) {
					inventory.addItem(item.clone())
				}
				entity.close()
				return true
			} else if (entity is EntityThrownTrident && entity.hadCollision) {
				val item = entity.item
				if (isSurvival && !inventory.canAddItem(item)) {
					return false
				}
				val pk = TakeItemEntityPacket()
				pk.entityId = getId()
				pk.target = entity.getId()
				broadcastPacket(entity.getViewers().values, pk)
				this.dataPacket(pk)
				if (!isCreative) {
					inventory.addItem(item.clone())
				}
				entity.close()
				return true
			} else if (entity is EntityItem) {
				if (entity.pickupDelay <= 0) {
					val item = entity.item
					if (item != null) {
						if (isSurvival && !inventory.canAddItem(item)) {
							return false
						}
						var ev: InventoryPickupItemEvent
						server.pluginManager.callEvent(InventoryPickupItemEvent(inventory, entity).also { ev = it })
						if (ev.isCancelled) {
							return false
						}
						when (item.id) {
							Item.WOOD, Item.WOOD2 -> awardAchievement("mineWood")
							Item.DIAMOND -> awardAchievement("diamond")
						}
						val pk = TakeItemEntityPacket()
						pk.entityId = getId()
						pk.target = entity.getId()
						broadcastPacket(entity.getViewers().values, pk)
						this.dataPacket(pk)
						entity.close()
						inventory.addItem(item.clone())
						return true
					}
				}
			}
		}
		val tick = getServer().tick
		if (pickedXPOrb < tick && entity is EntityXPOrb && boundingBox.isVectorInside(entity)) {
			val xpOrb = entity
			if (xpOrb.pickupDelay <= 0) {
				val exp = xpOrb.exp
				entity.kill()
				level.addSound(this, Sound.RANDOM_ORB)
				pickedXPOrb = tick

				//Mending
				val itemsWithMending = ArrayList<Int>()
				for (i in 0..3) {
					if (inventory.getArmorItem(i).getEnchantment(Enchantment.ID_MENDING.toShort()) != null) {
						itemsWithMending.add(inventory.size + i)
					}
				}
				if (inventory.itemInHand.getEnchantment(Enchantment.ID_MENDING.toShort()) != null) {
					itemsWithMending.add(inventory.heldItemIndex)
				}
				if (itemsWithMending.size > 0) {
					val rand = Random()
					val itemToRepair = itemsWithMending[rand.nextInt(itemsWithMending.size)]
					val toRepair = inventory.getItem(itemToRepair)
					if (toRepair is ItemTool || toRepair is ItemArmor) {
						if (toRepair.damage > 0) {
							var dmg = toRepair.damage - 2
							if (dmg < 0) dmg = 0
							toRepair.damage = dmg
							inventory.setItem(itemToRepair, toRepair)
							return true
						}
					}
				}
				addExperience(exp)
				return true
			}
		}
		return false
	}

	override fun hashCode(): Int {
		if (hash == 0 || hash == 485) {
			hash = 485 + if (uniqueId != null) uniqueId.hashCode() else 0
		}
		return hash
	}

	override fun equals(obj: Any?): Boolean {
		if (obj !is Player) {
			return false
		}
		val other = obj
		return this.uniqueId == other.uniqueId && getId() == other.getId()
	}

	/**
	 * Notifies an ACK response from the client
	 *
	 * @param identification packet identity
	 */
	fun notifyACK(identification: Int) {
		needACK[identification] = java.lang.Boolean.TRUE
	}

	fun isBreakingBlock(): Boolean {
		return breakingBlock != null
	}

	/**
	 * Show a window of a XBOX account's profile
	 * @param xuid XUID
	 */
	fun showXboxProfile(xuid: String?) {
		val pk = ShowProfilePacket()
		pk.xuid = xuid
		this.dataPacket(pk)
	}

	fun startFishing(fishingRod: Item?) {
		val nbt = CompoundTag()
				.putList(ListTag<DoubleTag>("Pos")
						.add(DoubleTag("", x))
						.add(DoubleTag("", y + this.eyeHeight))
						.add(DoubleTag("", z)))
				.putList(ListTag<DoubleTag>("Motion")
						.add(DoubleTag("", -Math.sin(yaw / 180 + Math.PI) * Math.cos(pitch / 180 * Math.PI)))
						.add(DoubleTag("", -Math.sin(pitch / 180 * Math.PI)))
						.add(DoubleTag("", Math.cos(yaw / 180 * Math.PI) * Math.cos(pitch / 180 * Math.PI))))
				.putList(ListTag<FloatTag>("Rotation")
						.add(FloatTag("", yaw.toFloat()))
						.add(FloatTag("", pitch.toFloat())))
		val f = 1.0
		val fishingHook = EntityFishingHook(chunk, nbt, this)
		fishingHook.motion = Vector3(-Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * f * f, -Math.sin(Math.toRadians(pitch)) * f * f,
				Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * f * f)
		val ev = ProjectileLaunchEvent(fishingHook)
		getServer().pluginManager.callEvent(ev)
		if (ev.isCancelled) {
			fishingHook.kill()
		} else {
			fishingHook.spawnToAll()
			fishing = fishingHook
			fishingHook.rod = fishingRod
		}
	}

	fun stopFishing(click: Boolean) {
		if (click) {
			fishing!!.reelLine()
		} else if (fishing != null) {
			fishing!!.kill()
			fishing!!.close()
		}
		fishing = null
	}

	override fun toString(): String {
		return "Player(name='" + name +
				"', location=" + super.toString() +
				')'
	}

	companion object {
		const val SURVIVAL = 0
		const val CREATIVE = 1
		const val ADVENTURE = 2
		const val SPECTATOR = 3
		const val VIEW = SPECTATOR
		const val SURVIVAL_SLOTS = 36
		const val CREATIVE_SLOTS = 112
		const val CRAFTING_SMALL = 0
		const val CRAFTING_BIG = 1
		const val CRAFTING_ANVIL = 2
		const val CRAFTING_ENCHANT = 3
		const val CRAFTING_BEACON = 4
		const val DEFAULT_SPEED = 0.1f
		const val MAXIMUM_SPEED = 0.5f
		const val PERMISSION_CUSTOM = 3
		const val PERMISSION_OPERATOR = 2
		const val PERMISSION_MEMBER = 1
		const val PERMISSION_VISITOR = 0
		const val ANVIL_WINDOW_ID = 2
		const val ENCHANT_WINDOW_ID = 3
		const val BEACON_WINDOW_ID = 4

		/**
		 * Returns a client-friendly gamemode of the specified real gamemode
		 * This function takes care of handling gamemodes known to MCPE (as of 1.1.0.3, that includes Survival, Creative and Adventure)
		 *
		 *
		 * TODO: remove this when Spectator Mode gets added properly to MCPE
		 */
		private fun getClientFriendlyGamemode(gamemode: Int): Int {
			var gamemode = gamemode
			gamemode = gamemode and 0x03
			return if (gamemode == SPECTATOR) {
				CREATIVE
			} else gamemode
		}

		fun calculateRequireExperience(level: Int): Int {
			return if (level >= 30) {
				112 + (level - 30) * 9
			} else if (level >= 15) {
				37 + (level - 15) * 5
			} else {
				7 + level * 2
			}
		}

		@JvmStatic
		fun getChunkCacheFromData(chunkX: Int, chunkZ: Int, subChunkCount: Int, payload: ByteArray?): BatchPacket {
			val pk = LevelChunkPacket()
			pk.chunkX = chunkX
			pk.chunkZ = chunkZ
			pk.subChunkCount = subChunkCount
			pk.data = payload
			pk.encode()
			val batch = BatchPacket()
			val batchPayload = arrayOfNulls<ByteArray>(2)
			val buf = pk.buffer
			batchPayload[0] = Binary.writeUnsignedVarInt(buf.size.toLong())
			batchPayload[1] = buf
			val data = Binary.appendBytes(batchPayload)
			try {
				batch.payload = Zlib.deflate(data, Server.instance!!.networkCompressionLevel)
			} catch (e: Exception) {
				throw RuntimeException(e)
			}
			return batch
		}
	}

	init {
		perm = PermissibleBase(this)
		server = Server.instance
		lastBreak = -1
		address = ip
		this.port = port
		this.clientID = clientID
		loaderId = Level.generateChunkLoaderId(this)
		chunksPerTick = server.config
		spawnThreshold = server.config
		spawnPosition = null
		gamemode = server.gamemode
		setLevel(server.defaultLevel)
		viewDistance = server.viewDistance
		chunkRadius = viewDistance
		//this.newPosition = new Vector3(0, 0, 0);
		boundingBox = SimpleAxisAlignedBB(0, 0, 0, 0, 0, 0)
		lastSkinChange = -1
		uuid = null
		rawUUID = null
		creationTime = System.currentTimeMillis()
	}
}