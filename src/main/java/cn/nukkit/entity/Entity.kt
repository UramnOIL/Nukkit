package cn.nukkit.entity

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.Server.Companion.broadcastPacket
import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.block.BlockFire
import cn.nukkit.block.BlockID
import cn.nukkit.block.BlockNetherPortal.Companion.spawnPortal
import cn.nukkit.block.BlockWater
import cn.nukkit.entity.data.*
import cn.nukkit.event.Event
import cn.nukkit.event.entity.*
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.event.entity.EntityDamageEvent.DamageModifier
import cn.nukkit.event.entity.EntityPortalEnterEvent.PortalType
import cn.nukkit.event.player.PlayerInteractEvent
import cn.nukkit.event.player.PlayerTeleportEvent.TeleportCause
import cn.nukkit.item.Item
import cn.nukkit.level.EnumLevel
import cn.nukkit.level.Level
import cn.nukkit.level.Location
import cn.nukkit.level.Position
import cn.nukkit.level.format.FullChunk
import cn.nukkit.math.*
import cn.nukkit.metadata.MetadataValue
import cn.nukkit.metadata.Metadatable
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.DoubleTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.network.protocol.*
import cn.nukkit.network.protocol.types.EntityLink
import cn.nukkit.plugin.Plugin
import cn.nukkit.potion.Effect
import cn.nukkit.scheduler.Task
import cn.nukkit.utils.ChunkException
import cn.nukkit.utils.MainLogger
import co.aikar.timings.Timing
import co.aikar.timings.Timings
import co.aikar.timings.TimingsHistory
import com.google.common.collect.Iterables
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author MagicDroidX
 */
abstract class Entity(chunk: FullChunk?, nbt: CompoundTag?) : Location(), Metadatable {
	abstract val networkId: Int
	protected val hasSpawned: MutableMap<Int, Player> = HashMap()
	protected val effects: MutableMap<Int, Effect?> = ConcurrentHashMap()
	var id: Long = 0
		protected set
	val dataProperties = EntityMetadata()
			.putLong(DATA_FLAGS, 0)
			.putByte(DATA_COLOR, 0)
			.putShort(DATA_AIR, 400)
			.putShort(DATA_MAX_AIR, 400)
			.putString(DATA_NAMETAG, "")
			.putLong(DATA_LEAD_HOLDER_EID, -1)
			.putFloat(DATA_SCALE, 1f)
	val passengers: MutableList<Entity> = ArrayList()
	var riding: Entity? = null
	var chunk: FullChunk? = null
	var lastDamageCause: EntityDamageEvent? = null
	var blocksAround: MutableList<Block>? = ArrayList()
	var collisionBlocks: MutableList<Block>? = ArrayList()
	var lastX = 0.0
	var lastY = 0.0
	var lastZ = 0.0
	var firstMove = true
	var motionX = 0.0
	var motionY = 0.0
	var motionZ = 0.0
	var temporalVector: Vector3? = null
	var lastMotionX = 0.0
	var lastMotionY = 0.0
	var lastMotionZ = 0.0
	var lastYaw = 0.0
	var lastPitch = 0.0
	var pitchDelta = 0.0
	var yawDelta = 0.0
	var entityCollisionReduction = 0.0 // Higher than 0.9 will result a fast collisions
	var boundingBox: AxisAlignedBB? = null
	var isOnGround = false
	var inBlock = false
	var positionChanged = false
	var motionChanged = false
	var deadTicks = 0
	protected var age = 0
	protected var health = 20f
	open var maxHealth = 20
		get() = field + if (hasEffect(Effect.HEALTH_BOOST)) 4 * (getEffect(Effect.HEALTH_BOOST)!!.amplifier + 1) else 0
	protected var absorption = 0f
	protected var ySize = 0f
	var keepMovement = false
	var fallDistance = 0f
	var ticksLived = 0
	var lastUpdate = 0
	var maxFireTicks = 0
	var fireTicks = 0
	var inPortalTicks = 0
	var scale = 1f
	var namedTag: CompoundTag? = null
	protected var isStatic = false
	var isCollided = false
	var isCollidedHorizontally = false
	var isCollidedVertically = false
	var noDamageTicks = 0
	var justCreated = false
	var fireProof = false
	var invulnerable = false
	open var server: Server? = null
		protected set
	var highestPosition = 0.0
	var isClosed = false
	protected var timing: Timing? = null
	protected var isPlayer = false

	@Volatile
	private var initialized = false
	open val height: Float
		get() = 0

	open val eyeHeight: Float
		get() = height / 2 + 0.1f

	open val width: Float
		get() = 0

	open val length: Float
		get() = 0

	protected open val stepHeight: Double
		protected get() = 0

	open fun canCollide(): Boolean {
		return true
	}

	protected open val gravity: Float
		protected get() = 0

	protected open val drag: Float
		protected get() = 0

	protected open val baseOffset: Float
		protected get() = 0

	protected open fun initEntity() {
		if (namedTag!!.contains("ActiveEffects")) {
			val effects = namedTag!!.getList("ActiveEffects", CompoundTag::class.java)
			for (e in effects.all) {
				val effect = Effect.getEffect(e.getByte("Id")) ?: continue
				effect.setAmplifier(e.getByte("Amplifier")).setDuration(e.getInt("Duration")).isVisible = e.getBoolean("showParticles")
				addEffect(effect)
			}
		}
		if (namedTag!!.contains("CustomName")) {
			setNameTag(namedTag!!.getString("CustomName"))
			if (namedTag!!.contains("CustomNameVisible")) {
				this.nameTagVisible = namedTag!!.getBoolean("CustomNameVisible")
			}
			if (namedTag!!.contains("CustomNameAlwaysVisible")) {
				this.nameTagAlwaysVisible = namedTag!!.getBoolean("CustomNameAlwaysVisible")
			}
		}
		this.setDataFlag(DATA_FLAGS, DATA_FLAG_HAS_COLLISION, true)
		dataProperties!!.putFloat(DATA_BOUNDING_BOX_HEIGHT, height)
		dataProperties.putFloat(DATA_BOUNDING_BOX_WIDTH, width)
		dataProperties.putInt(DATA_HEALTH, getHealth().toInt())
		scheduleUpdate()
	}

	protected fun init(chunk: FullChunk?, nbt: CompoundTag?) {
		if (chunk == null || chunk.provider == null) {
			throw ChunkException("Invalid garbage Chunk given to Entity")
		}
		if (initialized) {
			// We've already initialized this entity
			return
		}
		initialized = true
		timing = Timings.getEntityTiming(this)
		isPlayer = this is Player
		temporalVector = Vector3()
		id = entityCount++
		justCreated = true
		namedTag = nbt
		this.chunk = chunk
		setLevel(chunk.provider.level)
		server = chunk.provider.level.server
		boundingBox = SimpleAxisAlignedBB(0, 0, 0, 0, 0, 0)
		val posList = namedTag!!.getList("Pos", DoubleTag::class.java)
		val rotationList = namedTag!!.getList("Rotation", FloatTag::class.java)
		val motionList = namedTag!!.getList("Motion", DoubleTag::class.java)
		setPositionAndRotation(
				temporalVector!!.setComponents(
						posList[0].data,
						posList[1].data,
						posList[2].data
				),
				rotationList[0].data.toDouble(),
				rotationList[1].data
						.toDouble())
		setMotion(temporalVector!!.setComponents(
				motionList[0].data,
				motionList[1].data,
				motionList[2].data
		))
		if (!namedTag!!.contains("FallDistance")) {
			namedTag!!.putFloat("FallDistance", 0f)
		}
		fallDistance = namedTag!!.getFloat("FallDistance")
		highestPosition = y + namedTag!!.getFloat("FallDistance")
		if (!namedTag!!.contains("Fire") || namedTag!!.getShort("Fire") > 32767) {
			namedTag!!.putShort("Fire", 0)
		}
		fireTicks = namedTag!!.getShort("Fire")
		if (!namedTag!!.contains("Air")) {
			namedTag!!.putShort("Air", 300)
		}
		this.setDataProperty(ShortEntityData(DATA_AIR, namedTag!!.getShort("Air")), false)
		if (!namedTag!!.contains("OnGround")) {
			namedTag!!.putBoolean("OnGround", false)
		}
		isOnGround = namedTag!!.getBoolean("OnGround")
		if (!namedTag!!.contains("Invulnerable")) {
			namedTag!!.putBoolean("Invulnerable", false)
		}
		invulnerable = namedTag!!.getBoolean("Invulnerable")
		if (!namedTag!!.contains("Scale")) {
			namedTag!!.putFloat("Scale", 1f)
		}
		scale = namedTag!!.getFloat("Scale")
		this.setDataProperty(FloatEntityData(DATA_SCALE, scale), false)
		this.chunk!!.addEntity(this)
		level.addEntity(this)
		initEntity()
		lastUpdate = server.tick
		server.pluginManager.callEvent(EntitySpawnEvent(this))
		scheduleUpdate()
	}

