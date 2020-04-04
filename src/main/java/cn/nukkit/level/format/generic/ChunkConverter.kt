package cn.nukkit.level.format.generic

import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.format.LevelProvider
import cn.nukkit.level.format.anvil.ChunkSection
import cn.nukkit.level.format.mcregion.Chunk
import cn.nukkit.nbt.tag.CompoundTag
import java.util.*
import java.util.function.Consumer

class ChunkConverter(private val provider: LevelProvider) {
	private var chunk: BaseFullChunk? = null
	private var toClass: Class<out FullChunk>? = null
	fun from(chunk: BaseFullChunk?): ChunkConverter {
		require(!(chunk !is Chunk && chunk !is cn.nukkit.level.format.leveldb.Chunk)) { "From type can be only McRegion or LevelDB" }
		this.chunk = chunk
		return this
	}

	fun to(toClass: Class<out FullChunk>): ChunkConverter {
		require(toClass == cn.nukkit.level.format.anvil.Chunk::class.java) { "To type can be only Anvil" }
		this.toClass = toClass
		return this
	}

	fun perform(): FullChunk {
		val result: BaseFullChunk
		result = try {
			toClass!!.getMethod("getEmptyChunk", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, LevelProvider::class.java).invoke(null, chunk!!.getX(), chunk!!.getZ(), provider) as BaseFullChunk
		} catch (e: Exception) {
			throw RuntimeException(e)
		}
		if (toClass == cn.nukkit.level.format.anvil.Chunk::class.java) {
			for (Y in 0..7) {
				var empty = true
				for (x in 0..15) {
					for (y in 0..15) {
						for (z in 0..15) {
							if (chunk!!.getBlockId(x, Y shl 4 or y, z) != 0) {
								empty = false
								break
							}
						}
						if (!empty) break
					}
					if (!empty) break
				}
				if (!empty) {
					val section = ChunkSection(Y)
					for (x in 0..15) {
						for (y in 0..15) {
							for (z in 0..15) {
								section.setBlockId(x, y, z, chunk!!.getBlockId(x, Y shl 4 or y, z))
								section.setBlockData(x, y, z, chunk!!.getBlockData(x, Y shl 4 or y, z))
								section.setBlockLight(x, y, z, chunk!!.getBlockLight(x, Y shl 4 or y, z))
								section.setBlockSkyLight(x, y, z, chunk!!.getBlockSkyLight(x, Y shl 4 or y, z))
							}
						}
					}
					(result as BaseChunk).sections!!.get(Y) = section
				}
			}
		}
		System.arraycopy(chunk.biomes, 0, result.biomes, 0, 256)
		System.arraycopy(chunk.getHeightMapArray(), 0, result.heightMap, 0, 256)
		if (chunk!!.NBTentities != null && !chunk!!.NBTentities!!.isEmpty()) {
			result.NBTentities = ArrayList(chunk!!.NBTentities!!.size)
			chunk!!.NBTentities!!.forEach(Consumer { nbt: CompoundTag? -> result.NBTentities.add(nbt!!.copy()) })
		}
		if (chunk!!.NBTtiles != null && !chunk!!.NBTtiles!!.isEmpty()) {
			result.NBTtiles = ArrayList(chunk!!.NBTtiles!!.size)
			chunk!!.NBTtiles!!.forEach(Consumer { nbt: CompoundTag? -> result.NBTtiles.add(nbt!!.copy()) })
		}
		result.setGenerated()
		result.setPopulated()
		result.setLightPopulated()
		result.initChunk()
		return result
	}

}