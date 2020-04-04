package cn.nukkit.entity.item

import cn.nukkit.Player
import cn.nukkit.api.API
import cn.nukkit.api.API.Definition
import cn.nukkit.block.Block
import cn.nukkit.block.BlockRail
import cn.nukkit.block.BlockRailActivator
import cn.nukkit.block.BlockRailPowered
import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityHuman
import cn.nukkit.entity.EntityLiving
import cn.nukkit.entity.data.ByteEntityData
import cn.nukkit.entity.data.IntEntityData
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.vehicle.VehicleMoveEvent
import cn.nukkit.event.vehicle.VehicleUpdateEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemMinecart
import cn.nukkit.level.GameRule
import cn.nukkit.level.Location
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.particle.SmokeParticle
import cn.nukkit.math.MathHelper
import cn.nukkit.math.NukkitMath
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.utils.MinecartType
import cn.nukkit.utils.Rail
import java.util.*

/**
 * Created by: larryTheCoder on 2017/6/26.
 *
 *
 * Nukkit Project,
 * Minecart and Riding Project,
 * Package cn.nukkit.entity.item in project Nukkit.
 */
abstract class EntityMinecartAbstract(chunk: FullChunk?, nbt: CompoundTag?) : EntityVehicle(chunk, nbt) {
	override var name: String? = null
	private var currentSpeed = 0.0

	/**
	 * Get the minecart display block
	 *
	 * @return Block of minecart display block
	 */
	@get:API(usage = API.Usage.STABLE, definition = Definition.UNIVERSAL)
	var displayBlock: Block? = null
		private set

	/**
	 * Is the minecart can be slowed when empty?
	 *
	 * @return boolean
	 */
	/**
	 * Set the minecart slowdown flag
	 *
	 * @param slow The slowdown flag
	 */
	// Plugins modifiers
	@get:API(usage = API.Usage.EXPERIMENTAL, definition = Definition.UNIVERSAL)
	@set:API(usage = API.Usage.EXPERIMENTAL, definition = Definition.UNIVERSAL)
	var isSlowWhenEmpty = true
	private var derailedX = 0.5
	private var derailedY = 0.5
	private var derailedZ = 0.5
	private var flyingX = 0.95
	private var flyingY = 0.95
	private var flyingZ = 0.95
	var maxSpeed = 0.4
		private set
	private val devs = false // Avoid maintained features into production
	abstract val type: MinecartType
	abstract val isRideable: Boolean
	override val height: Float
		get() = 0.7f

	override val width: Float
		get() = 0.98f

	protected override val drag: Float
		protected get() = 0.1f

	override val baseOffset: Float
		get() = 0.35f

	override fun hasCustomName(): Boolean {
		return name != null
	}

	override fun canDoInteraction(): Boolean {
		return passengers.isEmpty() && displayBlock == null
	}

	public override fun initEntity() {
		super.initEntity()
		prepareDataProperty()
	}