	open fun hasCustomName(): Boolean {
		return !nameTag!!.isEmpty()
	}

	val nameTag: String?
		get() = getDataPropertyString(DATA_NAMETAG)

	var isNameTagVisible: Boolean
		get() = getDataFlag(DATA_FLAGS, DATA_FLAG_CAN_SHOW_NAMETAG)
		set(value) {
			this.setDataFlag(DATA_FLAGS, DATA_FLAG_CAN_SHOW_NAMETAG, value)
		}

	var isNameTagAlwaysVisible: Boolean
		get() = getDataPropertyByte(DATA_ALWAYS_SHOW_NAMETAG) == 1
		set(value) {
			this.setDataProperty(ByteEntityData(DATA_ALWAYS_SHOW_NAMETAG, if (value) 1 else 0))
		}

	fun setNameTag(name: String) {
		this.setDataProperty(StringEntityData(DATA_NAMETAG, name))
	}

	fun setNameTagVisible() {
		this.nameTagVisible = true
	}

	fun setNameTagAlwaysVisible() {
		this.nameTagAlwaysVisible = true
	}

	fun setScoreTag(score: String) {
		this.setDataProperty(StringEntityData(DATA_SCORE_TAG, score))
	}

	val scoreTag: String?
		get() = getDataPropertyString(DATA_SCORE_TAG)

	var isSneaking: Boolean
		get() = getDataFlag(DATA_FLAGS, DATA_FLAG_SNEAKING)
		set(value) {
			this.setDataFlag(DATA_FLAGS, DATA_FLAG_SNEAKING, value)
		}

	fun setSneaking() {
		this.sneaking = true
	}

	var isSwimming: Boolean
		get() = getDataFlag(DATA_FLAGS, DATA_FLAG_SWIMMING)
		set(value) {
			this.setDataFlag(DATA_FLAGS, DATA_FLAG_SWIMMING, value)
		}

	fun setSwimming() {
		this.swimming = true
	}

	open var isSprinting: Boolean
		get() = getDataFlag(DATA_FLAGS, DATA_FLAG_SPRINTING)
		set(value) {
			this.setDataFlag(DATA_FLAGS, DATA_FLAG_SPRINTING, value)
		}

	fun setSprinting() {
		this.sprinting = true
	}

	var isGliding: Boolean
		get() = getDataFlag(DATA_FLAGS, DATA_FLAG_GLIDING)
		set(value) {
			this.setDataFlag(DATA_FLAGS, DATA_FLAG_GLIDING, value)
		}

	fun setGliding() {
		this.gliding = true
	}

	var isImmobile: Boolean
		get() = getDataFlag(DATA_FLAGS, DATA_FLAG_IMMOBILE)
		set(value) {
			this.setDataFlag(DATA_FLAGS, DATA_FLAG_IMMOBILE, value)
		}

	fun setImmobile() {
		this.immobile = true
	}

	fun canClimb(): Boolean {
		return getDataFlag(DATA_FLAGS, DATA_FLAG_CAN_CLIMB)
	}

	fun setCanClimb() {
		this.setCanClimb(true)
	}

	fun setCanClimb(value: Boolean) {
		this.setDataFlag(DATA_FLAGS, DATA_FLAG_CAN_CLIMB, value)
	}

	fun canClimbWalls(): Boolean {
		return getDataFlag(DATA_FLAGS, DATA_FLAG_WALLCLIMBING)
	}

	fun setCanClimbWalls() {
		this.setCanClimbWalls(true)
	}

	fun setCanClimbWalls(value: Boolean) {
		this.setDataFlag(DATA_FLAGS, DATA_FLAG_WALLCLIMBING, value)
	}

	fun setScale(scale: Float) {
		this.scale = scale
		this.setDataProperty(FloatEntityData(DATA_SCALE, this.scale))
		recalculateBoundingBox()
	}

	fun getScale(): Float {
		return scale
	}

	fun getPassengers(): List<Entity> {
		return passengers
	}

	val passenger: Entity?
		get() = Iterables.getFirst(passengers, null)

	fun isPassenger(entity: Entity): Boolean {
		return passengers.contains(entity)
	}

	open fun isControlling(entity: Entity): Boolean {
		return passengers.indexOf(entity) == 0
	}

	fun hasControllingPassenger(): Boolean {
		return !passengers.isEmpty() && isControlling(passengers[0])
	}

	fun getEffects(): Map<Int, Effect?> {
		return effects
	}

	fun removeAllEffects() {
		for (effect in effects.values) {
			removeEffect(effect!!.id)
		}
	}

	fun removeEffect(effectId: Int) {
		if (effects.containsKey(effectId)) {
			val effect = effects[effectId]
			effects.remove(effectId)
			effect!!.remove(this)
			recalculateEffectColor()
		}
	}

	fun getEffect(effectId: Int): Effect? {
		return effects.getOrDefault(effectId, null)
	}

	fun hasEffect(effectId: Int): Boolean {
		return effects.containsKey(effectId)
	}

	fun addEffect(effect: Effect?) {
		if (effect == null) {
			return  //here add null means add nothing
		}
		effect.add(this)
		effects[effect.id] = effect
		recalculateEffectColor()
		if (effect.id == Effect.HEALTH_BOOST) {
			setHealth(getHealth() + 4 * (effect.amplifier + 1))
		}
	}

	@JvmOverloads
	fun recalculateBoundingBox(send: Boolean = true) {
		val height = height * scale
		val radius = width * scale / 2.0
		boundingBox!!.setBounds(x - radius, y, z - radius, x + radius, y + height, z + radius)
		val bbH = FloatEntityData(DATA_BOUNDING_BOX_HEIGHT, this.height)
		val bbW = FloatEntityData(DATA_BOUNDING_BOX_WIDTH, width)
		dataProperties!!.put(bbH)
		dataProperties.put(bbW)
		if (send) {
			sendData(hasSpawned.values.toTypedArray(), EntityMetadata().put(bbH).put(bbW))
		}
	}

	protected fun recalculateEffectColor() {
		val color = IntArray(3)
		var count = 0
		var ambient = true
		for (effect in effects.values) {
			if (effect!!.isVisible) {
				val c = effect.color
				color[0] += c[0] * (effect.amplifier + 1)
				color[1] += c[1] * (effect.amplifier + 1)
				color[2] += c[2] * (effect.amplifier + 1)
				count += effect.amplifier + 1
				if (!effect.isAmbient) {
					ambient = false
				}
			}
		}
		if (count > 0) {
			val r = color[0] / count and 0xff
			val g = color[1] / count and 0xff
			val b = color[2] / count and 0xff
			this.setDataProperty(IntEntityData(DATA_POTION_COLOR, (r shl 16) + (g shl 8) + b))
			this.setDataProperty(ByteEntityData(DATA_POTION_AMBIENT, if (ambient) 1 else 0))
		} else {
			this.setDataProperty(IntEntityData(DATA_POTION_COLOR, 0))
			this.setDataProperty(ByteEntityData(DATA_POTION_AMBIENT, 0))
		}
	}

	open fun saveNBT() {
		if (this !is Player) {
			namedTag!!.putString("id", saveId)
			if (nameTag != "") {
				namedTag!!.putString("CustomName", nameTag)
				namedTag!!.putBoolean("CustomNameVisible", isNameTagVisible)
				namedTag!!.putBoolean("CustomNameAlwaysVisible", isNameTagAlwaysVisible)
			} else {
				namedTag!!.remove("CustomName")
				namedTag!!.remove("CustomNameVisible")
				namedTag!!.remove("CustomNameAlwaysVisible")
			}
		}
		namedTag!!.putList(ListTag<DoubleTag>("Pos")
				.add(DoubleTag("0", x))
				.add(DoubleTag("1", y))
				.add(DoubleTag("2", z))
		)
		namedTag!!.putList(ListTag<DoubleTag>("Motion")
				.add(DoubleTag("0", motionX))
				.add(DoubleTag("1", motionY))
				.add(DoubleTag("2", motionZ))
		)
		namedTag!!.putList(ListTag<FloatTag>("Rotation")
				.add(FloatTag("0", yaw.toFloat()))
				.add(FloatTag("1", pitch.toFloat()))
		)
		namedTag!!.putFloat("FallDistance", fallDistance)
		namedTag!!.putShort("Fire", fireTicks)
		namedTag!!.putShort("Air", getDataPropertyShort(DATA_AIR))
		namedTag!!.putBoolean("OnGround", isOnGround)
		namedTag!!.putBoolean("Invulnerable", invulnerable)
		namedTag!!.putFloat("Scale", scale)
		if (!effects.isEmpty()) {
			val list = ListTag<CompoundTag>("ActiveEffects")
			for (effect in effects.values) {
				list.add(CompoundTag(effect!!.id.toString())
						.putByte("Id", effect.id)
						.putByte("Amplifier", effect.amplifier)
						.putInt("Duration", effect.duration)
						.putBoolean("Ambient", false)
						.putBoolean("ShowParticles", effect.isVisible)
				)
			}
			namedTag!!.putList(list)
		} else {
			namedTag!!.remove("ActiveEffects")
		}
	}

