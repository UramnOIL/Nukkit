package cn.nukkit.level.format.anvil

import cn.nukkit.block.Block
import cn.nukkit.level.format.anvil.util.BlockStorage
import cn.nukkit.level.format.anvil.util.NibbleArray
import cn.nukkit.level.format.generic.EmptyChunkSection
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.utils.Binary
import cn.nukkit.utils.ThreadCache
import cn.nukkit.utils.Utils
import cn.nukkit.utils.Zlib
import java.io.IOException
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ChunkSection : cn.nukkit.level.format.ChunkSection {
	override val y: Int
	private val storage: BlockStorage?
	var blockLight: ByteArray?
	var skyLight: ByteArray?
	protected var compressedLight: ByteArray?
	var hasBlockLight = false
	var hasSkyLight = false

	private constructor(y: Int, storage: BlockStorage?, blockLight: ByteArray?, skyLight: ByteArray?, compressedLight: ByteArray?,
						hasBlockLight: Boolean, hasSkyLight: Boolean) {
		this.y = y
		this.storage = storage
		this.skyLight = skyLight
		this.compressedLight = compressedLight
		this.hasBlockLight = hasBlockLight
		this.hasSkyLight = hasSkyLight
	}

	constructor(y: Int) {
		this.y = y
		hasBlockLight = false
		hasSkyLight = false
		storage = BlockStorage()
	}

	constructor(nbt: CompoundTag) {
		y = nbt.getByte("Y")
		val blocks = nbt.getByteArray("Blocks")
		val data = NibbleArray(nbt.getByteArray("Data"))
		storage = BlockStorage()

		// Convert YZX to XZY
		for (x in 0..15) {
			for (z in 0..15) {
				for (y in 0..15) {
					val index = getAnvilIndex(x, y, z)
					storage.setBlockId(x, y, z, blocks[index].toInt())
					storage.setBlockData(x, y, z, data[index].toInt())
				}
			}
		}
		blockLight = nbt.getByteArray("BlockLight")
		skyLight = nbt.getByteArray("SkyLight")
	}

	override fun getBlockId(x: Int, y: Int, z: Int): Int {
		synchronized(storage!!) { return storage.getBlockId(x, y, z) }
	}

	override fun setBlockId(x: Int, y: Int, z: Int, id: Int) {
		synchronized(storage!!) { storage.setBlockId(x, y, z, id) }
	}

	override fun setFullBlockId(x: Int, y: Int, z: Int, fullId: Int): Boolean {
		synchronized(storage!!) { storage.setFullBlock(x, y, z, fullId as Char.toInt()) }
		return true
	}

	override fun getBlockData(x: Int, y: Int, z: Int): Int {
		synchronized(storage!!) { return storage.getBlockData(x, y, z) }
	}

	override fun setBlockData(x: Int, y: Int, z: Int, data: Int) {
		synchronized(storage!!) { storage.setBlockData(x, y, z, data) }
	}

	override fun getFullBlock(x: Int, y: Int, z: Int): Int {
		synchronized(storage!!) { return storage.getFullBlock(x, y, z) }
	}

	override fun setBlock(x: Int, y: Int, z: Int, blockId: Int): Boolean {
		synchronized(storage!!) { return setBlock(x, y, z, blockId, 0) }
	}

	override fun getAndSetBlock(x: Int, y: Int, z: Int, block: Block?): Block? {
		synchronized(storage!!) {
			val fullId = storage.getAndSetFullBlock(x, y, z, block!!.fullId)
			return Block.fullList!![fullId]!!.clone()
		}
	}

	override fun setBlock(x: Int, y: Int, z: Int, blockId: Int, meta: Int): Boolean {
		val newFullId = (blockId shl 4) + meta
		synchronized(storage!!) {
			val previousFullId = storage.getAndSetFullBlock(x, y, z, newFullId)
			return newFullId != previousFullId
		}
	}

	override fun getBlockSkyLight(x: Int, y: Int, z: Int): Int {
		if (skyLight == null) {
			if (!hasSkyLight) {
				return 0
			} else if (compressedLight == null) {
				return 15
			}
		}
		skyLight = skyLightArray
		val sl: Int = skyLight!![y shl 7 or (z shl 3) or (x shr 1)] and 0xff
		return if (x and 1 == 0) {
			sl and 0x0f
		} else sl shr 4
	}

	override fun setBlockSkyLight(x: Int, y: Int, z: Int, level: Int) {
		if (skyLight == null) {
			if (hasSkyLight && compressedLight != null) {
				skyLight = skyLightArray
			} else if (level == (if (hasSkyLight) 15 else 0)) {
				return
			} else {
				skyLight = ByteArray(2048)
				if (hasSkyLight) {
					Arrays.fill(skyLight, 0xFF.toByte())
				}
			}
		}
		val i = y shl 7 or (z shl 3) or (x shr 1)
		val old: Int = skyLight!![i] and 0xff
		if (x and 1 == 0) {
			skyLight!![i] = (old and 0xf0 or (level and 0x0f)).toByte()
		} else {
			skyLight!![i] = (level and 0x0f shl 4 or (old and 0x0f)).toByte()
		}
	}

	override fun getBlockLight(x: Int, y: Int, z: Int): Int {
		if (blockLight == null && !hasBlockLight) return 0
		blockLight = lightArray
		val l: Int = blockLight!![y shl 7 or (z shl 3) or (x shr 1)] and 0xff
		return if (x and 1 == 0) {
			l and 0x0f
		} else l shr 4
	}

	override fun setBlockLight(x: Int, y: Int, z: Int, level: Int) {
		if (blockLight == null) {
			if (hasBlockLight) {
				blockLight = lightArray
			} else if (level == 0) {
				return
			} else {
				blockLight = ByteArray(2048)
			}
		}
		val i = y shl 7 or (z shl 3) or (x shr 1)
		val old: Int = blockLight!![i] and 0xff
		if (x and 1 == 0) {
			blockLight!![i] = (old and 0xf0 or (level and 0x0f)).toByte()
		} else {
			blockLight!![i] = (level and 0x0f shl 4 or (old and 0x0f)).toByte()
		}
	}

	override val idArray: ByteArray
		get() {
			synchronized(storage!!) {
				val anvil = ByteArray(4096)
				for (x in 0..15) {
					for (z in 0..15) {
						for (y in 0..15) {
							val index = getAnvilIndex(x, y, z)
							anvil[index] = storage.getBlockId(x, y, z).toByte()
						}
					}
				}
				return anvil
			}
		}

	override val dataArray: ByteArray?
		get() {
			synchronized(storage!!) {
				val anvil = NibbleArray(4096)
				for (x in 0..15) {
					for (z in 0..15) {
						for (y in 0..15) {
							val index = getAnvilIndex(x, y, z)
							anvil[index] = storage.getBlockData(x, y, z).toByte()
						}
					}
				}
				return anvil.data
			}
		}

	override val skyLightArray: ByteArray?
		get() {
			if (skyLight != null) return skyLight
			return if (hasSkyLight) {
				if (compressedLight != null) {
					inflate()
					return skyLight
				}
				EmptyChunkSection.Companion.EMPTY_SKY_LIGHT_ARR
			} else {
				EmptyChunkSection.Companion.EMPTY_LIGHT_ARR
			}
		}

	private fun inflate() {
		try {
			if (compressedLight != null && compressedLight!!.size != 0) {
				val inflated = Zlib.inflate(compressedLight)
				blockLight = Arrays.copyOfRange(inflated, 0, 2048)
				if (inflated.size > 2048) {
					skyLight = Arrays.copyOfRange(inflated, 2048, 4096)
				} else {
					skyLight = ByteArray(2048)
					if (hasSkyLight) {
						Arrays.fill(skyLight, 0xFF.toByte())
					}
				}
				compressedLight = null
			} else {
				blockLight = ByteArray(2048)
				skyLight = ByteArray(2048)
				if (hasSkyLight) {
					Arrays.fill(skyLight, 0xFF.toByte())
				}
			}
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}

	override val lightArray: ByteArray?
		get() {
			if (blockLight != null) return blockLight
			return if (hasBlockLight) {
				inflate()
				blockLight
			} else {
				EmptyChunkSection.Companion.EMPTY_LIGHT_ARR
			}
		}

	override val isEmpty: Boolean
		get() = false

	private fun toXZY(raw: CharArray): ByteArray {
		val buffer = ThreadCache.byteCache6144.get()
		for (i in 0..4095) {
			buffer[i] = (raw[i] shr 4) as Byte
		}
		var i = 0
		var j = 4096
		while (i < 4096) {
			buffer[j] = (raw[i + 1] and 0xF shl 4 or (raw[i] and 0xF)) as Byte
			i += 2
			j++
		}
		return buffer
	}

	override val bytes: ByteArray
		get() {
			synchronized(storage!!) {
				val ids = storage.blockIds
				val data = storage.blockData
				val merged = ByteArray(ids!!.size + data!!.size)
				System.arraycopy(ids, 0, merged, 0, ids.size)
				System.arraycopy(data, 0, merged, ids.size, data!!.size)
				return merged
			}
		}

	fun compress(): Boolean {
		if (blockLight != null) {
			val arr1: ByteArray = blockLight
			hasBlockLight = !Utils.isByteArrayEmpty(arr1)
			val arr2: ByteArray
			if (skyLight != null) {
				arr2 = skyLight
				hasSkyLight = !Utils.isByteArrayEmpty(arr2)
			} else if (hasSkyLight) {
				arr2 = EmptyChunkSection.Companion.EMPTY_SKY_LIGHT_ARR
			} else {
				arr2 = EmptyChunkSection.Companion.EMPTY_LIGHT_ARR
				hasSkyLight = false
			}
			blockLight = null
			skyLight = null
			var toDeflate: ByteArray? = null
			if (hasBlockLight && hasSkyLight && arr2 != EmptyChunkSection.Companion.EMPTY_SKY_LIGHT_ARR) {
				toDeflate = Binary.appendBytes(arr1, *arr2)
			} else if (hasBlockLight) {
				toDeflate = arr1
			}
			if (toDeflate != null) {
				try {
					compressedLight = Zlib.deflate(toDeflate, 1)
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
			return true
		}
		return false
	}

	override fun copy(): ChunkSection? {
		return ChunkSection(
				y,
				storage!!.copy(),
				if (blockLight == null) null else blockLight!!.clone(),
				if (skyLight == null) null else skyLight!!.clone(),
				if (compressedLight == null) null else compressedLight!!.clone(),
				hasBlockLight,
				hasSkyLight
		)
	}

	companion object {
		private fun getAnvilIndex(x: Int, y: Int, z: Int): Int {
			return (y shl 8) + (z shl 4) + x
		}
	}
}