package cn.nukkit.blockentity

import cn.nukkit.Server
import cn.nukkit.block.Block
import cn.nukkit.level.Position
import cn.nukkit.level.format.FullChunk
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.utils.ChunkException
import cn.nukkit.utils.MainLogger
import co.aikar.timings.Timing
import co.aikar.timings.Timings
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import kotlin.collections.set

/**
 * @author MagicDroidX
 */
abstract class BlockEntity(chunk: FullChunk?, nbt: CompoundTag) : Position() {
	@kotlin.jvm.JvmField
    var chunk: FullChunk?
	var name: String
	var id: Long
	var isMovable = true
	@kotlin.jvm.JvmField
    var closed = false
	@kotlin.jvm.JvmField
    var namedTag: CompoundTag
	protected var lastUpdate: Long
	protected var server: Server
	protected var timing: Timing
	protected open fun initBlockEntity() {}
	val saveId: String?
		get() = knownBlockEntities.inverse()[javaClass]

	open fun saveNBT() {
		namedTag.putString("id", saveId)
		namedTag.putInt("x", getX().toInt())
		namedTag.putInt("y", getY().toInt())
		namedTag.putInt("z", getZ().toInt())
		namedTag.putBoolean("isMovable", isMovable)
	}

	val cleanedNBT: CompoundTag?
		get() {
			saveNBT()
			val tag = namedTag.clone()
			tag.remove("x").remove("y").remove("z").remove("id")
			return if (tag.tags.size > 0) {
				tag
			} else {
				null
			}
		}

	open val block: Block?
		get() = this.levelBlock

	abstract val isBlockEntityValid: Boolean
	open fun onUpdate(): Boolean {
		return false
	}

	fun scheduleUpdate() {
		level.scheduleBlockEntityUpdate(this)
	}

	open fun close() {
		if (!closed) {
			closed = true
			if (chunk != null) {
				chunk!!.removeBlockEntity(this)
			}
			if (level != null) {
				level.removeBlockEntity(this)
			}
			level = null
		}
	}

	open fun onBreak() {}
	open fun setDirty() {
		chunk!!.setChanged()
	}

	open fun getName(): String? {
		return name
	}

	companion object {
		//WARNING: DO NOT CHANGE ANY NAME HERE, OR THE CLIENT WILL CRASH
		const val CHEST = "Chest"
		const val ENDER_CHEST = "EnderChest"
		const val FURNACE = "Furnace"
		const val SIGN = "Sign"
		const val MOB_SPAWNER = "MobSpawner"
		const val ENCHANT_TABLE = "EnchantTable"
		const val SKULL = "Skull"
		const val FLOWER_POT = "FlowerPot"
		const val BREWING_STAND = "BrewingStand"
		const val DAYLIGHT_DETECTOR = "DaylightDetector"
		const val MUSIC = "Music"
		const val ITEM_FRAME = "ItemFrame"
		const val CAULDRON = "Cauldron"
		const val BEACON = "Beacon"
		const val PISTON_ARM = "PistonArm"
		const val MOVING_BLOCK = "MovingBlock"
		const val COMPARATOR = "Comparator"
		const val HOPPER = "Hopper"
		const val BED = "Bed"
		const val JUKEBOX = "Jukebox"
		const val SHULKER_BOX = "ShulkerBox"
		const val BANNER = "Banner"
		var count: Long = 1
		private val knownBlockEntities: BiMap<String, Class<out BlockEntity>> = HashBiMap.create(21)
		@kotlin.jvm.JvmStatic
        fun createBlockEntity(type: String, chunk: FullChunk?, nbt: CompoundTag?, vararg args: Any?): BlockEntity? {
			var type = type
			type = type.replaceFirst("BlockEntity".toRegex(), "") //TODO: Remove this after the first release
			var blockEntity: BlockEntity? = null
			if (knownBlockEntities.containsKey(type)) {
				val clazz = knownBlockEntities[type] ?: return null
				for (constructor in clazz.constructors) {
					if (blockEntity != null) {
						break
					}
					if (constructor.parameterCount != (if (args == null) 2 else args.size + 2)) {
						continue
					}
					try {
						if (args == null || args.size == 0) {
							blockEntity = constructor.newInstance(chunk, nbt) as BlockEntity
						} else {
							val objects = arrayOfNulls<Any>(args.size + 2)
							objects[0] = chunk
							objects[1] = nbt
							System.arraycopy(args, 0, objects, 2, args.size)
							blockEntity = constructor.newInstance(*objects) as BlockEntity
						}
					} catch (e: Exception) {
						MainLogger.getLogger().logException(e)
					}
				}
			}
			return blockEntity
		}

		fun registerBlockEntity(name: String?, c: Class<out BlockEntity>?): Boolean {
			if (c == null) {
				return false
			}
			knownBlockEntities[name] = c
			return true
		}

		fun getDefaultCompound(pos: Vector3, id: String?): CompoundTag {
			return CompoundTag("")
					.putString("id", id)
					.putInt("x", pos.floorX)
					.putInt("y", pos.floorY)
					.putInt("z", pos.floorZ)
		}
	}

	init {
		if (chunk == null || chunk.provider == null) {
			throw ChunkException("Invalid garbage Chunk given to Block Entity")
		}
		timing = Timings.getBlockEntityTiming(this)
		server = chunk.provider.level.server
		this.chunk = chunk
		setLevel(chunk.provider.level)
		namedTag = nbt
		name = ""
		lastUpdate = System.currentTimeMillis()
		id = count++
		x = namedTag.getInt("x").toDouble()
		y = namedTag.getInt("y").toDouble()
		z = namedTag.getInt("z").toDouble()
		isMovable = namedTag.getBoolean("isMovable")
		initBlockEntity()
		this.chunk!!.addBlockEntity(this)
		getLevel().addBlockEntity(this)
	}
}