	open val name: String?
		get() = if (hasCustomName()) {
			nameTag
		} else {
			saveId
		}

	val saveId: String
		get() = shortNames.getOrDefault(this.javaClass.simpleName, "")

	open fun spawnTo(player: Player) {
		if (!hasSpawned.containsKey(player.loaderId) && chunk != null && player.usedChunks.containsKey(Level.chunkHash(chunk!!.x, chunk!!.z))) {
			hasSpawned[player.loaderId] = player
			player.dataPacket(createAddEntityPacket())
		}
		if (riding != null) {
			riding!!.spawnTo(player)
			val pkk = SetEntityLinkPacket()
			pkk.vehicleUniqueId = riding!!.id
			pkk.riderUniqueId = id
			pkk.type = 1
			pkk.immediate = 1
			player.dataPacket(pkk)
		}
	}

	protected open fun createAddEntityPacket(): DataPacket {
		val addEntity = AddEntityPacket()
		addEntity.type = networkId
		addEntity.entityUniqueId = id
		addEntity.entityRuntimeId = id
		addEntity.yaw = yaw.toFloat()
		addEntity.headYaw = yaw.toFloat()
		addEntity.pitch = pitch.toFloat()
		addEntity.x = x.toFloat()
		addEntity.y = y.toFloat()
		addEntity.z = z.toFloat()
		addEntity.speedX = motionX.toFloat()
		addEntity.speedY = motionY.toFloat()
		addEntity.speedZ = motionZ.toFloat()
		addEntity.metadata = dataProperties
		addEntity.links = arrayOfNulls(passengers.size)
		for (i in addEntity.links.indices) {
			addEntity.links[i] = EntityLink(id, passengers[i].id, if (i == 0) EntityLink.TYPE_RIDER else SetEntityLinkPacket.TYPE_PASSENGER, false)
		}
		return addEntity
	}

	val viewers: Map<Int, Player>
		get() = hasSpawned

	fun sendPotionEffects(player: Player) {
		for (effect in effects.values) {
			val pk = MobEffectPacket()
			pk.eid = id
			pk.effectId = effect!!.id
			pk.amplifier = effect.amplifier
			pk.particles = effect.isVisible
			pk.duration = effect.duration
			pk.eventId = MobEffectPacket.EVENT_ADD.toInt()
			player.dataPacket(pk)
		}
	}

	@JvmOverloads
	fun sendData(player: Player, data: EntityMetadata? = null) {
		val pk = SetEntityDataPacket()
		pk.eid = id
		pk.metadata = data ?: dataProperties
		player.dataPacket(pk)
	}

	@JvmOverloads
	fun sendData(players: Array<Player>, data: EntityMetadata? = null) {
		val pk = SetEntityDataPacket()
		pk.eid = id
		pk.metadata = data ?: dataProperties
		for (player in players) {
			if (player === this) {
				continue
			}
			player.dataPacket(pk.clone())
		}
		if (this is Player) {
			this.dataPacket(pk)
		}
	}

	open fun despawnFrom(player: Player) {
		if (hasSpawned.containsKey(player.loaderId)) {
			val pk = RemoveEntityPacket()
			pk.eid = id
			player.dataPacket(pk)
			hasSpawned.remove(player.loaderId)
		}
	}

	open fun attack(source: EntityDamageEvent): Boolean {
		if (hasEffect(Effect.FIRE_RESISTANCE)
				&& (source.cause == DamageCause.FIRE || source.cause == DamageCause.FIRE_TICK || source.cause == DamageCause.LAVA)) {
			return false
		}
		server!!.pluginManager.callEvent(source)
		if (source.isCancelled) {
			return false
		}
		if (absorption > 0) {  // Damage Absorption
			setAbsorption(Math.max(0f, getAbsorption() + source.getDamage(DamageModifier.ABSORPTION)))
		}
		lastDamageCause = source
		setHealth(getHealth() - source.finalDamage)
		return true
	}

	fun attack(damage: Float): Boolean {
		return this.attack(EntityDamageEvent(this, DamageCause.CUSTOM, damage))
	}

	fun heal(source: EntityRegainHealthEvent) {
		server!!.pluginManager.callEvent(source)
		if (source.isCancelled) {
			return
		}
		setHealth(getHealth() + source.amount)
	}

	fun heal(amount: Float) {
		this.heal(EntityRegainHealthEvent(this, amount, EntityRegainHealthEvent.CAUSE_REGEN))
	}

	fun getHealth(): Float {
		return health
	}

	val isAlive: Boolean
		get() = health > 0

	open fun setHealth(health: Float) {
		if (this.health == health) {
			return
		}
		if (health < 1) {
			if (isAlive) {
				kill()
			}
		} else if (health <= maxHealth || health < this.health) {
			this.health = health
		} else {
			this.health = maxHealth.toFloat()
		}
		setDataProperty(IntEntityData(DATA_HEALTH, this.health.toInt()))
	}

	open fun canCollideWith(entity: Entity): Boolean {
		return !justCreated && this !== entity
	}

	protected fun checkObstruction(x: Double, y: Double, z: Double): Boolean {
		if (level.getCollisionCubes(this, boundingBox, false).size == 0) {
			return false
		}
		val i = NukkitMath.floorDouble(x)
		val j = NukkitMath.floorDouble(y)
		val k = NukkitMath.floorDouble(z)
		val diffX = x - i
		val diffY = y - j
		val diffZ = z - k
		if (!Block.transparent!![level.getBlockIdAt(i, j, k)]) {
			val flag = Block.transparent!![level.getBlockIdAt(i - 1, j, k)]
			val flag1 = Block.transparent!![level.getBlockIdAt(i + 1, j, k)]
			val flag2 = Block.transparent!![level.getBlockIdAt(i, j - 1, k)]
			val flag3 = Block.transparent!![level.getBlockIdAt(i, j + 1, k)]
			val flag4 = Block.transparent!![level.getBlockIdAt(i, j, k - 1)]
			val flag5 = Block.transparent!![level.getBlockIdAt(i, j, k + 1)]
			var direction = -1
			var limit = 9999.0
			if (flag) {
				limit = diffX
				direction = 0
			}
			if (flag1 && 1 - diffX < limit) {
				limit = 1 - diffX
				direction = 1
			}
			if (flag2 && diffY < limit) {
				limit = diffY
				direction = 2
			}
			if (flag3 && 1 - diffY < limit) {
				limit = 1 - diffY
				direction = 3
			}
			if (flag4 && diffZ < limit) {
				limit = diffZ
				direction = 4
			}
			if (flag5 && 1 - diffZ < limit) {
				direction = 5
			}
			val force = Random().nextDouble() * 0.2 + 0.1
			if (direction == 0) {
				motionX = -force
				return true
			}
			if (direction == 1) {
				motionX = force
				return true
			}
			if (direction == 2) {
				motionY = -force
				return true
			}
			if (direction == 3) {
				motionY = force
				return true
			}
			if (direction == 4) {
				motionZ = -force
				return true
			}
			if (direction == 5) {
				motionZ = force
				return true
			}
		}
		return false
	}

	open fun entityBaseTick(): Boolean {
		return this.entityBaseTick(1)
	}

