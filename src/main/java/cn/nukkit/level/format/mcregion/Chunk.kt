package cn.nukkit.level.format.mcregion

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.level.format.LevelProvider
import cn.nukkit.level.format.anvil.palette.BiomePalette
import cn.nukkit.level.format.generic.BaseFullChunk
import cn.nukkit.level.format.generic.BaseRegionLoader
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.ByteArrayTag
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.IntArrayTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.utils.Binary
import cn.nukkit.utils.BinaryStream
import cn.nukkit.utils.Zlib
import java.io.ByteArrayInputStream
import java.nio.ByteOrder
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class Chunk @JvmOverloads constructor(level: LevelProvider?, nbt: CompoundTag? = null) : BaseFullChunk() {
	val nBT: CompoundTag?

	constructor(providerClass: Class<out LevelProvider?>?) : this(null as LevelProvider?, null) {
		this.providerClass = providerClass
	}

	constructor(providerClass: Class<out LevelProvider?>?, nbt: CompoundTag?) : this(null as LevelProvider?, nbt) {
		this.providerClass = providerClass
	}

	override fun getBlockId(x: Int, y: Int, z: Int): Int {
		return blocks[x shl 11 or (z shl 7) or y] and 0xff
	}

	override fun setBlockId(x: Int, y: Int, z: Int, id: Int) {
		blocks[x shl 11 or (z shl 7) or y] = id.toByte()
		setChanged()
	}

	override fun getBlockData(x: Int, y: Int, z: Int): Int {
		val b: Int = data[x shl 10 or (z shl 6) or (y shr 1)] and 0xff
		return if (y and 1 == 0) {
			b and 0x0f
		} else {
			b shr 4
		}
	}

	override fun setBlockData(x: Int, y: Int, z: Int, data: Int) {
		val i = x shl 10 or (z shl 6) or (y shr 1)
		val old: Int = this.data[i] and 0xff
		if (y and 1 == 0) {
			this.data[i] = (old and 0xf0 or (data and 0x0f)).toByte()
		} else {
			this.data[i] = (data and 0x0f shl 4 or (old and 0x0f)).toByte()
		}
		setChanged()
	}

	override fun getFullBlock(x: Int, y: Int, z: Int): Int {
		val i = x shl 11 or (z shl 7) or y
		val block: Int = blocks[i] and 0xff
		val data: Int = data[i shr 1] and 0xff
		return if (y and 1 == 0) {
			block shl 4 or (data and 0x0f)
		} else {
			block shl 4 or (data shr 4)
		}
	}

	override fun setBlock(x: Int, y: Int, z: Int, blockId: Int): Boolean {
		return setBlock(x, y, z, blockId, 0)
	}

	override fun setBlock(x: Int, y: Int, z: Int, blockId: Int, meta: Int): Boolean {
		var i = x shl 11 or (z shl 7) or y
		var changed = false
		val id = blockId.toByte()
		if (blocks[i] != id) {
			blocks[i] = id
			changed = true
		}
		if (Block.hasMeta!![blockId]) {
			i = i shr 1
			val old: Int = data[i] and 0xff
			if (y and 1 == 0) {
				data[i] = (old and 0xf0 or (meta and 0x0f)).toByte()
				if (old and 0x0f != meta) {
					changed = true
				}
			} else {
				data[i] = (meta and 0x0f shl 4 or (old and 0x0f)).toByte()
				if (meta != old shr 4) {
					changed = true
				}
			}
		}
		if (changed) {
			setChanged()
		}
		return changed
	}

	override fun getAndSetBlock(x: Int, y: Int, z: Int, block: Block?): Block? {
		var i = x shl 11 or (z shl 7) or y
		var changed = false
		val id = block!!.id.toByte()
		val previousId = blocks[i]
		if (previousId != id) {
			blocks[i] = id
			changed = true
		}
		val previousData: Int
		i = i shr 1
		val old: Int = data[i] and 0xff
		if (y and 1 == 0) {
			previousData = old and 0x0f
			if (Block.hasMeta!![block.id]) {
				data[i] = (old and 0xf0 or (block.damage and 0x0f)).toByte()
				if (block.damage != previousData) {
					changed = true
				}
			}
		} else {
			previousData = old shr 4
			if (Block.hasMeta!![block.id]) {
				data[i] = (block.damage and 0x0f shl 4 or (old and 0x0f)).toByte()
				if (block.damage != previousData) {
					changed = true
				}
			}
		}
		if (changed) {
			setChanged()
		}
		return get(previousId.toInt(), previousData)
	}

	override fun getBlockSkyLight(x: Int, y: Int, z: Int): Int {
		val sl: Int = skyLight[x shl 10 or (z shl 6) or (y shr 1)] and 0xff
		return if (y and 1 == 0) {
			sl and 0x0f
		} else {
			sl shr 4
		}
	}

	override fun setBlockSkyLight(x: Int, y: Int, z: Int, level: Int) {
		val i = x shl 10 or (z shl 6) or (y shr 1)
		val old: Int = skyLight[i] and 0xff
		if (y and 1 == 0) {
			skyLight[i] = (old and 0xf0 or (level and 0x0f)).toByte()
		} else {
			skyLight[i] = (level and 0x0f shl 4 or (old and 0x0f)).toByte()
		}
		setChanged()
	}

	override fun getBlockLight(x: Int, y: Int, z: Int): Int {
		val b: Int = blockLight[x shl 10 or (z shl 6) or (y shr 1)] and 0xff
		return if (y and 1 == 0) {
			b and 0x0f
		} else {
			b shr 4
		}
	}

	override fun setBlockLight(x: Int, y: Int, z: Int, level: Int) {
		val i = x shl 10 or (z shl 6) or (y shr 1)
		val old: Int = blockLight[i] and 0xff
		if (y and 1 == 0) {
			blockLight[i] = (old and 0xf0 or (level and 0x0f)).toByte()
		} else {
			blockLight[i] = (level and 0x0f shl 4 or (old and 0x0f)).toByte()
		}
		setChanged()
	}

	override var isLightPopulated: Boolean
		get() = nBT!!.getBoolean("LightPopulated")
		set(value) {
			nBT!!.putBoolean("LightPopulated", value)
			setChanged()
		}

	override fun setLightPopulated() {
		this.lightPopulated = true
	}

	override var isPopulated: Boolean
		get() = nBT!!.contains("TerrainPopulated") && nBT.getBoolean("TerrainPopulated")
		set(value) {
			nBT!!.putBoolean("TerrainPopulated", value)
			setChanged()
		}

	override fun setPopulated() {
		this.populated = true
	}

	override var isGenerated: Boolean
		get() {
			if (nBT!!.contains("TerrainGenerated")) {
				return nBT.getBoolean("TerrainGenerated")
			} else if (nBT.contains("TerrainPopulated")) {
				return nBT.getBoolean("TerrainPopulated")
			}
			return false
		}
		set(value) {
			nBT!!.putBoolean("TerrainGenerated", value)
			setChanged()
		}

	override fun setGenerated() {
		this.generated = true
	}

	override fun toFastBinary(): ByteArray? {
		val stream = BinaryStream(ByteArray(65536))
		stream.put(Binary.writeInt(this.getX()))
		stream.put(Binary.writeInt(this.getZ()))
		stream.put(this.getBlockIdArray())
		stream.put(this.getBlockDataArray())
		stream.put(this.getBlockSkyLightArray())
		stream.put(this.getBlockLightArray())
		stream.put(this.getHeightMapArray())
		stream.put(this.getBiomeIdArray())
		stream.putByte(((if (isLightPopulated) 1 shl 2 else 0) + (if (isPopulated) 1 shl 2 else 0) + if (isGenerated) 1 else 0).toByte())
		return stream.buffer
	}

	override fun toBinary(): ByteArray? {
		val nbt = nBT!!.copy()
		nbt.remove("BiomeColors")
		nbt.putInt("xPos", this.getX())
		nbt.putInt("zPos", this.getZ())
		if (isGenerated) {
			nbt.putByteArray("Blocks", this.getBlockIdArray())
			nbt.putByteArray("Data", this.getBlockDataArray())
			nbt.putByteArray("SkyLight", this.getBlockSkyLightArray())
			nbt.putByteArray("BlockLight", this.getBlockLightArray())
			nbt.putByteArray("Biomes", this.getBiomeIdArray())
			val heightInts = IntArray(256)
			val heightBytes: ByteArray = this.getHeightMapArray()
			for (i in heightInts.indices) {
				heightInts[i] = heightBytes[i] and 0xFF
			}
			nbt.putIntArray("HeightMap", heightInts)
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

	companion object {
		@JvmOverloads
		fun fromBinary(data: ByteArray?, provider: LevelProvider? = null): Chunk? {
			return try {
				val chunk = NBTIO.read(ByteArrayInputStream(Zlib.inflate(data)), ByteOrder.BIG_ENDIAN)
				if (!chunk.contains("Level") || chunk["Level"] !is CompoundTag) {
					null
				} else Chunk(provider ?: McRegion::class.java.newInstance(), chunk.getCompound("Level"))
			} catch (e: Exception) {
				null
			}
		}

		@JvmOverloads
		fun fromFastBinary(data: ByteArray, provider: LevelProvider? = null): Chunk {
			return try {
				var offset = 0
				val chunk = Chunk(provider ?: McRegion::class.java.newInstance(), null)
				chunk.provider = provider
				val chunkX = Binary.readInt(Arrays.copyOfRange(data, offset, offset + 3))
				offset += 4
				val chunkZ = Binary.readInt(Arrays.copyOfRange(data, offset, offset + 3))
				chunk.setPosition(chunkX, chunkZ)
				offset += 4
				chunk.blocks = Arrays.copyOfRange(data, offset, offset + 32767)
				offset += 32768
				chunk.data = Arrays.copyOfRange(data, offset, offset + 16383)
				offset += 16384
				chunk.skyLight = Arrays.copyOfRange(data, offset, offset + 16383)
				offset += 16384
				chunk.blockLight = Arrays.copyOfRange(data, offset, offset + 16383)
				offset += 16384
				chunk.heightMap = Arrays.copyOfRange(data, offset, offset + 256)
				offset += 256
				chunk.biomes = Arrays.copyOfRange(data, offset, offset + 256)
				offset += 256
				val flags = data[offset++]
				chunk.nBT!!.putByte("TerrainGenerated", flags and 1)
				chunk.nBT.putByte("TerrainPopulated", flags shr 1 and 1)
				chunk.nBT.putByte("LightPopulated", flags shr 2 and 1)
				chunk
			} catch (e: Exception) {
				throw RuntimeException(e)
			}
		}

		fun getEmptyChunk(chunkX: Int, chunkZ: Int): Chunk {
			return getEmptyChunk(chunkX, chunkZ, null)
		}

		fun getEmptyChunk(chunkX: Int, chunkZ: Int, provider: LevelProvider?): Chunk {
			return try {
				val chunk: Chunk
				chunk = if (provider != null) {
					Chunk(provider, null)
				} else {
					Chunk(McRegion::class.java, null)
				}
				chunk.setPosition(chunkX, chunkZ)
				chunk.data = ByteArray(16384)
				chunk.blocks = ByteArray(32768)
				val skyLight = ByteArray(16384)
				Arrays.fill(skyLight, 0xff.toByte())
				chunk.skyLight = skyLight
				chunk.blockLight = chunk.data
				chunk.heightMap = ByteArray(256)
				chunk.biomes = ByteArray(16 * 16)
				chunk.nBT!!.putByte("V", 1)
				chunk.nBT.putLong("InhabitedTime", 0)
				chunk.nBT.putBoolean("TerrainGenerated", false)
				chunk.nBT.putBoolean("TerrainPopulated", false)
				chunk.nBT.putBoolean("LightPopulated", false)
				chunk
			} catch (e: Exception) {
				throw RuntimeException(e)
			}
		}
	}

	init {
		this.provider = level
		if (level != null) {
			providerClass = level.javaClass
		}
		if (nbt == null) {
			return
		}
		nBT = nbt
		if (!(nBT!!.contains("Entities") && nBT["Entities"] is ListTag<*>)) {
			nBT.putList(ListTag<CompoundTag>("Entities"))
		}
		if (!(nBT.contains("TileEntities") && nBT["TileEntities"] is ListTag<*>)) {
			nBT.putList(ListTag<CompoundTag>("TileEntities"))
		}
		if (!(nBT.contains("TileTicks") && nBT["TileTicks"] is ListTag<*>)) {
			nBT.putList(ListTag<CompoundTag>("TileTicks"))
		}
		if (!(nBT.contains("Biomes") && nBT["Biomes"] is ByteArrayTag)) {
			nBT.putByteArray("Biomes", ByteArray(256))
		}
		if (!(nBT.contains("HeightMap") && nBT["HeightMap"] is IntArrayTag)) {
			nBT.putIntArray("HeightMap", IntArray(256))
		}
		if (!nBT.contains("Blocks")) {
			nBT.putByteArray("Blocks", ByteArray(32768))
		}
		if (!nBT.contains("Data")) {
			nBT.putByteArray("Data", ByteArray(16384))
			nBT.putByteArray("SkyLight", ByteArray(16384))
			nBT.putByteArray("BlockLight", ByteArray(16384))
		}
		val extraData: MutableMap<Int?, Int?> = HashMap()
		if (!nBT.contains("ExtraData") || nBT["ExtraData"] !is ByteArrayTag) {
			nBT.putByteArray("ExtraData", Binary.writeInt(0))
		} else {
			val stream = BinaryStream(nBT.getByteArray("ExtraData"))
			for (i in 0 until stream.int) {
				val key = stream.int
				extraData[key] = stream.short
			}
		}
		setPosition(nBT.getInt("xPos"), nBT.getInt("zPos"))
		blocks = nBT.getByteArray("Blocks")
		data = nBT.getByteArray("Data")
		skyLight = nBT.getByteArray("SkyLight")
		blockLight = nBT.getByteArray("BlockLight")
		if (nBT.contains("BiomeColors")) {
			biomes = ByteArray(16 * 16)
			val biomeColors = nBT.getIntArray("BiomeColors")
			if (biomeColors.size == 256) {
				val palette = BiomePalette(biomeColors)
				for (x in 0..15) {
					for (z in 0..15) {
						biomes[x shl 4 or z] = (palette[x, z] shr 24).toByte()
					}
				}
			}
		} else {
			biomes = nBT.getByteArray("Biomes")
		}
		val heightMap = nBT.getIntArray("HeightMap")
		this.heightMap = ByteArray(256)
		if (heightMap.size != 256) {
			Arrays.fill(this.heightMap, 255.toByte())
		} else {
			for (i in heightMap.indices) {
				this.heightMap[i] = heightMap[i].toByte()
			}
		}
		if (!extraData.isEmpty()) this.extraData = extraData
		NBTentities = (nBT.getList("Entities") as ListTag<CompoundTag?>).all
		NBTtiles = (nBT.getList("TileEntities") as ListTag<CompoundTag?>).all
		nBT.remove("Blocks")
		nBT.remove("Data")
		nBT.remove("SkyLight")
		nBT.remove("BlockLight")
		nBT.remove("BiomeColors")
		nBT.remove("HeightMap")
		nBT.remove("Biomes")
	}
}