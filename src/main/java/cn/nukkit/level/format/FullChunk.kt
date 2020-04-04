package cn.nukkit.level.format

import cn.nukkit.block.Block
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.entity.Entity
import cn.nukkit.level.biome.Biome
import java.io.IOException

/**
 * author: MagicDroidX
 * Nukkit Project
 */
interface FullChunk : Cloneable {
	var x: Int
	var z: Int
	fun setPosition(x: Int, z: Int) {
		x = x
		z = z
	}

	val index: Long
	var provider: LevelProvider?
	fun getFullBlock(x: Int, y: Int, z: Int): Int
	fun getAndSetBlock(x: Int, y: Int, z: Int, block: Block?): Block?
	fun setFullBlockId(x: Int, y: Int, z: Int, fullId: Int): Boolean {
		return setBlock(x, y, z, fullId shr 4, fullId and 0xF)
	}

	fun setBlock(x: Int, y: Int, z: Int, blockId: Int): Boolean
	fun setBlock(x: Int, y: Int, z: Int, blockId: Int, meta: Int): Boolean
	fun getBlockId(x: Int, y: Int, z: Int): Int
	fun setBlockId(x: Int, y: Int, z: Int, id: Int)
	fun getBlockData(x: Int, y: Int, z: Int): Int
	fun setBlockData(x: Int, y: Int, z: Int, data: Int)
	fun getBlockExtraData(x: Int, y: Int, z: Int): Int
	fun setBlockExtraData(x: Int, y: Int, z: Int, data: Int)
	fun getBlockSkyLight(x: Int, y: Int, z: Int): Int
	fun setBlockSkyLight(x: Int, y: Int, z: Int, level: Int)
	fun getBlockLight(x: Int, y: Int, z: Int): Int
	fun setBlockLight(x: Int, y: Int, z: Int, level: Int)
	fun getHighestBlockAt(x: Int, z: Int): Int
	fun getHighestBlockAt(x: Int, z: Int, cache: Boolean): Int
	fun getHeightMap(x: Int, z: Int): Int
	fun setHeightMap(x: Int, z: Int, value: Int)
	fun recalculateHeightMap()
	fun populateSkyLight()
	fun getBiomeId(x: Int, z: Int): Int
	fun setBiomeId(x: Int, z: Int, biomeId: Byte)
	fun setBiomeId(x: Int, z: Int, biomeId: Int) {
		setBiomeId(x, z, biomeId.toByte())
	}

	fun setBiome(x: Int, z: Int, biome: Biome) {
		setBiomeId(x, z, biome.id.toByte())
	}

	var isLightPopulated: Boolean
	fun setLightPopulated()
	var isPopulated: Boolean
	fun setPopulated()
	var isGenerated: Boolean
	fun setGenerated()
	fun addEntity(entity: Entity?)
	fun removeEntity(entity: Entity?)
	fun addBlockEntity(blockEntity: BlockEntity?)
	fun removeBlockEntity(blockEntity: BlockEntity?)
	val entities: Map<Long?, Entity?>?
	val blockEntities: Map<Long?, BlockEntity?>?
	fun getTile(x: Int, y: Int, z: Int): BlockEntity?
	val isLoaded: Boolean

	@Throws(IOException::class)
	fun load(): Boolean

	@Throws(IOException::class)
	fun load(generate: Boolean): Boolean

	@Throws(Exception::class)
	fun unload(): Boolean

	@Throws(Exception::class)
	fun unload(save: Boolean): Boolean

	@Throws(Exception::class)
	fun unload(save: Boolean, safe: Boolean): Boolean
	fun initChunk()
	val biomeIdArray: ByteArray?
	val heightMapArray: ByteArray?
	val blockIdArray: ByteArray?
	val blockDataArray: ByteArray?
	val blockExtraDataArray: Map<Int?, Int?>?
	val blockSkyLightArray: ByteArray?
	val blockLightArray: ByteArray?
	fun toBinary(): ByteArray?
	fun toFastBinary(): ByteArray?
	fun hasChanged(): Boolean
	fun setChanged()
	fun setChanged(changed: Boolean)
}