	open fun entityBaseTick(tickDiff: Int): Boolean {
		Timings.entityBaseTickTimer.startTiming()
		if (!isPlayer) {
			blocksAround = null
			collisionBlocks = null
		}
		justCreated = false
		if (!isAlive) {
			removeAllEffects()
			despawnFromAll()
			if (!isPlayer) {
				close()
			}
			Timings.entityBaseTickTimer.stopTiming()
			return false
		}
		if (riding != null && !riding!!.isAlive && riding is EntityRideable) {
			(riding as EntityRideable).mountEntity(this)
		}
		updatePassengers()
		if (!effects.isEmpty()) {
			for (effect in effects.values) {
				if (effect!!.canTick()) {
					effect.applyEffect(this)
				}
				effect.duration = effect.duration - tickDiff
				if (effect.duration <= 0) {
					removeEffect(effect.id)
				}
			}
		}
		var hasUpdate = false
		checkBlockCollision()
		if (y <= -16 && isAlive) {
			if (this is Player) {
				if (this.gamemode != 1) this.attack(EntityDamageEvent(this, DamageCause.VOID, 10))
			} else {
				this.attack(EntityDamageEvent(this, DamageCause.VOID, 10))
				hasUpdate = true
			}
		}
		if (fireTicks > 0) {
			if (fireProof) {
				fireTicks -= 4 * tickDiff
				if (fireTicks < 0) {
					fireTicks = 0
				}
			} else {
				if (!hasEffect(Effect.FIRE_RESISTANCE) && (fireTicks % 20 == 0 || tickDiff > 20)) {
					this.attack(EntityDamageEvent(this, DamageCause.FIRE_TICK, 1))
				}
				fireTicks -= tickDiff
			}
			if (fireTicks <= 0) {
				extinguish()
			} else if (!fireProof && (this !is Player || !this.isSpectator)) {
				this.setDataFlag(DATA_FLAGS, DATA_FLAG_ONFIRE, true)
				hasUpdate = true
			}
		}
		if (noDamageTicks > 0) {
			noDamageTicks -= tickDiff
			if (noDamageTicks < 0) {
				noDamageTicks = 0
			}
		}
		if (inPortalTicks == 80) {
			val ev = EntityPortalEnterEvent(this, PortalType.NETHER)
			server!!.pluginManager.callEvent(ev)
			if (!ev.isCancelled) {
				val newPos = EnumLevel.moveToNether(this)
				if (newPos != null) {
					for (x in -1..1) {
						for (z in -1..1) {
							val chunkX = (newPos.floorX shr 4) + x
							val chunkZ = (newPos.floorZ shr 4) + z
							val chunk: FullChunk? = newPos.level.getChunk(chunkX, chunkZ, false)
							if (chunk == null || !(chunk.isGenerated || chunk.isPopulated)) {
								newPos.level.generateChunk(chunkX, chunkZ, true)
							}
						}
					}
					this.teleport(newPos.add(1.5, 1.0, 0.5))
					server!!.scheduler.scheduleDelayedTask(object : Task() {
						override fun onRun(currentTick: Int) {
							// dirty hack to make sure chunks are loaded and generated before spawning
							// player
							teleport(newPos.add(1.5, 1.0, 0.5))
							spawnPortal(newPos)
						}
					}, 20)
				}
			}
		}
		age += tickDiff
		ticksLived += tickDiff
		TimingsHistory.activatedEntityTicks++
		Timings.entityBaseTickTimer.stopTiming()
		return hasUpdate
	}

	fun updateMovement() {
		val diffPosition = (x - lastX) * (x - lastX) + (y - lastY) * (y - lastY) + (z - lastZ) * (z - lastZ)
		val diffRotation = (yaw - lastYaw) * (yaw - lastYaw) + (pitch - lastPitch) * (pitch - lastPitch)
		val diffMotion = (motionX - lastMotionX) * (motionX - lastMotionX) + (motionY - lastMotionY) * (motionY - lastMotionY) + (motionZ - lastMotionZ) * (motionZ - lastMotionZ)
		if (diffPosition > 0.0001 || diffRotation > 1.0) { //0.2 ** 2, 1.5 ** 2
			lastX = x
			lastY = y
			lastZ = z
			lastYaw = yaw
			lastPitch = pitch
			addMovement(x, y + baseOffset, z, yaw, pitch, yaw)
		}
		if (diffMotion > 0.0025 || diffMotion > 0.0001 && motion.lengthSquared() <= 0.0001) { //0.05 ** 2
			lastMotionX = motionX
			lastMotionY = motionY
			lastMotionZ = motionZ
			addMotion(motionX, motionY, motionZ)
		}
	}

	open fun addMovement(x: Double, y: Double, z: Double, yaw: Double, pitch: Double, headYaw: Double) {
		level.addEntityMovement(this, x, y, z, yaw, pitch, headYaw)
	}

	fun addMotion(motionX: Double, motionY: Double, motionZ: Double) {
		val pk = SetEntityMotionPacket()
		pk.eid = id
		pk.motionX = motionX.toFloat()
		pk.motionY = motionY.toFloat()
		pk.motionZ = motionZ.toFloat()
		broadcastPacket(hasSpawned.values, pk)
	}

	override fun getDirectionVector(): Vector3 {
		val vector = super.getDirectionVector()
		return temporalVector!!.setComponents(vector.x, vector.y, vector.z)
	}

	val directionPlane: Vector2
		get() = Vector2((-Math.cos(Math.toRadians(yaw) - Math.PI / 2)).toFloat(), (-Math.sin(Math.toRadians(yaw) - Math.PI / 2)).toFloat()).normalize()

	val horizontalFacing: BlockFace
		get() = BlockFace.fromHorizontalIndex(NukkitMath.floorDouble(yaw * 4.0f / 360.0f + 0.5) and 3)

	open fun onUpdate(currentTick: Int): Boolean {
		if (isClosed) {
			return false
		}
		if (!isAlive) {
			++deadTicks
			if (deadTicks >= 10) {
				despawnFromAll()
				if (!isPlayer) {
					close()
				}
			}
			return deadTicks < 10
		}
		val tickDiff = currentTick - lastUpdate
		if (tickDiff <= 0) {
			return false
		}
		lastUpdate = currentTick
		val hasUpdate = this.entityBaseTick(tickDiff)
		updateMovement()
		return hasUpdate
	}

	open fun mountEntity(entity: Entity): Boolean {
		return mountEntity(entity, SetEntityLinkPacket.TYPE_RIDE)
	}

	/**
	 * Mount or Dismounts an Entity from a/into vehicle
	 *
	 * @param entity The target Entity
	 * @return `true` if the mounting successful
	 */
	open fun mountEntity(entity: Entity, mode: Byte): Boolean {
		Objects.requireNonNull(entity, "The target of the mounting entity can't be null")
		if (entity.riding != null) {
			dismountEntity(entity)
		} else {
			if (isPassenger(entity)) {
				return false
			}

			// Entity entering a vehicle
			val ev = EntityVehicleEnterEvent(entity, this)
			server!!.pluginManager.callEvent(ev)
			if (ev.isCancelled) {
				return false
			}
			broadcastLinkPacket(entity, mode)

			// Add variables to entity
			entity.riding = this
			entity.setDataFlag(DATA_FLAGS, DATA_FLAG_RIDING, true)
			passengers.add(entity)
			entity.setSeatPosition(getMountedOffset(entity))
			updatePassengerPosition(entity)
		}
		return true
	}

	open fun dismountEntity(entity: Entity): Boolean {
		// Run the events
		val ev = EntityVehicleExitEvent(entity, this)
		server!!.pluginManager.callEvent(ev)
		if (ev.isCancelled) {
			return false
		}
		broadcastLinkPacket(entity, SetEntityLinkPacket.TYPE_REMOVE)

		// Refurbish the entity
		entity.riding = null
		entity.setDataFlag(DATA_FLAGS, DATA_FLAG_RIDING, false)
		passengers.remove(entity)
		entity.setSeatPosition(Vector3f())
		updatePassengerPosition(entity)
		return true
	}

	protected fun broadcastLinkPacket(rider: Entity, type: Byte) {
		val pk = SetEntityLinkPacket()
		pk.vehicleUniqueId = id // To the?
		pk.riderUniqueId = rider.id // From who?
		pk.type = type
		broadcastPacket(hasSpawned.values, pk)
	}

	open fun updatePassengers() {
		if (passengers.isEmpty()) {
			return
		}
		for (passenger in ArrayList(passengers)) {
			if (!passenger.isAlive) {
				dismountEntity(passenger)
				continue
			}
			updatePassengerPosition(passenger)
		}
	}

	protected open fun updatePassengerPosition(passenger: Entity) {
		passenger.setPosition(this.add(passenger.seatPosition!!.asVector3()))
	}

	fun setSeatPosition(pos: Vector3f) {
		this.setDataProperty(Vector3fEntityData(DATA_RIDER_SEAT_POSITION, pos))
	}

	val seatPosition: Vector3f?
		get() = getDataPropertyVector3f(DATA_RIDER_SEAT_POSITION)

	open fun getMountedOffset(entity: Entity?): Vector3f {
		return Vector3f(0, height * 0.75f)
	}

	fun scheduleUpdate() {
		level.updateEntities[id] = this
	}

	val isOnFire: Boolean
		get() = fireTicks > 0