	override fun onUpdate(currentTick: Int): Boolean {
		if (closed) {
			return false
		}
		if (!this.isAlive) {
			++deadTicks
			if (deadTicks >= 10) {
				despawnFromAll()
				close()
			}
			return deadTicks < 10
		}
		val tickDiff = currentTick - lastUpdate
		if (tickDiff <= 0) {
			return false
		}
		lastUpdate = currentTick
		if (isAlive) {
			super.onUpdate(currentTick)

			// The damage token
			if (getHealth() < 20) {
				setHealth(getHealth() + 1)
			}

			// Entity variables
			lastX = x
			lastY = y
			lastZ = z
			motionY -= 0.03999999910593033
			val dx = MathHelper.floor(x)
			var dy = MathHelper.floor(y)
			val dz = MathHelper.floor(z)

			// Some hack to check rails
			if (Rail.isRailBlock(level.getBlockIdAt(dx, dy - 1, dz))) {
				--dy
			}
			val block = level.getBlock(Vector3(dx.toDouble(), dy.toDouble(), dz.toDouble()))

			// Ensure that the block is a rail
			if (Rail.isRailBlock(block)) {
				processMovement(dx, dy, dz, block as BlockRail)
				// Activate the minecart/TNT
				if (block is BlockRailActivator && block.isActive) {
					activate(dx, dy, dz, block.damage and 0x8 != 0)
				}
			} else {
				setFalling()
			}
			checkBlockCollision()

			// Minecart head
			pitch = 0.0
			val diffX = lastX - x
			val diffZ = lastZ - z
			var yawToChange = yaw
			if (diffX * diffX + diffZ * diffZ > 0.001) {
				yawToChange = Math.atan2(diffZ, diffX) * 180 / Math.PI
			}

			// Reverse yaw if yaw is below 0
			if (yawToChange < 0) {
				// -90-(-90)-(-90) = 90
				yawToChange -= yawToChange - yawToChange
			}
			setRotation(yawToChange, pitch)
			val from = Location(lastX, lastY, lastZ, lastYaw, lastPitch, level)
			val to = Location(x, y, z, yaw, pitch, level)
			getServer().pluginManager.callEvent(VehicleUpdateEvent(this))
			if (from != to) {
				getServer().pluginManager.callEvent(VehicleMoveEvent(this, from, to))
			}

			// Collisions
			for (entity in level.getNearbyEntities(boundingBox!!.grow(0.2, 0.0, 0.2), this)) {
				if (!passengers.contains(entity) && entity is EntityMinecartAbstract) {
					entity.applyEntityCollision(this)
				}
			}
			val linkedIterator = passengers.iterator()
			while (linkedIterator.hasNext()) {
				val linked = linkedIterator.next()
				if (!linked!!.isAlive) {
					if (linked.riding === this) {
						linked.riding = null
					}
					linkedIterator.remove()
				}
			}

			// No need to onGround or Motion diff! This always have an update
			return true
		}
		return false
	}

	override fun attack(source: EntityDamageEvent): Boolean {
		return if (invulnerable) {
			false
		} else {
			source.damage = source.damage * 15
			val attack = super.attack(source)
			if (isAlive) {
				performHurtAnimation()
			}
			attack
		}
	}

	open fun dropItem() {
		level.dropItem(this, ItemMinecart())
	}

	override fun kill() {
		super.kill()
		if (level.getGameRules().getBoolean(GameRule.DO_ENTITY_DROPS)) {
			dropItem()
		}
	}

	override fun close() {
		super.close()
		for (entity in passengers) {
			if (entity is Player) {
				entity.riding = null
			}
		}
		val particle = SmokeParticle(this)
		level.addParticle(particle)
	}

	override fun onInteract(p: Player, item: Item, clickedPos: Vector3?): Boolean {
		if (!passengers.isEmpty() && isRideable) {
			return false
		}
		if (displayBlock == null) {
			mountEntity(p)
		}
		return super.onInteract(p, item, clickedPos)
	}

	override fun applyEntityCollision(entity: Entity) {
		if (entity !== riding) {
			if (entity is EntityLiving
					&& entity !is EntityHuman
					&& motionX * motionX + motionZ * motionZ > 0.01 && passengers.isEmpty()
					&& entity.riding == null && displayBlock == null) {
				if (riding == null && devs) {
					mountEntity(entity) // TODO: rewrite (weird riding)
				}
			}
			var motiveX = entity.x - x
			var motiveZ = entity.z - z
			var square = motiveX * motiveX + motiveZ * motiveZ
			if (square >= 9.999999747378752E-5) {
				square = Math.sqrt(square)
				motiveX /= square
				motiveZ /= square
				var next = 1 / square
				if (next > 1) {
					next = 1.0
				}
				motiveX *= next
				motiveZ *= next
				motiveX *= 0.10000000149011612
				motiveZ *= 0.10000000149011612
				motiveX *= 1 + entityCollisionReduction
				motiveZ *= 1 + entityCollisionReduction
				motiveX *= 0.5
				motiveZ *= 0.5
				if (entity is EntityMinecartAbstract) {
					val mine = entity
					val desinityX = mine.x - x
					val desinityZ = mine.z - z
					val vector = Vector3(desinityX, 0, desinityZ).normalize()
					val vec = Vector3(MathHelper.cos(yaw.toFloat() * 0.017453292f).toDouble(), 0, MathHelper.sin(yaw.toFloat() * 0.017453292f).toDouble()).normalize()
					val desinityXZ = Math.abs(vector.dot(vec))
					if (desinityXZ < 0.800000011920929) {
						return
					}
					var motX = mine.motionX + motionX
					var motZ = mine.motionZ + motionZ
					if (mine.type.id == 2 && type.id != 2) {
						motionX *= 0.20000000298023224
						motionZ *= 0.20000000298023224
						motionX += mine.motionX - motiveX
						motionZ += mine.motionZ - motiveZ
						mine.motionX *= 0.949999988079071
						mine.motionZ *= 0.949999988079071
					} else if (mine.type.id != 2 && type.id == 2) {
						mine.motionX *= 0.20000000298023224
						mine.motionZ *= 0.20000000298023224
						motionX += mine.motionX + motiveX
						motionZ += mine.motionZ + motiveZ
						motionX *= 0.949999988079071
						motionZ *= 0.949999988079071
					} else {
						motX /= 2.0
						motZ /= 2.0
						motionX *= 0.20000000298023224
						motionZ *= 0.20000000298023224
						motionX += motX - motiveX
						motionZ += motZ - motiveZ
						mine.motionX *= 0.20000000298023224
						mine.motionZ *= 0.20000000298023224
						mine.motionX += motX + motiveX
						mine.motionZ += motZ + motiveZ
					}
				} else {
					motionX -= motiveX
					motionZ -= motiveZ
				}
			}
		}
	}

