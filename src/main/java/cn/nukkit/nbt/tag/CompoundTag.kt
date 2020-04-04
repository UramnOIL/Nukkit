package cn.nukkit.nbt.tag

import cn.nukkit.nbt.stream.NBTInputStream
import cn.nukkit.nbt.stream.NBTOutputStream
import java.io.IOException
import java.io.PrintStream
import java.util.Collection
import java.util.HashMap
import java.util.Map
import java.util.Map.Entry
import java.util.StringJoiner
import kotlin.jvm.Throws

class CompoundTag : Tag, Cloneable {
	private val tags: Map<String, Tag> = HashMap()

	constructor() : super("") {}
	constructor(name: String?) : super(name) {}

	@Override
	@Throws(IOException::class)
	override fun write(dos: NBTOutputStream) {
		for (entry in tags.entrySet()) {
			Tag.writeNamedTag(entry.getValue(), entry.getKey(), dos)
		}
		dos.writeByte(Tag.TAG_End)
	}

	@Override
	@Throws(IOException::class)
	override fun load(dis: NBTInputStream) {
		tags.clear()
		var tag: Tag
		while (Tag.readNamedTag(dis).also({ tag = it }).getId() !== Tag.TAG_End) {
			tags.put(tag.getName(), tag)
		}
	}

	val allTags: Collection<cn.nukkit.nbt.tag.Tag>
		get() = tags.values()

	@get:Override
	override val id: Byte
		get() = TAG_Compound

	fun put(name: String?, tag: Tag): CompoundTag {
		tags.put(name, tag.setName(name))
		return this
	}

	fun putByte(name: String?, value: Int): CompoundTag {
		tags.put(name, ByteTag(name, value))
		return this
	}

	fun putShort(name: String?, value: Int): CompoundTag {
		tags.put(name, ShortTag(name, value))
		return this
	}

	fun putInt(name: String?, value: Int): CompoundTag {
		tags.put(name, IntTag(name, value))
		return this
	}

	fun putLong(name: String?, value: Long): CompoundTag {
		tags.put(name, LongTag(name, value))
		return this
	}

	fun putFloat(name: String?, value: Float): CompoundTag {
		tags.put(name, FloatTag(name, value))
		return this
	}

	fun putDouble(name: String?, value: Double): CompoundTag {
		tags.put(name, DoubleTag(name, value))
		return this
	}

	fun putString(name: String?, value: String?): CompoundTag {
		tags.put(name, StringTag(name, value))
		return this
	}

	fun putByteArray(name: String?, value: ByteArray?): CompoundTag {
		tags.put(name, ByteArrayTag(name, value))
		return this
	}

	fun putIntArray(name: String?, value: IntArray?): CompoundTag {
		tags.put(name, IntArrayTag(name, value))
		return this
	}

	fun putList(listTag: ListTag<out Tag?>): CompoundTag {
		tags.put(listTag.getName(), listTag)
		return this
	}

	fun putCompound(name: String?, value: CompoundTag): CompoundTag {
		tags.put(name, value.setName(name))
		return this
	}

	fun putBoolean(string: String?, `val`: Boolean): CompoundTag {
		putByte(string, if (`val`) 1 else 0)
		return this
	}

	operator fun get(name: String): Tag? {
		return tags[name]
	}

	operator fun contains(name: String): Boolean {
		return tags.containsKey(name)
	}

	fun remove(name: String?): CompoundTag {
		tags.remove(name)
		return this
	}

	fun <T : Tag?> removeAndGet(name: String?): T {
		return tags.remove(name) as T
	}

	fun getByte(name: String): Int {
		return if (!tags.containsKey(name)) 0.toByte() else (tags[name] as NumberTag?).getData().intValue()
	}

	fun getShort(name: String): Int {
		return if (!tags.containsKey(name)) 0 else (tags[name] as NumberTag?).getData().intValue()
	}