	open fun setOnFire(seconds: Int) {
		val ticks = seconds * 20
		if (ticks > fireTicks) {
			fireTicks = ticks
		}
	}

	fun getAbsorption(): Float {
		return absorption
	}

	fun setAbsorption(absorption: Float) {
		if (absorption != this.absorption) {
			this.absorption = absorption
			if (this is Player) this.setAttribute(Attribute.Companion.getAttribute(Attribute.Companion.ABSORPTION).setValue(absorption))
		}
	}

	open val direction: BlockFace?
		get() {
			var rotation = yaw % 360
			if (rotation < 0) {
				rotation += 360.0
			}
			return if (0 <= rotation && rotation < 45 || 315 <= rotation && rotation < 360) {
				BlockFace.SOUTH
			} else if (45 <= rotation && rotation < 135) {
				BlockFace.WEST
			} else if (135 <= rotation && rotation < 225) {
				BlockFace.NORTH
			} else if (225 <= rotation && rotation < 315) {
				BlockFace.EAST
			} else {
				null
			}
		}

	fun extinguish() {
		fireTicks = 0
		this.setDataFlag(DATA_FLAGS, DATA_FLAG_ONFIRE, false)
	}

	fun canTriggerWalking(): Boolean {
		return true
	}

	open fun resetFallDistance() {
		highestPosition = 0.0
	}

	protected fun updateFallState(onGround: Boolean) {
		if (onGround) {
			fallDistance = (highestPosition - y).toFloat()
			if (fallDistance > 0) {
				// check if we fell into at least 1 block of water
				if (this is EntityLiving && getLevelBlock() !is BlockWater) {
					fall(fallDistance)
				}
				resetFallDistance()
			}
		}
	}

	fun fall(fallDistance: Float) {
		if (hasEffect(Effect.SLOW_FALLING)) {
			return
		}
		val damage = Math.floor(fallDistance - 3 - (if (hasEffect(Effect.JUMP)) getEffect(Effect.JUMP)!!.amplifier + 1 else 0).toDouble()).toFloat()
		if (damage > 0) {
			this.attack(EntityDamageEvent(this, DamageCause.FALL, damage))
		}
		if (fallDistance > 0.75) {
			val down = level.getBlock(floor().down())
			if (down.id == Item.FARMLAND) {
				val ev: Event
				ev = if (this is Player) {
					PlayerInteractEvent(this, null, down, null, PlayerInteractEvent.Action.PHYSICAL)
				} else {
					EntityInteractEvent(this, down)
				}
				server!!.pluginManager.callEvent(ev)
				if (ev.isCancelled) {
					return
				}
				level.setBlock(down, get(BlockID.DIRT), false, true)
			}
		}
	}

	fun handleLavaMovement() {
		//todo
	}

	fun moveFlying(strafe: Float, forward: Float, friction: Float) {
		// This is special for Nukkit! :)
		var strafe = strafe
		var forward = forward
		var speed = strafe * strafe + forward * forward
		if (speed >= 1.0E-4f) {
			speed = MathHelper.sqrt(speed)
			if (speed < 1.0f) {
				speed = 1.0f
			}
			speed = friction / speed
			strafe *= speed
			forward *= speed
			val nest = MathHelper.sin((yaw * 3.1415927f / 180.0f).toFloat())
			val place = MathHelper.cos((yaw * 3.1415927f / 180.0f).toFloat())
			motionX += strafe * place - forward * nest.toDouble()
			motionZ += forward * place + strafe * nest.toDouble()
		}
	}

	fun onCollideWithPlayer(entityPlayer: EntityHuman?) {}
	open fun applyEntityCollision(entity: Entity) {
		if (entity.riding !== this && !entity.passengers.contains(this)) {
			var dx = entity.x - x
			var dy = entity.z - z
			var dz = NukkitMath.getDirection(dx, dy)
			if (dz >= 0.009999999776482582) {
				dz = MathHelper.sqrt(dz.toFloat()).toDouble()
				dx /= dz
				dy /= dz
				var d3 = 1.0 / dz
				if (d3 > 1.0) {
					d3 = 1.0
				}
				dx *= d3
				dy *= d3
				dx *= 0.05000000074505806
				dy *= 0.05000000074505806
				dx *= 1f + entityCollisionReduction
				if (riding == null) {
					motionX -= dx
					motionZ -= dy
				}
			}
		}
	}

	open fun onStruckByLightning(entity: Entity?) {
		if (this.attack(EntityDamageByEntityEvent(entity, this, DamageCause.LIGHTNING, 5))) {
			if (fireTicks < 8 * 20) {
				setOnFire(8)
			}
		}
	}

	open fun onInteract(player: Player, item: Item, clickedPos: Vector3?): Boolean {
		return onInteract(player, item)
	}

	fun onInteract(player: Player?, item: Item?): Boolean {
		return false
	}

	protected open fun switchLevel(targetLevel: Level?): Boolean {
		if (isClosed) {
			return false
		}
		if (this.isValid) {
			val ev = EntityLevelChangeEvent(this, level, targetLevel)
			server!!.pluginManager.callEvent(ev)
			if (ev.isCancelled) {
				return false
			}
			level.removeEntity(this)
			if (chunk != null) {
				chunk!!.removeEntity(this)
			}
			despawnFromAll()
		}
		setLevel(targetLevel)
		level.addEntity(this)
		chunk = null
		return true
	}

	val position: Position
		get() = Position(x, y, z, level)

	override fun getLocation(): Location {
		return Location(x, y, z, yaw, pitch, level)
	}

	val isInsideOfWater: Boolean
		get() {
			val y = y + eyeHeight
			val block = level.getBlock(temporalVector!!.setComponents(NukkitMath.floorDouble(x).toDouble(), NukkitMath.floorDouble(y).toDouble(), NukkitMath.floorDouble(z).toDouble()))
			if (block is BlockWater) {
				val f = block.y + 1 - (block.fluidHeightPercent - 0.1111111)
				return y < f
			}
			return false
		}

	val isInsideOfSolid: Boolean
		get() {
			val y = y + eyeHeight
			val block = level.getBlock(
					temporalVector!!.setComponents(
							NukkitMath.floorDouble(x).toDouble(),
							NukkitMath.floorDouble(y).toDouble(),
							NukkitMath.floorDouble(z).toDouble())
			)
			val bb = block.boundingBox
			return bb != null && block.isSolid && !block.isTransparent && bb.intersectsWith(boundingBox)
		}

	val isInsideOfFire: Boolean
		get() {
			for (block in getCollisionBlocks()!!) {
				if (block is BlockFire) {
					return true
				}
			}
			return false
		}

	val isOnLadder: Boolean
		get() {
			val b = this.levelBlock
			return b.id == Block.LADDER
		}

	fun fastMove(dx: Double, dy: Double, dz: Double): Boolean {
		if (dx == 0.0 && dy == 0.0 && dz == 0.0) {
			return true
		}
		Timings.entityMoveTimer.startTiming()
		val newBB = boundingBox!!.getOffsetBoundingBox(dx, dy, dz)
		if (server!!.allowFlight || !level.hasCollision(this, newBB, false)) {
			boundingBox = newBB
		}
		x = (boundingBox!!.minX + boundingBox!!.maxX) / 2
		y = boundingBox!!.minY - ySize
		z = (boundingBox!!.minZ + boundingBox!!.maxZ) / 2
		checkChunks()
		if (!isOnGround || dy != 0.0) {
			val bb = boundingBox!!.clone()
			bb.minY = bb.minY - 0.75
			isOnGround = level.getCollisionBlocks(bb).size > 0
		}
		isCollided = isOnGround
		updateFallState(isOnGround)
		Timings.entityMoveTimer.stopTiming()
		return true
	}

