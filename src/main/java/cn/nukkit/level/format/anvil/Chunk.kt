package cn.nukkit.level.format.anvil

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.block.Block
import cn.nukkit.level.format.LevelProvider
import cn.nukkit.level.format.anvil.palette.BiomePalette
import cn.nukkit.level.format.generic.BaseChunk
import cn.nukkit.level.format.generic.BaseRegionLoader
import cn.nukkit.level.format.generic.EmptyChunkSection
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.*
import cn.nukkit.utils.BinaryStream
import cn.nukkit.utils.BlockUpdateEntry
import cn.nukkit.utils.ChunkException
import cn.nukkit.utils.Zlib
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.IOException
import java.lang.reflect.Constructor
import java.nio.ByteOrder
import java.util.*
import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.collections.Set
import kotlin.collections.indices
import kotlin.collections.set

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class Chunk @JvmOverloads constructor(level: LevelProvider?, nbt: CompoundTag? = null) : BaseChunk() {
	protected var inhabitedTime: Long
	protected var terrainPopulated: Boolean
	protected var terrainGenerated: Boolean
	override fun clone(): Chunk {
		return super.clone() as Chunk
	}

	constructor(providerClass: Class<out LevelProvider?>?) : this(null as LevelProvider?, null) {
		this.providerClass = providerClass
	}

	constructor(providerClass: Class<out LevelProvider?>?, nbt: CompoundTag?) : this(null as LevelProvider?, nbt) {
		this.providerClass = providerClass
	}

	override var isPopulated: Boolean
		get() = terrainPopulated
		set(value) {
			if (value != terrainPopulated) {
				terrainPopulated = value
				setChanged()
			}
		}

	override fun setPopulated() {
		this.populated = true
	}

	override var isGenerated: Boolean
		get() = terrainGenerated || terrainPopulated
		set(value) {
			if (terrainGenerated != value) {
				terrainGenerated = value
				setChanged()
			}
		}

	override fun setGenerated() {
		this.generated = true
	}

	val nBT: CompoundTag
		get() {
			val tag = CompoundTag()
			tag.put("LightPopulated", ByteTag("LightPopulated", (if (isLightPopulated()) 1 else 0).toByte()))
			tag.put("InhabitedTime", LongTag("InhabitedTime", inhabitedTime))
			tag.put("V", ByteTag("V", 1.toByte()))
			tag.put("TerrainGenerated", ByteTag("TerrainGenerated", (if (isGenerated) 1 else 0).toByte()))
			tag.put("TerrainPopulated", ByteTag("TerrainPopulated", (if (isPopulated) 1 else 0).toByte()))
			return tag
		}

	override fun toFastBinary(): ByteArray? {
		val nbt = nBT.copy()
		nbt.remove("BiomeColors")
		nbt.putInt("xPos", this.getX())
		nbt.putInt("zPos", this.getZ())
		nbt.putByteArray("Biomes", this.getBiomeIdArray())
		val heightInts = IntArray(256)
		val heightBytes: ByteArray = this.getHeightMapArray()
		for (i in heightInts.indices) {
			heightInts[i] = heightBytes[i] and 0xFF
		}
		for (section in this.getSections()) {
			if (section is EmptyChunkSection) {
				continue
			}
			val s = CompoundTag(null)
			s.putByte("Y", section.y)
			s.putByteArray("Blocks", section.idArray)
			s.putByteArray("Data", section.dataArray)
			s.putByteArray("BlockLight", section.lightArray)
			s.putByteArray("SkyLight", section.skyLightArray)
			nbt.getList("Sections", CompoundTag::class.java).add(s)
		}
		val entities = ArrayList<CompoundTag?>()
		for (entity in this.getEntities().values) {
			if (entity !is Player && !entity.closed) {
				entity.saveNBT()
				entities.add(entity.namedTag)
			}
		}
		val entityListTag = ListTag<CompoundTag?>("Entities")
		entityListTag.setAll(entities)
		nbt.putList(entityListTag)
		val tiles = ArrayList<CompoundTag>()
		for (blockEntity in this.getBlockEntities().values) {
			blockEntity.saveNBT()
			tiles.add(blockEntity.namedTag)
		}
		val tileListTag = ListTag<CompoundTag>("TileEntities")
		tileListTag.setAll(tiles)
		nbt.putList(tileListTag)
		val entries: Set<BlockUpdateEntry> = this.provider!!.level!!.getPendingBlockUpdates(this)
		if (entries != null) {
			val tileTickTag = ListTag<CompoundTag>("TileTicks")
			val totalTime: Long = this.provider!!.level!!.getCurrentTick()
			for (entry in entries) {
				val entryNBT = CompoundTag()
						.putString("i", entry.block.saveId)
						.putInt("x", entry.pos.floorX)
						.putInt("y", entry.pos.floorY)
						.putInt("z", entry.pos.floorZ)
						.putInt("t", (entry.delay - totalTime).toInt())
						.putInt("p", entry.priority)
				tileTickTag.add(entryNBT)
			}
			nbt.putList(tileTickTag)
		}
		val extraData = BinaryStream()
		val extraDataArray: Map<Int, Int> = this.getBlockExtraDataArray()
		extraData.putInt(extraDataArray.size)
		for (key in extraDataArray.keys) {
			extraData.putInt(key)
			extraData.putShort(extraDataArray[key]!!)
		}
		nbt.putByteArray("ExtraData", extraData.buffer)
		val chunk = CompoundTag("")
		chunk.putCompound("Level", nbt)
		return try {
			NBTIO.write(chunk, ByteOrder.BIG_ENDIAN)
		} catch (e: IOException) {
			throw RuntimeException(e)
		}
	}

	override fun toBinary(): ByteArray? {
		val nbt = nBT.copy()
		nbt.remove("BiomeColors")
		nbt.putInt("xPos", this.getX())
		nbt.putInt("zPos", this.getZ())
		val sectionList = ListTag<CompoundTag>("Sections")
		for (section in this.getSections()) {
			if (section is EmptyChunkSection) {
				continue
			}
			val s = CompoundTag(null)
			s.putByte("Y", section.y)
			s.putByteArray("Blocks", section.idArray)
			s.putByteArray("Data", section.dataArray)
			s.putByteArray("BlockLight", section.lightArray)
			s.putByteArray("SkyLight", section.skyLightArray)
			sectionList.add(s)
		}
		nbt.putList(sectionList)
		nbt.putByteArray("Biomes", this.getBiomeIdArray())
		val heightInts = IntArray(256)
		val heightBytes: ByteArray = this.getHeightMapArray()
		for (i in heightInts.indices) {
			heightInts[i] = heightBytes[i] and 0xFF
		}
		nbt.putIntArray("HeightMap", heightInts)
		val entities = ArrayList<CompoundTag?>()
		for (entity in this.getEntities().values) {
			if (entity !is Player && !entity.closed) {
				entity.saveNBT()
				entities.add(entity.namedTag)
			}
		}
		val entityListTag = ListTag<CompoundTag?>("Entities")
		entityListTag.setAll(entities)
		nbt.putList(entityListTag)
		val tiles = ArrayList<CompoundTag>()
		for (blockEntity in this.getBlockEntities().values) {
			blockEntity.saveNBT()
			tiles.add(blockEntity.namedTag)
		}
		val tileListTag = ListTag<CompoundTag>("TileEntities")
		tileListTag.setAll(tiles)
		nbt.putList(tileListTag)
		val entries: Set<BlockUpdateEntry> = this.provider!!.level!!.getPendingBlockUpdates(this)
		if (entries != null) {
			val tileTickTag = ListTag<CompoundTag>("TileTicks")
			val totalTime: Long = this.provider!!.level!!.getCurrentTick()
			for (entry in entries) {
				val entryNBT = CompoundTag()
						.putString("i", entry.block.saveId)
						.putInt("x", entry.pos.floorX)
						.putInt("y", entry.pos.floorY)
						.putInt("z", entry.pos.floorZ)
						.putInt("t", (entry.delay - totalTime).toInt())
						.putInt("p", entry.priority)
				tileTickTag.add(entryNBT)
			}
			nbt.putList(tileTickTag)
		}
		val extraData = BinaryStream()
		val extraDataArray: Map<Int, Int> = this.getBlockExtraDataArray()
		extraData.putInt(extraDataArray.size)
		for (key in extraDataArray.keys) {
			extraData.putInt(key)
			extraData.putShort(extraDataArray[key]!!)
		}
		nbt.putByteArray("ExtraData", extraData.buffer)
		val chunk = CompoundTag("")
		chunk.putCompound("Level", nbt)
		return try {
			Zlib.deflate(NBTIO.write(chunk, ByteOrder.BIG_ENDIAN), BaseRegionLoader.Companion.COMPRESSION_LEVEL)
		} catch (e: Exception) {
			throw RuntimeException(e)
		}
	}

	override fun getBlockSkyLight(x: Int, y: Int, z: Int): Int {
		val section: cn.nukkit.level.format.ChunkSection = this.sections!!.get(y shr 4)!!
		return if (section is ChunkSection) {
			val anvilSection = section
			if (anvilSection.skyLight != null) {
				section.getBlockSkyLight(x, y and 0x0f, z)
			} else if (!anvilSection.hasSkyLight) {
				0
			} else {
				val height = getHighestBlockAt(x, z)
				if (height < y) {
					15
				} else if (height == y) {
					if (Block.transparent!![getBlockId(x, y, z)]) 15 else 0
				} else {
					section.getBlockSkyLight(x, y and 0x0f, z)
				}
			}
		} else {
			section.getBlockSkyLight(x, y and 0x0f, z)
		}
	}

	override fun getBlockLight(x: Int, y: Int, z: Int): Int {
		val section: cn.nukkit.level.format.ChunkSection = this.sections!!.get(y shr 4)!!
		return if (section is ChunkSection) {
			val anvilSection = section
			if (anvilSection.blockLight != null) {
				section.getBlockLight(x, y and 0x0f, z)
			} else if (!anvilSection.hasBlockLight) {
				0
			} else {
				section.getBlockLight(x, y and 0x0f, z)
			}
		} else {
			section.getBlockLight(x, y and 0x0f, z)
		}
	}

	override fun compress(): Boolean {
		super.compress()
		var result = false
		for (section in getSections()) {
			if (section is ChunkSection) {
				val anvilSection = section as ChunkSection
				if (!anvilSection.isEmpty()) {
					result = result or anvilSection.compress()
				}
			}
		}
		return result
	}

	companion object {
		@JvmOverloads
		fun fromBinary(data: ByteArray?, provider: LevelProvider? = null): Chunk? {
			return try {
				val chunk = NBTIO.read(ByteArrayInputStream(Zlib.inflate(data)), ByteOrder.BIG_ENDIAN)
				if (!chunk.contains("Level") || chunk["Level"] !is CompoundTag) {
					null
				} else Chunk(provider, chunk.getCompound("Level"))
			} catch (e: Exception) {
				Server.instance!!.logger.logException(e)
				null
			}
		}

		@JvmOverloads
		fun fromFastBinary(data: ByteArray?, provider: LevelProvider? = null): Chunk? {
			return try {
				val chunk = NBTIO.read(DataInputStream(ByteArrayInputStream(data)), ByteOrder.BIG_ENDIAN)
				if (!chunk.contains("Level") || chunk["Level"] !is CompoundTag) {
					null
				} else Chunk(provider, chunk.getCompound("Level"))
			} catch (e: Exception) {
				null
			}
		}

		fun getEmptyChunk(chunkX: Int, chunkZ: Int): Chunk? {
			return getEmptyChunk(chunkX, chunkZ, null)
		}

		fun getEmptyChunk(chunkX: Int, chunkZ: Int, provider: LevelProvider?): Chunk? {
			return try {
				val chunk: Chunk
				chunk = if (provider != null) {
					Chunk(provider, null)
				} else {
					Chunk(Anvil::class.java, null)
				}
				chunk.setPosition(chunkX, chunkZ)
				chunk.heightMap = ByteArray(256)
				chunk.inhabitedTime = 0
				chunk.terrainGenerated = false
				chunk.terrainPopulated = false
				//            chunk.lightPopulated = false;
				chunk
			} catch (e: Exception) {
				null
			}
		}
	}

	init {
		this.provider = level
		if (level != null) {
			providerClass = level.javaClass
		}
		if (nbt == null) {
			biomes = ByteArray(16 * 16)
			this.sections = arrayOfNulls<cn.nukkit.level.format.ChunkSection>(16)
			if (16 >= 0) System.arraycopy(EmptyChunkSection.Companion.EMPTY, 0, this.sections, 0, 16)
			return
		}
		this.sections = arrayOfNulls<cn.nukkit.level.format.ChunkSection>(16)
		for (section in nbt!!.getList("Sections").all) {
			if (section is CompoundTag) {
				val y = section.getByte("Y")
				if (y < 16) {
					sections.get(y) = ChunkSection(section)
				}
			}
		}
		for (y in 0..15) {
			if (sections.get(y) == null) {
				sections.get(y) = EmptyChunkSection.Companion.EMPTY.get(y)
			}
		}
		val extraData: MutableMap<Int?, Int?> = HashMap()
		val extra = nbt["ExtraData"]
		if (extra is ByteArrayTag) {
			val stream = BinaryStream(extra.data)
			for (i in 0 until stream.int) {
				val key = stream.int
				extraData[key] = stream.short
			}
		}
		setPosition(nbt.getInt("xPos"), nbt.getInt("zPos"))
		if (sections.size > cn.nukkit.level.format.Chunk.SECTION_COUNT) {
			throw ChunkException("Invalid amount of chunks")
		}
		if (nbt.contains("BiomeColors")) {
			biomes = ByteArray(16 * 16)
			val biomeColors = nbt.getIntArray("BiomeColors")
			if (biomeColors != null && biomeColors.size == 256) {
				val palette = BiomePalette(biomeColors)
				for (x in 0..15) {
					for (z in 0..15) {
						biomes[x shl 4 or z] = (palette[x, z] shr 24).toByte()
					}
				}
			}
		} else {
			biomes = Arrays.copyOf(nbt.getByteArray("Biomes"), 256)
		}
		val heightMap = nbt.getIntArray("HeightMap")
		this.heightMap = ByteArray(256)
		if (heightMap.size != 256) {
			Arrays.fill(this.heightMap, 255.toByte())
		} else {
			for (i in heightMap.indices) {
				this.heightMap[i] = heightMap[i].toByte()
			}
		}
		if (!extraData.isEmpty()) this.extraData = extraData
		NBTentities = nbt.getList("Entities", CompoundTag::class.java).all
		NBTtiles = nbt.getList("TileEntities", CompoundTag::class.java).all
		if (NBTentities.isEmpty()) NBTentities = null
		if (NBTtiles.isEmpty()) NBTtiles = null
		val updateEntries = nbt.getList("TileTicks", CompoundTag::class.java)
		if (updateEntries != null && updateEntries.size() > 0) {
			for (entryNBT in updateEntries.all) {
				var block: Block? = null
				try {
					val tag = entryNBT["i"]
					if (tag is StringTag) {
						val name = tag.data
						val clazz = Class.forName("cn.nukkit.block.$name") as Class<out Block>
						val constructor: Constructor<*> = clazz.getDeclaredConstructor()
						constructor.isAccessible = true
						block = constructor.newInstance() as Block
					}
				} catch (e: Throwable) {
					continue
				}
				if (block == null) {
					continue
				}
				block.x = entryNBT.getInt("x")
				block.y = entryNBT.getInt("y")
				block.z = entryNBT.getInt("z")
				this.provider!!.level!!.scheduleUpdate(block, block, entryNBT.getInt("t"), entryNBT.getInt("p"), false)
			}
		}
		inhabitedTime = nbt.getLong("InhabitedTime")
		terrainPopulated = nbt.getBoolean("TerrainPopulated")
		terrainGenerated = nbt.getBoolean("TerrainGenerated")
	}
}