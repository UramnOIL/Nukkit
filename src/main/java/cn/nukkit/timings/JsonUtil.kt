package cn.nukkit.timings

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.util.*
import java.util.function.Function

/**
 * @author Tee7even
 *
 *
 * Various methods for more compact JSON object constructing
 */
object JsonUtil {
	private val GSON = GsonBuilder().setPrettyPrinting().create()
	@JvmStatic
    fun toArray(vararg objects: Any?): JsonArray {
		val array: List<*> = ArrayList<Any?>()
		Collections.addAll(array, *objects)
		return GSON.toJsonTree(array).asJsonArray
	}

	@JvmStatic
    fun toObject(`object`: Any?): JsonObject {
		return GSON.toJsonTree(`object`).asJsonObject
	}

	@JvmStatic
    fun <E> mapToObject(collection: Iterable<E>, mapper: Function<E, JSONPair?>): JsonObject {
		val `object`: MutableMap<*, *> = LinkedHashMap<Any?, Any?>()
		for (e in collection) {
			val pair = mapper.apply(e)
			if (pair != null) {
				`object`[pair.key] = pair.value
			}
		}
		return GSON.toJsonTree(`object`).asJsonObject
	}

	fun <E> mapToArray(elements: Array<E>, mapper: Function<E, Any?>?): JsonArray {
		val array: ArrayList<*> = ArrayList<Any?>()
		Collections.addAll(array, *elements)
		return mapToArray(array, mapper)
	}

	fun <E> mapToArray(collection: Iterable<E>, mapper: Function<E, Any?>): JsonArray {
		val array: MutableList<*> = ArrayList<Any?>()
		for (e in collection) {
			val obj = mapper.apply(e)
			if (obj != null) {
				array.add(obj)
			}
		}
		return GSON.toJsonTree(array).asJsonArray
	}

	class JSONPair {
		val key: String
		val value: Any

		constructor(key: String, value: Any) {
			this.key = key
			this.value = value
		}

		constructor(key: Int, value: Any) {
			this.key = key.toString()
			this.value = value
		}
	}
}