	fun getInt(name: String): Int {
		return if (!tags.containsKey(name)) 0 else (tags[name] as NumberTag?).getData().intValue()
	}

	fun getLong(name: String): Long {
		return if (!tags.containsKey(name)) 0.toLong() else (tags[name] as NumberTag?).getData().longValue()
	}

	fun getFloat(name: String): Float {
		return if (!tags.containsKey(name)) 0.toFloat() else (tags[name] as NumberTag?).getData().floatValue()
	}

	fun getDouble(name: String): Double {
		return if (!tags.containsKey(name)) 0.toDouble() else (tags[name] as NumberTag?).getData().doubleValue()
	}

	fun getString(name: String): String? {
		if (!tags.containsKey(name)) return ""
		val tag: Tag? = tags[name]
		return if (tag is NumberTag) {
			String.valueOf(tag.getData())
		} else (tag as StringTag?)!!.data
	}

	fun getByteArray(name: String): ByteArray? {
		return if (!tags.containsKey(name)) ByteArray(0) else (tags[name] as ByteArrayTag?)!!.data
	}

	fun getIntArray(name: String): IntArray? {
		return if (!tags.containsKey(name)) IntArray(0) else (tags[name] as IntArrayTag?)!!.data
	}

	fun getCompound(name: String): CompoundTag? {
		return if (!tags.containsKey(name)) CompoundTag(name) else tags[name] as CompoundTag?
	}

	@SuppressWarnings("unchecked")
	fun getList(name: String): ListTag<out Tag?>? {
		return if (!tags.containsKey(name)) ListTag(name) else tags[name] as ListTag<out Tag?>?
	}

	@SuppressWarnings("unchecked")
	fun <T : Tag?> getList(name: String, type: Class<T>?): ListTag<T>? {
		return if (tags.containsKey(name)) {
			tags[name] as ListTag<T>?
		} else ListTag(name)
	}

	fun getTags(): Map<String, Tag> {
		return HashMap(tags)
	}

	@Override
	override fun parseValue(): Map<String, Object> {
		val value: Map<String, Object> = HashMap(tags.size())
		for (entry in tags.entrySet()) {
			value.put(entry.getKey(), entry.getValue().parseValue())
		}
		return value
	}

	fun getBoolean(name: String): Boolean {
		return getByte(name) != 0
	}

	override fun toString(): String {
		val joiner = StringJoiner(",\n\t")
		tags.forEach { key, tag -> joiner.add('\'' + key + "' : " + tag.toString().replace("\n", "\n\t")) }
		return """CompoundTag '${this.getName().toString()}' (${tags.size().toString()} entries) {
	${joiner.toString().toString()}
}"""
	}

	fun print(prefix: String, out: PrintStream) {
		var prefix = prefix
		super.print(prefix, out)
		out.println("$prefix{")
		val orgPrefix = prefix
		prefix += "   "
		for (tag in tags.values()) {
			tag.print(prefix, out)
		}
		out.println("$orgPrefix}")
	}

	val isEmpty: Boolean
		get() = tags.isEmpty()

	override fun copy(): CompoundTag {
		val tag = CompoundTag(getName())
		for (key in tags.keySet()) {
			tag.put(key, tags[key]!!.copy())
		}
		return tag
	}

	@Override
	override fun equals(obj: Object): Boolean {
		if (super.equals(obj)) {
			val o = obj as CompoundTag
			return tags.entrySet().equals(o.tags.entrySet())
		}
		return false
	}

	/**
	 * Check existence of NBT tag
	 *
	 * @param name - NBT tag Id.
	 * @return - true, if tag exists
	 */
	fun exist(name: String): Boolean {
		return tags.containsKey(name)
	}

	@Override
	fun clone(): CompoundTag {
		val nbt = CompoundTag()
		getTags().forEach { key, value -> nbt.put(key, value.copy()) }
		return nbt
	}
}