	fun move(dx: Double, dy: Double, dz: Double): Boolean {
		var dx = dx
		var dy = dy
		var dz = dz
		if (dx == 0.0 && dz == 0.0 && dy == 0.0) {
			return true
		}
		return if (keepMovement) {
			boundingBox!!.offset(dx, dy, dz)
			setPosition(temporalVector!!.setComponents((boundingBox!!.minX + boundingBox!!.maxX) / 2, boundingBox!!.minY, (boundingBox!!.minZ + boundingBox!!.maxZ) / 2))
			isOnGround = isPlayer
			true
		} else {
			Timings.entityMoveTimer.startTiming()
			ySize *= 0.4f
			val movX = dx
			val movY = dy
			val movZ = dz
			val axisalignedbb = boundingBox!!.clone()
			var list = level.getCollisionCubes(this, if (level.tickRate > 1) boundingBox!!.getOffsetBoundingBox(dx, dy, dz) else boundingBox!!.addCoord(dx, dy, dz), false, true)
			for (bb in list) {
				dy = bb.calculateYOffset(boundingBox, dy)
			}
			boundingBox!!.offset(0.0, dy, 0.0)
			val fallingFlag = isOnGround || dy != movY && movY < 0
			for (bb in list) {
				dx = bb.calculateXOffset(boundingBox, dx)
			}
			boundingBox!!.offset(dx, 0.0, 0.0)
			for (bb in list) {
				dz = bb.calculateZOffset(boundingBox, dz)
			}
			boundingBox!!.offset(0.0, 0.0, dz)
			if (stepHeight > 0 && fallingFlag && ySize < 0.05 && (movX != dx || movZ != dz)) {
				val cx = dx
				val cy = dy
				val cz = dz
				dx = movX
				dy = stepHeight
				dz = movZ
				val axisalignedbb1 = boundingBox!!.clone()
				boundingBox!!.setBB(axisalignedbb)
				list = level.getCollisionCubes(this, boundingBox!!.addCoord(dx, dy, dz), false)
				for (bb in list) {
					dy = bb.calculateYOffset(boundingBox, dy)
				}
				boundingBox!!.offset(0.0, dy, 0.0)
				for (bb in list) {
					dx = bb.calculateXOffset(boundingBox, dx)
				}
				boundingBox!!.offset(dx, 0.0, 0.0)
				for (bb in list) {
					dz = bb.calculateZOffset(boundingBox, dz)
				}
				boundingBox!!.offset(0.0, 0.0, dz)
				boundingBox!!.offset(0.0, 0.0, dz)
				if (cx * cx + cz * cz >= dx * dx + dz * dz) {
					dx = cx
					dy = cy
					dz = cz
					boundingBox!!.setBB(axisalignedbb1)
				} else {
					ySize += 0.5f
				}
			}
			x = (boundingBox!!.minX + boundingBox!!.maxX) / 2
			y = boundingBox!!.minY - ySize
			z = (boundingBox!!.minZ + boundingBox!!.maxZ) / 2
			checkChunks()
			checkGroundState(movX, movY, movZ, dx, dy, dz)
			updateFallState(isOnGround)
			if (movX != dx) {
				motionX = 0.0
			}
			if (movY != dy) {
				motionY = 0.0
			}
			if (movZ != dz) {
				motionZ = 0.0
			}

			//TODO: vehicle collision events (first we need to spawn them!)
			Timings.entityMoveTimer.stopTiming()
			true
		}
	}

	protected open fun checkGroundState(movX: Double, movY: Double, movZ: Double, dx: Double, dy: Double, dz: Double) {
		isCollidedVertically = movY != dy
		isCollidedHorizontally = movX != dx || movZ != dz
		isCollided = isCollidedHorizontally || isCollidedVertically
		isOnGround = movY != dy && movY < 0
	}

	fun getBlocksAround(): List<Block>? {
		if (blocksAround == null) {
			val minX = NukkitMath.floorDouble(boundingBox!!.minX)
			val minY = NukkitMath.floorDouble(boundingBox!!.minY)
			val minZ = NukkitMath.floorDouble(boundingBox!!.minZ)
			val maxX = NukkitMath.ceilDouble(boundingBox!!.maxX)
			val maxY = NukkitMath.ceilDouble(boundingBox!!.maxY)
			val maxZ = NukkitMath.ceilDouble(boundingBox!!.maxZ)
			blocksAround = ArrayList()
			for (z in minZ..maxZ) {
				for (x in minX..maxX) {
					for (y in minY..maxY) {
						val block = level.getBlock(temporalVector!!.setComponents(x.toDouble(), y.toDouble(), z.toDouble()))
						blocksAround.add(block)
					}
				}
			}
		}
		return blocksAround
	}

	fun getCollisionBlocks(): List<Block>? {
		if (collisionBlocks == null) {
			collisionBlocks = ArrayList()
			for (b in getBlocksAround()!!) {
				if (b.collidesWithBB(boundingBox!!, true)) {
					collisionBlocks.add(b)
				}
			}
		}
		return collisionBlocks
	}

	/**
	 * Returns whether this entity can be moved by currents in liquids.
	 *
	 * @return boolean
	 */
	open fun canBeMovedByCurrents(): Boolean {
		return true
	}

	protected open fun checkBlockCollision() {
		var vector = Vector3(0, 0, 0)
		var portal = false
		for (block in getCollisionBlocks()!!) {
			if (block.id == Block.NETHER_PORTAL) {
				portal = true
				continue
			}
			block.onEntityCollide(this)
			block.addVelocityToEntity(this, vector)
		}
		if (portal) {
			if (inPortalTicks < 80) {
				inPortalTicks = 80
			} else {
				inPortalTicks++
			}
		} else {
			inPortalTicks = 0
		}
		if (vector.lengthSquared() > 0) {
			vector = vector.normalize()
			val d = 0.014
			motionX += vector.x * d
			motionY += vector.y * d
			motionZ += vector.z * d
		}
	}

	fun setPositionAndRotation(pos: Vector3, yaw: Double, pitch: Double): Boolean {
		if (setPosition(pos)) {
			setRotation(yaw, pitch)
			return true
		}
		return false
	}

	fun setRotation(yaw: Double, pitch: Double) {
		this.yaw = yaw
		this.pitch = pitch
		scheduleUpdate()
	}

	/**
	 * Whether the entity can active pressure plates.
	 * Used for [cn.nukkit.entity.passive.EntityBat]s only.
	 *
	 * @return triggers pressure plate
	 */
	open fun doesTriggerPressurePlate(): Boolean {
		return true
	}

	open fun canPassThrough(): Boolean {
		return true
	}

