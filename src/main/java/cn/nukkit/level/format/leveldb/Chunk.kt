package cn.nukkit.level.format.leveldb

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.level.format.LevelProvider
import cn.nukkit.level.format.anvil.palette.BiomePalette
import cn.nukkit.level.format.generic.BaseFullChunk
import cn.nukkit.level.format.leveldb.key.EntitiesKey
import cn.nukkit.level.format.leveldb.key.ExtraDataKey
import cn.nukkit.level.format.leveldb.key.TilesKey
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.stream.NBTInputStream
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.Tag
import cn.nukkit.utils.Binary
import cn.nukkit.utils.BinaryStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class Chunk @JvmOverloads constructor(level: LevelProvider?, chunkX: Int, chunkZ: Int, terrain: ByteArray?, entityData: List<CompoundTag?>? = null, tileData: List<CompoundTag?>? = null, extraData: Map<Int?, Int?>? = null) : BaseFullChunk() {
	override var isLightPopulated = false
	protected override var isPopulated = false
	protected override var isGenerated = false

	constructor(providerClass: Class<out LevelProvider?>?, chunkX: Int, chunkZ: Int, terrain: ByteArray?) : this(null, chunkX, chunkZ, terrain, null) {
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

	override fun setLightPopulated() {
		this.lightPopulated = true
	}

	override fun isPopulated(): Boolean {
		return isPopulated
	}

	override fun setPopulated() {
		this.setPopulated(true)
	}

	override fun setPopulated(value: Boolean) {
		isPopulated = true
	}

	override fun isGenerated(): Boolean {
		return isGenerated
	}

	override fun setGenerated() {
		this.setGenerated(true)
	}

	override fun setGenerated(value: Boolean) {
		isGenerated = true
	}

	override fun toFastBinary(): ByteArray? {
		return this.toBinary(false)
	}

	override fun toBinary(): ByteArray? {
		return this.toBinary(false)
	}

	fun toBinary(saveExtra: Boolean): ByteArray {
		return try {
			val provider: LevelProvider = this.getProvider()
			if (saveExtra && provider is LevelDB) {
				val entities: MutableList<CompoundTag?> = ArrayList()
				for (entity in this.getEntities().values) {
					if (entity !is Player && !entity.closed) {
						entity.saveNBT()
						entities.add(entity.namedTag)
					}
				}
				val entitiesKey: EntitiesKey = EntitiesKey.Companion.create(this.getX(), this.getZ())
				if (!entities.isEmpty()) {
					provider.database.put(entitiesKey.toArray(), NBTIO.write(entities))
				} else {
					provider.database.delete(entitiesKey.toArray())
				}
				val tiles: MutableList<CompoundTag> = ArrayList()
				for (blockEntity in this.getBlockEntities().values) {
					if (!blockEntity.closed) {
						blockEntity.saveNBT()
						tiles.add(blockEntity.namedTag)
					}
				}
				val tilesKey: TilesKey = TilesKey.Companion.create(this.getX(), this.getZ())
				if (!tiles.isEmpty()) {
					provider.database.put(tilesKey.toArray(), NBTIO.write(tiles))
				} else {
					provider.database.delete(tilesKey.toArray())
				}
				val extraDataKey: ExtraDataKey = ExtraDataKey.Companion.create(this.getX(), this.getZ())
				if (!this.getBlockExtraDataArray().isEmpty()) {
					val extraData = BinaryStream()
					val extraDataArray: Map<Int, Int> = this.getBlockExtraDataArray()
					extraData.putInt(extraDataArray.size)
					for (key in extraDataArray.keys) {
						extraData.putInt(key)
						extraData.putShort(extraDataArray[key]!!)
					}
					provider.database.put(extraDataKey.toArray(), extraData.buffer)
				} else {
					provider.database.delete(extraDataKey.toArray())
				}
			}
			val heightMap: ByteArray = this.getHeightMapArray()
			val biomeColors = ByteArray(biomes.size * 4)
			for (i in biomes.indices) {
				val bytes = Binary.writeInt(biomes[i] shl 24)
				biomeColors[i * 4] = bytes[0]
				biomeColors[i * 4 + 1] = bytes[1]
				biomeColors[i * 4 + 2] = bytes[2]
				biomeColors[i * 4 + 3] = bytes[3]
			}
			Binary.appendBytes(
					Binary.writeLInt(this.getX()),
					Binary.writeLInt(this.getZ()),
					this.getBlockIdArray(),
					this.getBlockDataArray(),
					this.getBlockSkyLightArray(),
					this.getBlockLightArray(),
					heightMap,
					biomeColors, byteArrayOf(((if (isLightPopulated) 0x04 else 0) or (if (isPopulated()) 0x02 else 0) or if (isGenerated()) 0x01 else 0 and 0xff).toByte()))
		} catch (e: Exception) {
			throw RuntimeException(e)
		}
	}

	companion object {
		const val DATA_LENGTH = 16384 * (2 + 1 + 1 + 1) + 256 + 1024

		@JvmOverloads
		fun fromBinary(data: ByteArray, provider: LevelProvider? = null): Chunk? {
			try {
				val chunkX = Binary.readLInt(byteArrayOf(data[0], data[1], data[2], data[3]))
				val chunkZ = Binary.readLInt(byteArrayOf(data[4], data[5], data[6], data[7]))
				val chunkData = Binary.subBytes(data, 8, data.size - 1)
				val flags = data[data.size - 1].toInt()
				val entities: MutableList<CompoundTag?> = ArrayList()
				val tiles: MutableList<CompoundTag?> = ArrayList()
				val extraDataMap: MutableMap<Int?, Int?> = HashMap()
				if (provider is LevelDB) {
					val entityData = provider.database[EntitiesKey.Companion.create(chunkX, chunkZ).toArray()]
					if (entityData != null && entityData.size > 0) {
						NBTInputStream(ByteArrayInputStream(entityData), ByteOrder.LITTLE_ENDIAN).use { nbtInputStream ->
							while (nbtInputStream.available() > 0) {
								val tag = Tag.readNamedTag(nbtInputStream) as? CompoundTag
										?: throw IOException("Root tag must be a named compound tag")
								entities.add(tag)
							}
						}
					}
					val tileData = provider.database[TilesKey.Companion.create(chunkX, chunkZ).toArray()]
					if (tileData != null && tileData.size > 0) {
						NBTInputStream(ByteArrayInputStream(tileData), ByteOrder.LITTLE_ENDIAN).use { nbtInputStream ->
							while (nbtInputStream.available() > 0) {
								val tag = Tag.readNamedTag(nbtInputStream) as? CompoundTag
										?: throw IOException("Root tag must be a named compound tag")
								tiles.add(tag)
							}
						}
					}
					val extraData = provider.database[ExtraDataKey.Companion.create(chunkX, chunkZ).toArray()]
					if (extraData != null && extraData.size > 0) {
						val stream = BinaryStream(tileData)
						val count = stream.int
						for (i in 0 until count) {
							val key = stream.int
							val value = stream.short
							extraDataMap[key] = value
						}
					}

					/*if (!entities.isEmpty() || !blockEntities.isEmpty()) {
                    CompoundTag ct = new CompoundTag();
                    ListTag<CompoundTag> entityList = new ListTag<>("entities");
                    ListTag<CompoundTag> tileList = new ListTag<>("blockEntities");

                    entityList.list = entities;
                    tileList.list = blockEntities;
                    ct.putList(entityList);
                    ct.putList(tileList);
                    NBTIO.write(ct, new File(Nukkit.DATA_PATH + chunkX + "_" + chunkZ + ".dat"));
                }*/
					val chunk = Chunk(provider, chunkX, chunkZ, chunkData, entities, tiles, extraDataMap)
					if (flags and 0x01 > 0) {
						chunk.setGenerated()
					}
					if (flags and 0x02 > 0) {
						chunk.setPopulated()
					}
					if (flags and 0x04 > 0) {
						chunk.setLightPopulated()
					}
					return chunk
				}
			} catch (e: Exception) {
				Server.instance!!.logger.logException(e)
			}
			return null
		}

		@JvmOverloads
		fun fromFastBinary(data: ByteArray, provider: LevelProvider? = null): Chunk? {
			return fromBinary(data, provider)
		}

		fun getEmptyChunk(chunkX: Int, chunkZ: Int): Chunk {
			return getEmptyChunk(chunkX, chunkZ, null)
		}

		fun getEmptyChunk(chunkX: Int, chunkZ: Int, provider: LevelProvider?): Chunk {
			return try {
				val chunk: Chunk
				chunk = if (provider != null) {
					Chunk(provider, chunkX, chunkZ, ByteArray(DATA_LENGTH))
				} else {
					Chunk(LevelDB::class.java, chunkX, chunkZ, ByteArray(DATA_LENGTH))
				}
				val skyLight = ByteArray(16384)
				Arrays.fill(skyLight, 0xff.toByte())
				chunk.skyLight = skyLight
				chunk
			} catch (e: Exception) {
				throw RuntimeException(e)
			}
		}
	}

	init {
		val buffer = ByteBuffer.wrap(terrain).order(ByteOrder.BIG_ENDIAN)
		val blocks = ByteArray(32768)
		buffer[blocks]
		val data = ByteArray(16384)
		buffer[data]
		val skyLight = ByteArray(16384)
		buffer[skyLight]
		val blockLight = ByteArray(16384)
		buffer[blockLight]
		val heightMap = ByteArray(256)
		for (i in 0..255) {
			heightMap[i] = buffer.get()
		}
		val biomeColors = IntArray(256)
		for (i in 0..255) {
			biomeColors[i] = buffer.int
		}
		this.provider = level
		if (level != null) {
			providerClass = level.javaClass
		}
		setPosition(chunkX, chunkZ)
		this.blocks = blocks
		this.data = data
		this.skyLight = skyLight
		this.blockLight = blockLight
		biomes = ByteArray(16 * 16)
		if (biomeColors.size == 256) {
			val palette = BiomePalette(biomeColors)
			for (x in 0..15) {
				for (z in 0..15) {
					biomes[x shl 4 or z] = (palette[x, z] shr 24).toByte()
				}
			}
		}
		if (heightMap.size == 256) {
			this.heightMap = heightMap
		} else {
			val bytes = ByteArray(256)
			Arrays.fill(bytes, 256.toByte())
			this.heightMap = bytes
		}
		NBTentities = entityData
		NBTtiles = tileData
		this.extraData = extraData
	}
}