	override fun saveNBT() {
		super.saveNBT()
		saveEntityData()
	}

	protected open fun activate(x: Int, y: Int, z: Int, flag: Boolean) {}
	private var hasUpdated = false
	private fun setFalling() {
		motionX = NukkitMath.clamp(motionX, -maxSpeed, maxSpeed)
		motionZ = NukkitMath.clamp(motionZ, -maxSpeed, maxSpeed)
		if (!hasUpdated) {
			for (linked in passengers) {
				linked.seatPosition = getMountedOffset(linked).add(0f, 0.35f)
				updatePassengerPosition(linked)
			}
			hasUpdated = true
		}
		if (onGround) {
			motionX *= derailedX
			motionY *= derailedY
			motionZ *= derailedZ
		}
		move(motionX, motionY, motionZ)
		if (!onGround) {
			motionX *= flyingX
			motionY *= flyingY
			motionZ *= flyingZ
		}
	}

	private fun processMovement(dx: Int, dy: Int, dz: Int, block: BlockRail) {
		fallDistance = 0.0f
		val vector = getNextRail(x, y, z)
		y = dy.toDouble()
		var isPowered = false
		var isSlowed = false
		if (block is BlockRailPowered) {
			isPowered = block.isActive
			isSlowed = !block.isActive
		}
		when (Rail.Orientation.byMetadata(block.realMeta)) {
			Rail.Orientation.ASCENDING_NORTH -> {
				motionX -= 0.0078125
				y += 1.0
			}
			Rail.Orientation.ASCENDING_SOUTH -> {
				motionX += 0.0078125
				y += 1.0
			}
			Rail.Orientation.ASCENDING_EAST -> {
				motionZ += 0.0078125
				y += 1.0
			}
			Rail.Orientation.ASCENDING_WEST -> {
				motionZ -= 0.0078125
				y += 1.0
			}
		}
		val facing = matrix[block.realMeta]
		var facing1 = (facing[1][0] - facing[0][0]).toDouble()
		var facing2 = (facing[1][2] - facing[0][2]).toDouble()
		val speedOnTurns = Math.sqrt(facing1 * facing1 + facing2 * facing2)
		val realFacing = motionX * facing1 + motionZ * facing2
		if (realFacing < 0) {
			facing1 = -facing1
			facing2 = -facing2
		}
		var squareOfFame = Math.sqrt(motionX * motionX + motionZ * motionZ)
		if (squareOfFame > 2) {
			squareOfFame = 2.0
		}
		motionX = squareOfFame * facing1 / speedOnTurns
		motionZ = squareOfFame * facing2 / speedOnTurns
		var expectedSpeed: Double
		var playerYawNeg: Double // PlayerYawNegative
		var playerYawPos: Double // PlayerYawPositive
		var motion: Double
		val linked = passenger
		if (linked is EntityLiving) {
			expectedSpeed = currentSpeed
			if (expectedSpeed > 0) {
				// This is a trajectory (Angle of elevation)
				playerYawNeg = -Math.sin(linked.yaw * Math.PI / 180.0f)
				playerYawPos = Math.cos(linked.yaw * Math.PI / 180.0f)
				motion = motionX * motionX + motionZ * motionZ
				if (motion < 0.01) {
					motionX += playerYawNeg * 0.1
					motionZ += playerYawPos * 0.1
					isSlowed = false
				}
			}
		}

		//http://minecraft.gamepedia.com/Powered_Rail#Rail
		if (isSlowed) {
			expectedSpeed = Math.sqrt(motionX * motionX + motionZ * motionZ)
			if (expectedSpeed < 0.03) {
				motionX *= 0.0
				motionY *= 0.0
				motionZ *= 0.0
			} else {
				motionX *= 0.5
				motionY *= 0.0
				motionZ *= 0.5
			}
		}
		playerYawNeg = dx.toDouble() + 0.5 + facing[0][0].toDouble() * 0.5
		playerYawPos = dz.toDouble() + 0.5 + facing[0][2].toDouble() * 0.5
		motion = dx.toDouble() + 0.5 + facing[1][0].toDouble() * 0.5
		val wallOfFame = dz.toDouble() + 0.5 + facing[1][2].toDouble() * 0.5
		facing1 = motion - playerYawNeg
		facing2 = wallOfFame - playerYawPos
		var motX: Double
		var motZ: Double
		if (facing1 == 0.0) {
			x = dx.toDouble() + 0.5
			expectedSpeed = z - dz.toDouble()
		} else if (facing2 == 0.0) {
			z = dz.toDouble() + 0.5
			expectedSpeed = x - dx.toDouble()
		} else {
			motX = x - playerYawNeg
			motZ = z - playerYawPos
			expectedSpeed = (motX * facing1 + motZ * facing2) * 2
		}
		x = playerYawNeg + facing1 * expectedSpeed
		z = playerYawPos + facing2 * expectedSpeed
		setPosition(Vector3(x, y, z)) // Hehe, my minstake :3
		motX = motionX
		motZ = motionZ
		if (!passengers.isEmpty()) {
			motX *= 0.75
			motZ *= 0.75
		}
		motX = NukkitMath.clamp(motX, -maxSpeed, maxSpeed)
		motZ = NukkitMath.clamp(motZ, -maxSpeed, maxSpeed)
		move(motX, 0.0, motZ)
		if (facing[0][1] != 0 && MathHelper.floor(x) - dx == facing[0][0] && MathHelper.floor(z) - dz == facing[0][2]) {
			setPosition(Vector3(x, y + facing[0][1].toDouble(), z))
		} else if (facing[1][1] != 0 && MathHelper.floor(x) - dx == facing[1][0] && MathHelper.floor(z) - dz == facing[1][2]) {
			setPosition(Vector3(x, y + facing[1][1].toDouble(), z))
		}
		applyDrag()
		val vector1 = getNextRail(x, y, z)
		if (vector1 != null && vector != null) {
			val d14 = (vector.y - vector1.y) * 0.05
			squareOfFame = Math.sqrt(motionX * motionX + motionZ * motionZ)
			if (squareOfFame > 0) {
				motionX = motionX / squareOfFame * (squareOfFame + d14)
				motionZ = motionZ / squareOfFame * (squareOfFame + d14)
			}
			setPosition(Vector3(x, vector1.y, z))
		}
		val floorX = MathHelper.floor(x)
		val floorZ = MathHelper.floor(z)
		if (floorX != dx || floorZ != dz) {
			squareOfFame = Math.sqrt(motionX * motionX + motionZ * motionZ)
			motionX = squareOfFame * (floorX - dx) as Double
			motionZ = squareOfFame * (floorZ - dz) as Double
		}
		if (isPowered) {
			val newMovie = Math.sqrt(motionX * motionX + motionZ * motionZ)
			if (newMovie > 0.01) {
				val nextMovie = 0.06
				motionX += motionX / newMovie * nextMovie
				motionZ += motionZ / newMovie * nextMovie
			} else if (block.orientation == Rail.Orientation.STRAIGHT_NORTH_SOUTH) {
				if (level.getBlock(Vector3(dx - 1, dy, dz)).isNormalBlock) {
					motionX = 0.02
				} else if (level.getBlock(Vector3(dx + 1, dy, dz)).isNormalBlock) {
					motionX = -0.02
				}
			} else if (block.orientation == Rail.Orientation.STRAIGHT_EAST_WEST) {
				if (level.getBlock(Vector3(dx, dy, dz - 1)).isNormalBlock) {
					motionZ = 0.02
				} else if (level.getBlock(Vector3(dx, dy, dz + 1)).isNormalBlock) {
					motionZ = -0.02
				}
			}
		}
	}

