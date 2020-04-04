package cn.nukkit.level.format.generic

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntity.Companion.createBlockEntity
import cn.nukkit.entity.Entity
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.Level
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.format.LevelProvider
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.NumberTag
import cn.nukkit.network.protocol.BatchPacket
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import java.io.IOException
import java.util.*
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.emptyMap
import kotlin.collections.set

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class BaseFullChunk : FullChunk, ChunkManager {
	protected override var entities: MutableMap<Long, Entity?>? = null
	protected var tiles: MutableMap<Long, BlockEntity?>? = null
	protected var tileList: MutableMap<Int, BlockEntity?>? = null

	/**
	 * encoded as:
	 *
	 * (x &lt;&lt; 4) | z
	 */
	override var biomeIdArray: ByteArray?
	override var blockIdArray: ByteArray?
		protected set
	override var blockDataArray: ByteArray?
		protected set
	override var blockSkyLightArray: ByteArray?
		protected set
	override var blockLightArray: ByteArray?
		protected set
	override var heightMapArray: ByteArray?
	var NBTtiles: MutableList<CompoundTag?>? = null
	var NBTentities: MutableList<CompoundTag?>? = null
	protected var extraData: MutableMap<Int, Int>? = null
	override var provider: LevelProvider? = null
	protected var providerClass: Class<out LevelProvider>? = null
	private override var x = 0
	private override var z = 0
	override var index: Long = 0
		private set
	var changes: Long = 0
		protected set
	protected var isInit = false
	protected var chunkPacket: BatchPacket? = null
	public override fun clone(): BaseFullChunk {
		val chunk: BaseFullChunk
		chunk = try {
			super.clone() as BaseFullChunk
		} catch (e: CloneNotSupportedException) {
			return null
		}
		if (biomeIdArray != null) {
			chunk.biomeIdArray = biomeIdArray!!.clone()
		}
		if (blockIdArray != null) {
			chunk.blockIdArray = blockIdArray!!.clone()
		}
		if (blockDataArray != null) {
			chunk.blockDataArray = blockDataArray!!.clone()
		}
		if (blockSkyLightArray != null) {
			chunk.blockSkyLightArray = blockSkyLightArray!!.clone()
		}
		if (blockLightArray != null) {
			chunk.blockLightArray = blockLightArray!!.clone()
		}
		if (heightMapArray != null) {
			chunk.heightMapArray = heightMapArray!!.clone()
		}
		return chunk
	}

	fun setChunkPacket(packet: BatchPacket?) {
		packet?.trim()
		chunkPacket = packet
	}

	fun getChunkPacket(): BatchPacket? {
		val pk = chunkPacket
		pk?.trim()
		return chunkPacket
	}

	override fun initChunk() {
		if (provider != null && !isInit) {
			var changed = false
			if (NBTentities != null) {
				provider!!.level!!.timings.syncChunkLoadEntitiesTimer.startTiming()
				for (nbt in NBTentities!!) {
					if (!nbt!!.contains("id")) {
						this.setChanged()
						continue
					}
					val pos = nbt.getList("Pos")
					if ((pos[0] as NumberTag<*>).data.intValue() shr 4 != getX() || (pos[2] as NumberTag<*>).data.intValue() shr 4 != getZ()) {
						changed = true
						continue
					}
					val entity = Entity.createEntity(nbt.getString("id"), this, nbt)
					if (entity != null) {
						changed = true
					}
				}
				provider!!.level!!.timings.syncChunkLoadEntitiesTimer.stopTiming()
				NBTentities = null
			}
			if (NBTtiles != null) {
				provider!!.level!!.timings.syncChunkLoadBlockEntitiesTimer.startTiming()
				for (nbt in NBTtiles!!) {
					if (nbt != null) {
						if (!nbt.contains("id")) {
							changed = true
							continue
						}
						if (nbt.getInt("x") shr 4 != getX() || nbt.getInt("z") shr 4 != getZ()) {
							changed = true
							continue
						}
						val blockEntity = createBlockEntity(nbt.getString("id"), this, nbt)
						if (blockEntity == null) {
							changed = true
						}
					}
				}
				provider!!.level!!.timings.syncChunkLoadBlockEntitiesTimer.stopTiming()
				NBTtiles = null
			}
			this.setChanged(changed)
			isInit = true
		}
	}

	override fun getX(): Int {
		return x
	}

	override fun getZ(): Int {
		return z
	}

	override fun setPosition(x: Int, z: Int) {
		this.x = x
		this.z = z
		index = Level.chunkHash(x, z)
	}

	override fun setX(x: Int) {
		this.x = x
		index = Level.chunkHash(x, getZ())
	}

	override fun setZ(z: Int) {
		this.z = z
		index = Level.chunkHash(getX(), z)
	}

	override fun getBiomeId(x: Int, z: Int): Int {
		return biomeIdArray!![x shl 4 or z] and 0xFF
	}

	override fun setBiomeId(x: Int, z: Int, biomeId: Byte) {
		this.setChanged()
		biomeIdArray!![x shl 4 or z] = biomeId
	}

	override fun getHeightMap(x: Int, z: Int): Int {
		return heightMapArray!![z shl 4 or x] and 0xFF
	}

	override fun setHeightMap(x: Int, z: Int, value: Int) {
		heightMapArray!![z shl 4 or x] = value.toByte()
	}

	override fun recalculateHeightMap() {
		for (z in 0..15) {
			for (x in 0..15) {
				setHeightMap(x, z, this.getHighestBlockAt(x, z, false))
			}
		}
	}

	override fun getBlockExtraData(x: Int, y: Int, z: Int): Int {
		val index = Level.chunkBlockHash(x, y, z)
		return if (extraData != null && extraData!!.containsKey(index)) {
			extraData!![index]!!
		} else 0
	}

	override fun setBlockExtraData(x: Int, y: Int, z: Int, data: Int) {
		if (data == 0) {
			if (extraData != null) {
				extraData!!.remove(Level.chunkBlockHash(x, y, z))
			}
		} else {
			if (extraData == null) extraData = Int2ObjectOpenHashMap()
			extraData!![Level.chunkBlockHash(x, y, z)] = data
		}
		this.setChanged(true)
	}

	override fun populateSkyLight() {
		for (z in 0..15) {
			for (x in 0..15) {
				val top = getHeightMap(x, z)
				for (y in 255 downTo top + 1) {
					setBlockSkyLight(x, y, z, 15)
				}
				for (y in top downTo 0) {
					if (Block.solid!![getBlockId(x, y, z)]) {
						break
					}
					setBlockSkyLight(x, y, z, 15)
				}
				setHeightMap(x, z, this.getHighestBlockAt(x, z, false))
			}
		}
	}

	override fun getHighestBlockAt(x: Int, z: Int): Int {
		return this.getHighestBlockAt(x, z, true)
	}

	override fun getHighestBlockAt(x: Int, z: Int, cache: Boolean): Int {
		if (cache) {
			val h = getHeightMap(x, z)
			if (h != 0 && h != 255) {
				return h
			}
		}
		for (y in 255 downTo 0) {
			if (getBlockId(x, y, z) != 0x00) {
				setHeightMap(x, z, y)
				return y
			}
		}
		return 0
	}

	override fun addEntity(entity: Entity?) {
		if (entities == null) {
			entities = Long2ObjectOpenHashMap()
		}
		entities!![entity!!.id] = entity
		if (entity !is Player && isInit) {
			this.setChanged()
		}
	}

	override fun removeEntity(entity: Entity?) {
		if (entities != null) {
			entities!!.remove(entity!!.id)
			if (entity !is Player && isInit) {
				this.setChanged()
			}
		}
	}

	override fun addBlockEntity(blockEntity: BlockEntity?) {
		if (tiles == null) {
			tiles = Long2ObjectOpenHashMap()
			tileList = Int2ObjectOpenHashMap()
		}
		tiles!![blockEntity!!.id] = blockEntity
		val index = blockEntity.floorZ and 0x0f shl 12 or (blockEntity.floorX and 0x0f shl 8) or (blockEntity.floorY and 0xff)
		if (tileList!!.containsKey(index) && !tileList!![index]!!.equals(blockEntity)) {
			val entity = tileList!![index]
			tiles!!.remove(entity!!.id)
			entity.close()
		}
		tileList!![index] = blockEntity
		if (isInit) {
			this.setChanged()
		}
	}

	override fun removeBlockEntity(blockEntity: BlockEntity?) {
		if (tiles != null) {
			tiles!!.remove(blockEntity!!.id)
			val index = blockEntity.floorZ and 0x0f shl 12 or (blockEntity.floorX and 0x0f shl 8) or (blockEntity.floorY and 0xff)
			tileList!!.remove(index)
			if (isInit) {
				this.setChanged()
			}
		}
	}

	override fun getEntities(): Map<Long, Entity> {
		return if (entities == null) emptyMap() else entities
	}

	override val blockEntities: Map<Long, BlockEntity>
		get() = if (tiles == null) emptyMap() else tiles

	override val blockExtraDataArray: Map<Int, Int>
		get() = (if (extraData == null) emptyMap() else extraData!!)

	override fun getTile(x: Int, y: Int, z: Int): BlockEntity? {
		return if (tileList != null) tileList!![z shl 12 or (x shl 8) or y] else null
	}

	override val isLoaded: Boolean
		get() = provider != null && provider!!.isChunkLoaded(getX(), getZ())

	@Throws(IOException::class)
	override fun load(): Boolean {
		return this.load(true)
	}

	@Throws(IOException::class)
	override fun load(generate: Boolean): Boolean {
		return provider != null && provider!!.getChunk(getX(), getZ(), true) != null
	}

	@Throws(Exception::class)
	override fun unload(): Boolean {
		return this.unload(true, true)
	}

	@Throws(Exception::class)
	override fun unload(save: Boolean): Boolean {
		return this.unload(save, true)
	}

	override fun unload(save: Boolean, safe: Boolean): Boolean {
		val provider = provider ?: return true
		if (save && changes != 0L) {
			provider.saveChunk(getX(), getZ())
		}
		if (safe) {
			for (entity in getEntities().values) {
				if (entity is Player) {
					return false
				}
			}
		}
		for (entity in ArrayList(getEntities().values)) {
			if (entity is Player) {
				continue
			}
			entity.close()
		}
		for (blockEntity in ArrayList(blockEntities.values)) {
			blockEntity.close()
		}
		this.provider = null
		return true
	}

	override fun hasChanged(): Boolean {
		return changes != 0L
	}

	override fun setChanged() {
		changes++
		chunkPacket = null
	}

	override fun setChanged(changed: Boolean) {
		if (changed) {
			setChanged()
		} else {
			changes = 0
		}
	}

	override fun toFastBinary(): ByteArray? {
		return toBinary()
	}

	override var isLightPopulated: Boolean
		get() = true
		set(value) {}

	override fun setLightPopulated() {
		this.lightPopulated = true
	}

	override fun getBlockIdAt(x: Int, y: Int, z: Int): Int {
		return if (x shr 4 == getX() && z shr 4 == getZ()) {
			getBlockId(x and 15, y, z and 15)
		} else 0
	}

	override fun setBlockFullIdAt(x: Int, y: Int, z: Int, fullId: Int) {
		if (x shr 4 == getX() && z shr 4 == getZ()) {
			setFullBlockId(x and 15, y, z and 15, fullId)
		}
	}

	override fun setBlockIdAt(x: Int, y: Int, z: Int, id: Int) {
		if (x shr 4 == getX() && z shr 4 == getZ()) {
			setBlockId(x and 15, y, z and 15, id)
		}
	}

	override fun setBlockAt(x: Int, y: Int, z: Int, id: Int, data: Int) {
		if (x shr 4 == getX() && z shr 4 == getZ()) {
			setBlock(x and 15, y, z and 15, id, data)
		}
	}

	override fun getBlockDataAt(x: Int, y: Int, z: Int): Int {
		return if (x shr 4 == getX() && z shr 4 == getZ()) {
			getBlockIdAt(x and 15, y, z and 15)
		} else 0
	}

	override fun setBlockDataAt(x: Int, y: Int, z: Int, data: Int) {
		if (x shr 4 == getX() && z shr 4 == getZ()) {
			setBlockData(x and 15, y, z and 15, data)
		}
	}

	override fun getChunk(chunkX: Int, chunkZ: Int): BaseFullChunk? {
		return if (chunkX == getX() && chunkZ == getZ()) this else null
	}

	override fun setChunk(chunkX: Int, chunkZ: Int) {
		setChunk(chunkX, chunkZ, null)
	}

	override fun setChunk(chunkX: Int, chunkZ: Int, chunk: BaseFullChunk?) {
		throw UnsupportedOperationException()
	}

	override val seed: Long
		get() {
			throw UnsupportedOperationException("Chunk does not have a seed")
		}

	open fun compress(): Boolean {
		val pk = chunkPacket
		if (pk != null) {
			pk.trim()
			return true
		}
		return false
	}
}