	protected open fun checkChunks() {
		if (chunk == null || chunk!!.x != x.toInt() shr 4 || chunk!!.z != z.toInt() shr 4) {
			if (chunk != null) {
				chunk!!.removeEntity(this)
			}
			chunk = level.getChunk(x.toInt() shr 4, z.toInt() shr 4, true)
			if (!justCreated) {
				val newChunk = level.getChunkPlayers(x.toInt() shr 4, z.toInt() shr 4)
				for (player in ArrayList(hasSpawned.values)) {
					if (!newChunk.containsKey(player.loaderId)) {
						despawnFrom(player)
					} else {
						newChunk.remove(player.loaderId)
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

	fun setPosition(pos: Vector3): Boolean {
		if (isClosed) {
			return false
		}
		if (pos is Position && pos.level != null && pos.level !== level) {
			if (!switchLevel(pos.getLevel())) {
				return false
			}
		}
		x = pos.x
		y = pos.y
		z = pos.z
		recalculateBoundingBox(false) // Don't need to send BB height/width to client on position change
		checkChunks()
		return true
	}

	val motion: Vector3
		get() = Vector3(motionX, motionY, motionZ)

	open fun setMotion(motion: Vector3): Boolean {
		if (!justCreated) {
			val ev = EntityMotionEvent(this, motion)
			server!!.pluginManager.callEvent(ev)
			if (ev.isCancelled) {
				return false
			}
		}
		motionX = motion.x
		motionY = motion.y
		motionZ = motion.z
		if (!justCreated) {
			updateMovement()
		}
		return true
	}

	open fun kill() {
		health = 0f
		scheduleUpdate()
		for (passenger in ArrayList(passengers)) {
			dismountEntity(passenger)
		}
	}

	@JvmOverloads
	fun teleport(pos: Vector3?, cause: TeleportCause? = TeleportCause.PLUGIN): Boolean {
		return this.teleport(fromObject(pos, level, yaw, pitch), cause)
	}

	@JvmOverloads
	fun teleport(pos: Position, cause: TeleportCause? = TeleportCause.PLUGIN): Boolean {
		return this.teleport(fromObject(pos, pos.level, yaw, pitch), cause)
	}

	fun teleport(location: Location): Boolean {
		return this.teleport(location, TeleportCause.PLUGIN)
	}

	open fun teleport(location: Location, cause: TeleportCause?): Boolean {
		val yaw = location.yaw
		val pitch = location.pitch
		val from = location
		var to = location
		if (cause != null) {
			val ev = EntityTeleportEvent(this, from, to)
			server!!.pluginManager.callEvent(ev)
			if (ev.isCancelled) {
				return false
			}
			to = ev.to
		}
		ySize = 0f
		setMotion(temporalVector!!.setComponents(0.0, 0.0, 0.0))
		if (setPositionAndRotation(to, yaw, pitch)) {
			resetFallDistance()
			isOnGround = true
			updateMovement()
			return true
		}
		return false
	}

	fun respawnToAll() {
		for (player in hasSpawned.values) {
			spawnTo(player)
		}
		hasSpawned.clear()
	}

	fun spawnToAll() {
		if (chunk == null || isClosed) {
			return
		}
		for (player in level.getChunkPlayers(chunk!!.x, chunk!!.z).values) {
			if (player.isOnline()) {
				spawnTo(player)
			}
		}
	}

	fun despawnFromAll() {
		for (player in ArrayList(hasSpawned.values)) {
			despawnFrom(player)
		}
	}

	open fun close() {
		if (!isClosed) {
			isClosed = true
			server!!.pluginManager.callEvent(EntityDespawnEvent(this))
			despawnFromAll()
			if (chunk != null) {
				chunk!!.removeEntity(this)
			}
			if (level != null) {
				level.removeEntity(this)
			}
		}
	}

	fun setDataProperty(data: EntityData<*>): Boolean {
		return setDataProperty(data, true)
	}

	fun setDataProperty(data: EntityData<*>, send: Boolean): Boolean {
		if (data != dataProperties!![data.id]) {
			dataProperties.put(data)
			if (send) {
				this.sendData(hasSpawned.values.toTypedArray(), EntityMetadata().put(dataProperties[data.id]))
			}
			return true
		}
		return false
	}

	fun getDataProperty(id: Int): EntityData<*>? {
		return dataProperties!![id]
	}

	fun getDataPropertyInt(id: Int): Int {
		return dataProperties!!.getInt(id)
	}

	fun getDataPropertyShort(id: Int): Int {
		return dataProperties!!.getShort(id)
	}

	fun getDataPropertyByte(id: Int): Int {
		return dataProperties!!.getByte(id)
	}

	fun getDataPropertyBoolean(id: Int): Boolean {
		return dataProperties!!.getBoolean(id)
	}

	fun getDataPropertyLong(id: Int): Long {
		return dataProperties!!.getLong(id)
	}

	fun getDataPropertyString(id: Int): String? {
		return dataProperties!!.getString(id)
	}

	fun getDataPropertyFloat(id: Int): Float {
		return dataProperties!!.getFloat(id)
	}

	fun getDataPropertyNBT(id: Int): CompoundTag? {
		return dataProperties!!.getNBT(id)
	}

	fun getDataPropertyPos(id: Int): Vector3? {
		return dataProperties!!.getPosition(id)
	}

	fun getDataPropertyVector3f(id: Int): Vector3f? {
		return dataProperties!!.getFloatPosition(id)
	}

	fun getDataPropertyType(id: Int): Int {
		return if (dataProperties!!.exists(id)) getDataProperty(id).getType() else -1
	}

	fun setDataFlag(propertyId: Int, id: Int) {
		this.setDataFlag(propertyId, id, true)
	}

	fun setDataFlag(propertyId: Int, id: Int, value: Boolean) {
		if (getDataFlag(propertyId, id) != value) {
			if (propertyId == EntityHuman.Companion.DATA_PLAYER_FLAGS) {
				var flags = getDataPropertyByte(propertyId).toByte()
				flags = flags xor (1 shl id)
				this.setDataProperty(ByteEntityData(propertyId, flags.toInt()))
			} else {
				var flags = getDataPropertyLong(propertyId)
				flags = flags xor (1L shl id)
				this.setDataProperty(LongEntityData(propertyId, flags))
			}
		}
	}

	fun getDataFlag(propertyId: Int, id: Int): Boolean {
		return (if (propertyId == EntityHuman.Companion.DATA_PLAYER_FLAGS) getDataPropertyByte(propertyId) and 0xff else getDataPropertyLong(propertyId)) and (1L shl id) > 0
	}

	override fun setMetadata(metadataKey: String, newMetadataValue: MetadataValue) {
		server!!.entityMetadata.setMetadata(this, metadataKey, newMetadataValue)
	}

	override fun getMetadata(metadataKey: String): List<MetadataValue> {
		return server!!.entityMetadata.getMetadata(this, metadataKey)
	}

	override fun hasMetadata(metadataKey: String): Boolean {
		return server!!.entityMetadata.hasMetadata(this, metadataKey)
	}

	override fun removeMetadata(metadataKey: String, owningPlugin: Plugin) {
		server!!.entityMetadata.removeMetadata(this, metadataKey, owningPlugin)
	}

	override fun equals(obj: Any?): Boolean {
		if (obj == null) {
			return false
		}
		if (javaClass != obj.javaClass) {
			return false
		}
		val other = obj as Entity
		return id == other.id
	}

	override fun hashCode(): Int {
		var hash = 7
		hash = (29 * hash + id).toInt()
		return hash
	}

	companion object {
		const val NETWORK_ID = -1
		const val DATA_TYPE_BYTE = 0
		const val DATA_TYPE_SHORT = 1
		const val DATA_TYPE_INT = 2
		const val DATA_TYPE_FLOAT = 3
		const val DATA_TYPE_STRING = 4
		const val DATA_TYPE_NBT = 5
		const val DATA_TYPE_POS = 6
		const val DATA_TYPE_LONG = 7
		const val DATA_TYPE_VECTOR3F = 8
		const val DATA_FLAGS = 0
		const val DATA_HEALTH = 1 //int (minecart/boat)
		const val DATA_VARIANT = 2 //int
		const val DATA_COLOR = 3
		const val DATA_COLOUR = 3 //byte
		const val DATA_NAMETAG = 4 //string
		const val DATA_OWNER_EID = 5 //long
		const val DATA_TARGET_EID = 6 //long
		const val DATA_AIR = 7 //short
		const val DATA_POTION_COLOR = 8 //int (ARGB!)
		const val DATA_POTION_AMBIENT = 9 //byte
		const val DATA_JUMP_DURATION = 10 //long
		const val DATA_HURT_TIME = 11 //int (minecart/boat)
		const val DATA_HURT_DIRECTION = 12 //int (minecart/boat)
		const val DATA_PADDLE_TIME_LEFT = 13 //float
		const val DATA_PADDLE_TIME_RIGHT = 14 //float
		const val DATA_EXPERIENCE_VALUE = 15 //int (xp orb)
		const val DATA_DISPLAY_ITEM = 16 //int (id | (data << 16))
		const val DATA_DISPLAY_OFFSET = 17 //int
		const val DATA_HAS_DISPLAY = 18 //byte (must be 1 for minecart to show block inside)

		//TODO: add more properties
		const val DATA_ENDERMAN_HELD_RUNTIME_ID = 23 //short
		const val DATA_ENTITY_AGE = 24 //short
		const val DATA_PLAYER_FLAGS = 26 //byte

		/* 27 (int) player "index"? */
		const val DATA_PLAYER_BED_POSITION = 28 //block coords
		const val DATA_FIREBALL_POWER_X = 29 //float
		const val DATA_FIREBALL_POWER_Y = 30
		const val DATA_FIREBALL_POWER_Z = 31

		/* 32 (unknown)
     * 33 (float) fishing bobber
     * 34 (float) fishing bobber
     * 35 (float) fishing bobber */
		const val DATA_POTION_AUX_VALUE = 36 //short
		const val DATA_LEAD_HOLDER_EID = 37 //long
		const val DATA_SCALE = 38 //float
		const val DATA_INTERACTIVE_TAG = 39 //string (button text)
		const val DATA_NPC_SKIN_ID = 40 //string
		const val DATA_URL_TAG = 41 //string
		const val DATA_MAX_AIR = 42 //short
		const val DATA_MARK_VARIANT = 43 //int
		const val DATA_CONTAINER_TYPE = 44 //byte
		const val DATA_CONTAINER_BASE_SIZE = 45 //int
		const val DATA_CONTAINER_EXTRA_SLOTS_PER_STRENGTH = 46 //int
		const val DATA_BLOCK_TARGET = 47 //block coords (ender crystal)
		const val DATA_WITHER_INVULNERABLE_TICKS = 48 //int
		const val DATA_WITHER_TARGET_1 = 49 //long
		const val DATA_WITHER_TARGET_2 = 50 //long
		const val DATA_WITHER_TARGET_3 = 51 //long

		/* 52 (short) */
		const val DATA_BOUNDING_BOX_WIDTH = 53 //float
		const val DATA_BOUNDING_BOX_HEIGHT = 54 //float
		const val DATA_FUSE_LENGTH = 55 //int
		const val DATA_RIDER_SEAT_POSITION = 56 //vector3f
		const val DATA_RIDER_ROTATION_LOCKED = 57 //byte
		const val DATA_RIDER_MAX_ROTATION = 58 //float
		const val DATA_RIDER_MIN_ROTATION = 59 //float
		const val DATA_AREA_EFFECT_CLOUD_RADIUS = 60 //float
		const val DATA_AREA_EFFECT_CLOUD_WAITING = 61 //int
		const val DATA_AREA_EFFECT_CLOUD_PARTICLE_ID = 62 //int

		/* 63 (int) shulker-related */
		const val DATA_SHULKER_ATTACH_FACE = 64 //byte

		/* 65 (short) shulker-related */
		const val DATA_SHULKER_ATTACH_POS = 66 //block coords
		const val DATA_TRADING_PLAYER_EID = 67 //long

		/* 69 (byte) command-block */
		const val DATA_COMMAND_BLOCK_COMMAND = 70 //string
		const val DATA_COMMAND_BLOCK_LAST_OUTPUT = 71 //string
		const val DATA_COMMAND_BLOCK_TRACK_OUTPUT = 72 //byte
		const val DATA_CONTROLLING_RIDER_SEAT_NUMBER = 73 //byte
		const val DATA_STRENGTH = 74 //int
		const val DATA_MAX_STRENGTH = 75 //int

		// 76 (int)
		const val DATA_LIMITED_LIFE = 77
		const val DATA_ARMOR_STAND_POSE_INDEX = 78 // int
		const val DATA_ENDER_CRYSTAL_TIME_OFFSET = 79 // int
		const val DATA_ALWAYS_SHOW_NAMETAG = 80 // byte
		const val DATA_COLOR_2 = 81 // byte

		// 82 unknown
		const val DATA_SCORE_TAG = 83 //String
		const val DATA_BALLOON_ATTACHED_ENTITY = 84 // long
		const val DATA_PUFFERFISH_SIZE = 85
		const val DATA_FLAGS_EXTENDED = 91
		const val DATA_SKIN_ID = 103 // int ???

		// Flags
		const val DATA_FLAG_ONFIRE = 0
		const val DATA_FLAG_SNEAKING = 1
		const val DATA_FLAG_RIDING = 2
		const val DATA_FLAG_SPRINTING = 3
		const val DATA_FLAG_ACTION = 4
		const val DATA_FLAG_INVISIBLE = 5
		const val DATA_FLAG_TEMPTED = 6
		const val DATA_FLAG_INLOVE = 7
		const val DATA_FLAG_SADDLED = 8
		const val DATA_FLAG_POWERED = 9
		const val DATA_FLAG_IGNITED = 10
		const val DATA_FLAG_BABY = 11 //disable head scaling
		const val DATA_FLAG_CONVERTING = 12
		const val DATA_FLAG_CRITICAL = 13
		const val DATA_FLAG_CAN_SHOW_NAMETAG = 14
		const val DATA_FLAG_ALWAYS_SHOW_NAMETAG = 15
		const val DATA_FLAG_IMMOBILE = 16
		const val DATA_FLAG_NO_AI = 16
		const val DATA_FLAG_SILENT = 17
		const val DATA_FLAG_WALLCLIMBING = 18
		const val DATA_FLAG_CAN_CLIMB = 19
		const val DATA_FLAG_SWIMMER = 20
		const val DATA_FLAG_CAN_FLY = 21
		const val DATA_FLAG_WALKER = 22
		const val DATA_FLAG_RESTING = 23
		const val DATA_FLAG_SITTING = 24
		const val DATA_FLAG_ANGRY = 25
		const val DATA_FLAG_INTERESTED = 26
		const val DATA_FLAG_CHARGED = 27
		const val DATA_FLAG_TAMED = 28
		const val DATA_FLAG_ORPHANED = 29
		const val DATA_FLAG_LEASHED = 30
		const val DATA_FLAG_SHEARED = 31
		const val DATA_FLAG_GLIDING = 32
		const val DATA_FLAG_ELDER = 33
		const val DATA_FLAG_MOVING = 34
		const val DATA_FLAG_BREATHING = 35
		const val DATA_FLAG_CHESTED = 36
		const val DATA_FLAG_STACKABLE = 37
		const val DATA_FLAG_SHOWBASE = 38
		const val DATA_FLAG_REARING = 39
		const val DATA_FLAG_VIBRATING = 40
		const val DATA_FLAG_IDLING = 41
		const val DATA_FLAG_EVOKER_SPELL = 42
		const val DATA_FLAG_CHARGE_ATTACK = 43
		const val DATA_FLAG_WASD_CONTROLLED = 44
		const val DATA_FLAG_CAN_POWER_JUMP = 45
		const val DATA_FLAG_LINGER = 46
		const val DATA_FLAG_HAS_COLLISION = 47
		const val DATA_FLAG_GRAVITY = 48
		const val DATA_FLAG_FIRE_IMMUNE = 49
		const val DATA_FLAG_DANCING = 50
		const val DATA_FLAG_ENCHANTED = 51
		const val DATA_FLAG_SHOW_TRIDENT_ROPE = 52 // tridents show an animated rope when enchanted with loyalty after they are thrown and return to their owner. To be combined with DATA_OWNER_EID
		const val DATA_FLAG_CONTAINER_PRIVATE = 53 //inventory is private, doesn't drop contents when killed if true

		//public static final int TransformationComponent 54; ???
		const val DATA_FLAG_SPIN_ATTACK = 55
		const val DATA_FLAG_SWIMMING = 56
		const val DATA_FLAG_BRIBED = 57 //dolphins have this set when they go to find treasure for the player
		const val DATA_FLAG_PREGNANT = 58
		const val DATA_FLAG_LAYING_EGG = 59
		var entityCount: Long = 1
		private val knownEntities: MutableMap<String, Class<out Entity>> = HashMap()
		private val shortNames: MutableMap<String, String> = HashMap()
		fun createEntity(name: String, pos: Position, vararg args: Any?): Entity? {
			return createEntity(name, pos.chunk, getDefaultNBT(pos), *args)
		}

		fun createEntity(type: Int, pos: Position, vararg args: Any?): Entity? {
			return createEntity(type.toString(), pos.chunk, getDefaultNBT(pos), *args)
		}

		fun createEntity(name: String, chunk: FullChunk?, nbt: CompoundTag?, vararg args: Any?): Entity? {
			var entity: Entity? = null
			if (knownEntities.containsKey(name)) {
				val clazz = knownEntities[name] ?: return null
				for (constructor in clazz.constructors) {
					if (entity != null) {
						break
					}
					if (constructor.parameterCount != (if (args == null) 2 else args.size + 2)) {
						continue
					}
					try {
						if (args == null || args.size == 0) {
							entity = constructor.newInstance(chunk, nbt) as Entity
						} else {
							val objects = arrayOfNulls<Any>(args.size + 2)
							objects[0] = chunk
							objects[1] = nbt
							System.arraycopy(args, 0, objects, 2, args.size)
							entity = constructor.newInstance(*objects) as Entity
						}
					} catch (e: Exception) {
						MainLogger.getLogger().logException(e)
					}
				}
			}
			return entity
		}

		fun createEntity(type: Int, chunk: FullChunk?, nbt: CompoundTag?, vararg args: Any?): Entity? {
			return createEntity(type.toString(), chunk, nbt, *args)
		}

		@JvmOverloads
		fun registerEntity(name: String, clazz: Class<out Entity>?, force: Boolean = false): Boolean {
			if (clazz == null) {
				return false
			}
			try {
				val networkId = clazz.getField("NETWORK_ID").getInt(null)
				knownEntities[networkId.toString()] = clazz
			} catch (e: Exception) {
				if (!force) {
					return false
				}
			}
			knownEntities[name] = clazz
			shortNames[clazz.simpleName] = name
			return true
		}

		fun getDefaultNBT(pos: Vector3): CompoundTag {
			return getDefaultNBT(pos, null)
		}

		fun getDefaultNBT(pos: Vector3, motion: Vector3?): CompoundTag {
			val loc = if (pos is Location) pos else null
			return if (loc != null) {
				getDefaultNBT(pos, motion, loc.getYaw().toFloat(), loc.getPitch().toFloat())
			} else getDefaultNBT(pos, motion, 0f, 0f)
		}

		fun getDefaultNBT(pos: Vector3, motion: Vector3?, yaw: Float, pitch: Float): CompoundTag {
			return CompoundTag()
					.putList(ListTag<DoubleTag>("Pos")
							.add(DoubleTag("", pos.x))
							.add(DoubleTag("", pos.y))
							.add(DoubleTag("", pos.z)))
					.putList(ListTag<DoubleTag>("Motion")
							.add(DoubleTag("", motion?.x ?: 0))
							.add(DoubleTag("", motion?.y ?: 0))
							.add(DoubleTag("", motion?.z ?: 0)))
					.putList(ListTag<FloatTag>("Rotation")
							.add(FloatTag("", yaw))
							.add(FloatTag("", pitch)))
		}
	}

	init {
		if (this is Player) {
			return
		}
		init(chunk, nbt)
	}
}