	private fun applyDrag() {
		if (!passengers.isEmpty() || !isSlowWhenEmpty) {
			motionX *= 0.996999979019165
			motionY *= 0.0
			motionZ *= 0.996999979019165
		} else {
			motionX *= 0.9599999785423279
			motionY *= 0.0
			motionZ *= 0.9599999785423279
		}
	}

	private fun getNextRail(dx: Double, dy: Double, dz: Double): Vector3? {
		var dx = dx
		var dy = dy
		var dz = dz
		val checkX = MathHelper.floor(dx)
		var checkY = MathHelper.floor(dy)
		val checkZ = MathHelper.floor(dz)
		if (Rail.isRailBlock(level.getBlockIdAt(checkX, checkY - 1, checkZ))) {
			--checkY
		}
		val block = level.getBlock(Vector3(checkX.toDouble(), checkY.toDouble(), checkZ.toDouble()))
		return if (Rail.isRailBlock(block)) {
			val facing = matrix[(block as BlockRail).realMeta]
			val rail: Double
			// Genisys mistake (Doesn't check surrounding more exactly)
			val nextOne = checkX.toDouble() + 0.5 + facing[0][0].toDouble() * 0.5
			val nextTwo = checkY.toDouble() + 0.5 + facing[0][1].toDouble() * 0.5
			val nextThree = checkZ.toDouble() + 0.5 + facing[0][2].toDouble() * 0.5
			val nextFour = checkX.toDouble() + 0.5 + facing[1][0].toDouble() * 0.5
			val nextFive = checkY.toDouble() + 0.5 + facing[1][1].toDouble() * 0.5
			val nextSix = checkZ.toDouble() + 0.5 + facing[1][2].toDouble() * 0.5
			val nextSeven = nextFour - nextOne
			val nextEight = (nextFive - nextTwo) * 2
			val nextMax = nextSix - nextThree
			rail = if (nextSeven == 0.0) {
				dz - checkZ.toDouble()
			} else if (nextMax == 0.0) {
				dx - checkX.toDouble()
			} else {
				val whatOne = dx - nextOne
				val whatTwo = dz - nextThree
				(whatOne * nextSeven + whatTwo * nextMax) * 2
			}
			dx = nextOne + nextSeven * rail
			dy = nextTwo + nextEight * rail
			dz = nextThree + nextMax * rail
			if (nextEight < 0) {
				++dy
			}
			if (nextEight > 0) {
				dy += 0.5
			}
			Vector3(dx, dy, dz)
		} else {
			null
		}
	}

