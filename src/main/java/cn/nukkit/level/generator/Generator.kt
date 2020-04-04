package cn.nukkit.level.generator

import cn.nukkit.level.ChunkManager
import cn.nukkit.level.Level
import cn.nukkit.math.NukkitRandom
import cn.nukkit.math.Vector3
import java.util.*
import kotlin.collections.MutableMap
import kotlin.collections.set

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class Generator {
	abstract val id: Int
	open val dimension: Int = Level.DIMENSION_OVERWORLD

	abstract fun init(level: ChunkManager, random: NukkitRandom)
	abstract fun generateChunk(chunkX: Int, chunkZ: Int)
	abstract fun populateChunk(chunkX: Int, chunkZ: Int)
	abstract val settings: Map<String, Any>
	abstract val name: String
	abstract val spawn: Vector3
	abstract val chunkManager: ChunkManager

	companion object {
		const val TYPE_OLD = 0
		const val TYPE_INFINITE = 1
		const val TYPE_FLAT = 2
		const val TYPE_NETHER = 3
		private val nameList: MutableMap<String, Class<out Generator>> = HashMap()
		private val typeList: MutableMap<Int, Class<out Generator>> = HashMap()
		fun addGenerator(clazz: Class<out Generator>?, name: String, type: Int): Boolean {
			var name = name
			name = name.toLowerCase()
			if (clazz != null && !nameList.containsKey(name)) {
				nameList[name] = clazz
				if (!typeList.containsKey(type)) {
					typeList[type] = clazz
				}
				return true
			}
			return false
		}

		val generatorList: Array<String>
			get() {
				return nameList.keys.toTypedArray()
			}

		fun getGenerator(name: String): Class<out Generator> {
			var name = name
			name = name.toLowerCase()
			return if (nameList.containsKey(name)) {
				nameList[name]!!
			} else Normal::class.java
		}

		fun getGenerator(type: Int): Class<out Generator> {
			return if (typeList.containsKey(type)) {
				typeList[type]!!
			} else Normal::class.java
		}

		fun getGeneratorName(c: Class<out Generator?>): String {
			for (key in nameList.keys) {
				if (nameList[key] == c) {
					return key
				}
			}
			return "unknown"
		}

		fun getGeneratorType(c: Class<out Generator?>): Int {
			for (key in typeList.keys) {
				if (typeList[key] == c) {
					return key
				}
			}
			return TYPE_INFINITE
		}
	}
}