	/**
	 * Used to multiply the minecart current speed
	 *
	 * @param speed The speed of the minecart that will be calculated
	 */
	fun setCurrentSpeed(speed: Double) {
		currentSpeed = speed
	}

	private fun prepareDataProperty() {
		rollingAmplitude = 0
		setRollingDirection(1)
		if (namedTag!!.contains("CustomDisplayTile")) {
			if (namedTag!!.getBoolean("CustomDisplayTile")) {
				val display = namedTag!!.getInt("DisplayTile")
				val offSet = namedTag!!.getInt("DisplayOffset")
				setDataProperty(ByteEntityData(Entity.Companion.DATA_HAS_DISPLAY, 1))
				setDataProperty(IntEntityData(Entity.Companion.DATA_DISPLAY_ITEM, display))
				setDataProperty(IntEntityData(Entity.Companion.DATA_DISPLAY_OFFSET, offSet))
			}
		} else {
			val display = if (displayBlock == null) 0 else displayBlock!!.id
					or displayBlock!!.damage shl 16
			if (display == 0) {
				setDataProperty(ByteEntityData(Entity.Companion.DATA_HAS_DISPLAY, 0))
				return
			}
			setDataProperty(ByteEntityData(Entity.Companion.DATA_HAS_DISPLAY, 1))
			setDataProperty(IntEntityData(Entity.Companion.DATA_DISPLAY_ITEM, display))
			setDataProperty(IntEntityData(Entity.Companion.DATA_DISPLAY_OFFSET, 6))
		}
	}

	private fun saveEntityData() {
		val hasDisplay = (super.getDataPropertyByte(Entity.Companion.DATA_HAS_DISPLAY) == 1
				|| displayBlock != null)
		val display: Int
		val offSet: Int
		namedTag!!.putBoolean("CustomDisplayTile", hasDisplay)
		if (hasDisplay) {
			display = (displayBlock!!.id
					or displayBlock!!.damage shl 16)
			offSet = getDataPropertyInt(Entity.Companion.DATA_DISPLAY_OFFSET)
			namedTag!!.putInt("DisplayTile", display)
			namedTag!!.putInt("DisplayOffset", offSet)
		}
	}

	/**
	 * Set the minecart display block
	 *
	 * @param block The block that will changed. Set `null` for BlockAir
	 * @return `true` if the block is normal block
	 */
	fun setDisplayBlock(block: Block?): Boolean {
		return setDisplayBlock(block, true)
	}

	/**
	 * Set the minecart display block
	 *
	 * @param block The block that will changed. Set `null` for BlockAir
	 * @param update Do update for the block. (This state changes if you want to show the block)
	 * @return `true` if the block is normal block
	 */
	@API(usage = API.Usage.MAINTAINED, definition = Definition.UNIVERSAL)
	fun setDisplayBlock(block: Block?, update: Boolean): Boolean {
		if (!update) {
			if (block!!.isNormalBlock) {
				displayBlock = block
			} else {
				displayBlock = null
			}
			return true
		}
		if (block != null) {
			if (block.isNormalBlock) {
				displayBlock = block
				val display = (displayBlock!!.id
						or displayBlock!!.damage shl 16)
				setDataProperty(ByteEntityData(Entity.Companion.DATA_HAS_DISPLAY, 1))
				setDataProperty(IntEntityData(Entity.Companion.DATA_DISPLAY_ITEM, display))
				displayBlockOffset = 6
			}
		} else {
			// Set block to air (default).
			displayBlock = null
			setDataProperty(ByteEntityData(Entity.Companion.DATA_HAS_DISPLAY, 0))
			setDataProperty(IntEntityData(Entity.Companion.DATA_DISPLAY_ITEM, 0))
			displayBlockOffset = 0
		}
		return true
	}

	/**
	 * Get the block display offset
	 *
	 * @return integer
	 */
	/**
	 * Set the block offset.
	 *
	 * @param offset The offset
	 */
	@get:API(usage = API.Usage.EXPERIMENTAL, definition = Definition.UNIVERSAL)
	@set:API(usage = API.Usage.EXPERIMENTAL, definition = Definition.PLATFORM_NATIVE)
	var displayBlockOffset: Int
		get() = super.getDataPropertyInt(Entity.Companion.DATA_DISPLAY_OFFSET)
		set(offset) {
			setDataProperty(IntEntityData(Entity.Companion.DATA_DISPLAY_OFFSET, offset))
		}

	var flyingVelocityMod: Vector3
		get() = Vector3(flyingX, flyingY, flyingZ)
		set(flying) {
			Objects.requireNonNull(flying, "Flying velocity modifiers cannot be null")
			flyingX = flying.getX()
			flyingY = flying.getY()
			flyingZ = flying.getZ()
		}

	var derailedVelocityMod: Vector3
		get() = Vector3(derailedX, derailedY, derailedZ)
		set(derailed) {
			Objects.requireNonNull(derailed, "Derailed velocity modifiers cannot be null")
			derailedX = derailed.getX()
			derailedY = derailed.getY()
			derailedZ = derailed.getZ()
		}

	fun setMaximumSpeed(speed: Double) {
		maxSpeed = speed
	}

	companion object {
		private val matrix = arrayOf(arrayOf(intArrayOf(0, 0, -1), intArrayOf(0, 0, 1)), arrayOf(intArrayOf(-1, 0, 0), intArrayOf(1, 0, 0)), arrayOf(intArrayOf(-1, -1, 0), intArrayOf(1, 0, 0)), arrayOf(intArrayOf(-1, 0, 0), intArrayOf(1, -1, 0)), arrayOf(intArrayOf(0, 0, -1), intArrayOf(0, -1, 1)), arrayOf(intArrayOf(0, -1, -1), intArrayOf(0, 0, 1)), arrayOf(intArrayOf(0, 0, 1), intArrayOf(1, 0, 0)), arrayOf(intArrayOf(0, 0, 1), intArrayOf(-1, 0, 0)), arrayOf(intArrayOf(0, 0, -1), intArrayOf(-1, 0, 0)), arrayOf(intArrayOf(0, 0, -1), intArrayOf(1, 0, 0)))
	}

	init {
		maxHealth = 40
		setHealth(40f)
	}
}