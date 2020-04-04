package cn.nukkit.utils

import cn.nukkit.entity.Attribute
import cn.nukkit.entity.data.Skin
import cn.nukkit.item.Item
import cn.nukkit.item.Item.Companion.get
import cn.nukkit.item.ItemDurable
import cn.nukkit.level.GameRule
import cn.nukkit.level.GameRules
import cn.nukkit.math.BlockFace
import cn.nukkit.math.BlockVector3
import cn.nukkit.math.Vector3f
import cn.nukkit.nbt.NBTIO.read
import cn.nukkit.nbt.NBTIO.write
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.nbt.tag.StringTag
import cn.nukkit.network.protocol.types.EntityLink
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream
import java.io.IOException
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class BinaryStream {
	var offset: Int
	private var buffer: ByteArray?
	var count: Int
		private set

	constructor() {
		buffer = ByteArray(32)
		offset = 0
		count = 0
	}

	@JvmOverloads
	constructor(buffer: ByteArray, offset: Int = 0) {
		this.buffer = buffer
		this.offset = offset
		count = buffer.size
	}

	open fun reset(): BinaryStream? {
		offset = 0
		count = 0
		return this
	}

	fun setBuffer(buffer: ByteArray?) {
		this.buffer = buffer
		count = buffer?.size ?: -1
	}

	fun setBuffer(buffer: ByteArray?, offset: Int) {
		this.setBuffer(buffer)
		this.offset = offset
	}

	fun getBuffer(): ByteArray {
		return Arrays.copyOf(buffer, count)
	}

	@JvmOverloads
	operator fun get(len: Int = count - offset): ByteArray {
		var len = len
		if (len < 0) {
			offset = count - 1
			return ByteArray(0)
		}
		len = Math.min(len, count - offset)
		offset += len
		return Arrays.copyOfRange(buffer, offset - len, offset)
	}

	fun put(bytes: ByteArray?) {
		if (bytes == null) {
			return
		}
		ensureCapacity(count + bytes.size)
		System.arraycopy(bytes, 0, buffer, count, bytes.size)
		count += bytes.size
	}

	val long: Long
		get() = Binary.Companion.readLong(this[8])

	fun putLong(l: Long) {
		put(Binary.Companion.writeLong(l))
	}

	val int: Int
		get() = Binary.Companion.readInt(this[4])

	fun putInt(i: Int) {
		put(Binary.Companion.writeInt(i))
	}

	val lLong: Long
		get() = Binary.Companion.readLLong(this[8])

	fun putLLong(l: Long) {
		put(Binary.Companion.writeLLong(l))
	}

	val lInt: Int
		get() = Binary.Companion.readLInt(this[4])

	fun putLInt(i: Int) {
		put(Binary.Companion.writeLInt(i))
	}

	val short: Int
		get() = Binary.Companion.readShort(this[2])

	fun putShort(s: Int) {
		put(Binary.Companion.writeShort(s))
	}

	val lShort: Int
		get() = Binary.Companion.readLShort(this[2])

	fun putLShort(s: Int) {
		put(Binary.Companion.writeLShort(s))
	}

	val float: Float
		get() = getFloat(-1)

	fun getFloat(accuracy: Int): Float {
		return Binary.Companion.readFloat(this[4], accuracy)
	}

	fun putFloat(v: Float) {
		put(Binary.Companion.writeFloat(v))
	}

	val lFloat: Float
		get() = getLFloat(-1)

	fun getLFloat(accuracy: Int): Float {
		return Binary.Companion.readLFloat(this[4], accuracy)
	}

	fun putLFloat(v: Float) {
		put(Binary.Companion.writeLFloat(v))
	}

	val triad: Int
		get() = Binary.Companion.readTriad(this[3])

	fun putTriad(triad: Int) {
		put(Binary.Companion.writeTriad(triad))
	}

	val lTriad: Int
		get() = Binary.Companion.readLTriad(this[3])

	fun putLTriad(triad: Int) {
		put(Binary.Companion.writeLTriad(triad))
	}

	val boolean: Boolean
		get() = byte == 0x01

	fun putBoolean(bool: Boolean) {
		putByte((if (bool) 1 else 0).toByte())
	}

	val byte: Int
		get() = buffer!![offset++] and 0xff

	fun putByte(b: Byte) {
		put(byteArrayOf(b))
	}

	/**
	 * Reads a list of Attributes from the stream.
	 *
	 * @return Attribute[]
	 */
	@get:Throws(Exception::class)
	val attributeList: Array<Attribute>
		get() {
			val list: MutableList<Attribute> = ArrayList()
			val count = unsignedVarInt
			for (i in 0 until count) {
				val name = string
				val attr = Attribute.getAttributeByName(name)
				if (attr != null) {
					attr.setMinValue(lFloat)
					attr.setValue(lFloat)
					attr.setMaxValue(lFloat)
					list.add(attr)
				} else {
					throw Exception("Unknown attribute type \"$name\"")
				}
			}
			return list.toTypedArray()
		}

	/**
	 * Writes a list of Attributes to the packet buffer using the standard format.
	 */
	fun putAttributeList(attributes: Array<Attribute>) {
		putUnsignedVarInt(attributes.size.toLong())
		for (attribute in attributes) {
			putString(attribute.name)
			putLFloat(attribute.minValue)
			putLFloat(attribute.value)
			putLFloat(attribute.maxValue)
		}
	}

	fun putUUID(uuid: UUID) {
		put(Binary.Companion.writeUUID(uuid))
	}

	val uUID: UUID
		get() = Binary.Companion.readUUID(this[16])

	fun putSkin(skin: Skin) {
		putString(skin.getSkinId())
		putString(skin.getSkinResourcePatch())
		putImage(skin.getSkinData())
		val animations: List<SkinAnimation?> = skin.animations
		putLInt(animations.size)
		for (animation in animations) {
			putImage(animation!!.image)
			putLInt(animation.type)
			putLFloat(animation.frames)
		}
		putImage(skin.getCapeData())
		putString(skin.geometryData)
		putString(skin.animationData)
		putBoolean(skin.isPremium)
		putBoolean(skin.isPersona)
		putBoolean(skin.isCapeOnClassic)
		putString(skin.capeId)
		putString(skin.fullSkinId)
	}

	// TODO: Full skin id
	open val skin: Skin?
		get() {
			val skin = Skin()
			skin.setSkinId(string)
			skin.setSkinResourcePatch(string)
			skin.setSkinData(image)
			val animationCount = lInt
			for (i in 0 until animationCount) {
				val image = image
				val type = lInt
				val frames = lFloat
				skin.animations.add(SkinAnimation(image, type, frames))
			}
			skin.setCapeData(image)
			skin.geometryData = string
			skin.animationData = string
			skin.isPremium = boolean
			skin.isPersona = boolean
			skin.isCapeOnClassic = boolean
			skin.capeId = string
			string // TODO: Full skin id
			return skin
		}

	fun putImage(image: SerializedImage) {
		putLInt(image.width)
		putLInt(image.height)
		putByteArray(image.data)
	}

	open val image: SerializedImage?
		get() {
			val width = lInt
			val height = lInt
			val data = byteArray
			return SerializedImage(width, height, data)
		}// TODO: Shields

	// TODO: 05/02/2019 This hack is necessary because we keep the raw NBT tag. Try to remove it.
	open val slot: Item?
		get() {
			val id = varInt
			if (id == 0) {
				return get(0, 0, 0)
			}
			val auxValue = varInt
			var data = auxValue shr 8
			if (data == Short.MAX_VALUE.toInt()) {
				data = -1
			}
			val cnt = auxValue and 0xff
			val nbtLen = lShort
			var nbt: ByteArray? = ByteArray(0)
			if (nbtLen < Short.MAX_VALUE) {
				nbt = this[nbtLen]
			} else if (nbtLen == 65535) {
				val nbtTagCount = unsignedVarInt.toInt()
				var offset = offset
				val stream = FastByteArrayInputStream(get())
				for (i in 0 until nbtTagCount) {
					try {
						// TODO: 05/02/2019 This hack is necessary because we keep the raw NBT tag. Try to remove it.
						val tag = read(stream, ByteOrder.LITTLE_ENDIAN, true)
						// tool damage hack
						if (tag.contains("Damage")) {
							data = tag.getInt("Damage")
							tag.remove("Damage")
						}
						if (tag.contains("__DamageConflict__")) {
							tag.put("Damage", tag.removeAndGet("__DamageConflict__"))
						}
						if (tag.allTags.size > 0) {
							nbt = write(tag, ByteOrder.LITTLE_ENDIAN, false)
						}
					} catch (e: IOException) {
						throw RuntimeException(e)
					}
				}
				offset = offset + stream.position().toInt()
			}
			val canPlaceOn = arrayOfNulls<String>(varInt)
			for (i in canPlaceOn.indices) {
				canPlaceOn[i] = string
			}
			val canDestroy = arrayOfNulls<String>(varInt)
			for (i in canDestroy.indices) {
				canDestroy[i] = string
			}
			val item = get(
					id, data, cnt, nbt!!
			)
			if (canDestroy.size > 0 || canPlaceOn.size > 0) {
				var namedTag = item.namedTag
				if (namedTag == null) {
					namedTag = CompoundTag()
				}
				if (canDestroy.size > 0) {
					val listTag = ListTag<StringTag>("CanDestroy")
					for (blockName in canDestroy) {
						listTag.add(StringTag("", blockName))
					}
					namedTag.put("CanDestroy", listTag)
				}
				if (canPlaceOn.size > 0) {
					val listTag = ListTag<StringTag>("CanPlaceOn")
					for (blockName in canPlaceOn) {
						listTag.add(StringTag("", blockName))
					}
					namedTag.put("CanPlaceOn", listTag)
				}
				item.setNamedTag(namedTag)
			}
			if (item.id == 513) { // TODO: Shields
				varLong
			}
			return item
		}

	fun putSlot(item: Item?) {
		if (item == null || item.id == 0) {
			putVarInt(0)
			return
		}
		val isDurable = item is ItemDurable
		putVarInt(item.id)
		var auxValue = item.count
		if (!isDurable) {
			auxValue = auxValue or ((if (item.hasMeta()) item.damage else -1)!! and 0x7fff shl 8)
		}
		putVarInt(auxValue)
		if (item.hasCompoundTag() || isDurable) {
			try {
				// hack for tool damage
				val nbt = item.compoundTag
				val tag: CompoundTag
				tag = if (nbt == null || nbt.size == 0) {
					CompoundTag()
				} else {
					read(nbt, ByteOrder.LITTLE_ENDIAN, false)
				}
				if (tag.contains("Damage")) {
					tag.put("__DamageConflict__", tag.removeAndGet("Damage"))
				}
				if (isDurable) {
					tag.putInt("Damage", item.damage!!)
				}
				putLShort(0xffff)
				putByte(1.toByte())
				put(write(tag, ByteOrder.LITTLE_ENDIAN, true))
			} catch (e: IOException) {
				throw RuntimeException(e)
			}
		} else {
			putLShort(0)
		}
		val canPlaceOn = extractStringList(item, "CanPlaceOn")
		val canDestroy = extractStringList(item, "CanDestroy")
		putVarInt(canPlaceOn.size)
		for (block in canPlaceOn) {
			putString(block)
		}
		putVarInt(canDestroy.size)
		for (block in canDestroy) {
			putString(block)
		}
		if (item.id == 513) { // TODO: Shields
			putVarLong(0)
		}
	}

	val recipeIngredient: Item
		get() {
			val id = varInt
			if (id == 0) {
				return get(0, 0, 0)
			}
			var damage = varInt
			if (damage == 0x7fff) damage = -1
			val count = varInt
			return get(id, damage, count)
		}

	fun putRecipeIngredient(ingredient: Item?) {
		if (ingredient == null || ingredient.id == 0) {
			putVarInt(0)
			return
		}
		putVarInt(ingredient.id)
		val damage: Int
		damage = if (ingredient.hasMeta()) {
			ingredient.damage!!
		} else {
			0x7fff
		}
		putVarInt(damage)
		putVarInt(ingredient.count)
	}

	private fun extractStringList(item: Item, tagName: String): List<String?> {
		val namedTag = item.namedTag ?: return emptyList<String>()
		val listTag = namedTag.getList(tagName, StringTag::class.java) ?: return emptyList<String>()
		val size = listTag.size()
		val values: MutableList<String?> = ArrayList(size)
		for (i in 0 until size) {
			val stringTag = listTag[i]
			if (stringTag != null) {
				values.add(stringTag.data)
			}
		}
		return values
	}

	val byteArray: ByteArray
		get() = this[unsignedVarInt.toInt()]

	fun putByteArray(b: ByteArray) {
		putUnsignedVarInt(b.size.toLong())
		put(b)
	}

	val string: String
		get() = String(byteArray, StandardCharsets.UTF_8)

	fun putString(string: String?) {
		val b = string!!.toByteArray(StandardCharsets.UTF_8)
		putByteArray(b)
	}

	val unsignedVarInt: Long
		get() = VarInt.readUnsignedVarInt(this)

	fun putUnsignedVarInt(v: Long) {
		VarInt.writeUnsignedVarInt(this, v)
	}

	val varInt: Int
		get() = VarInt.readVarInt(this)

	fun putVarInt(v: Int) {
		VarInt.writeVarInt(this, v)
	}

	val varLong: Long
		get() = VarInt.readVarLong(this)

	fun putVarLong(v: Long) {
		VarInt.writeVarLong(this, v)
	}

	val unsignedVarLong: Long
		get() = VarInt.readUnsignedVarLong(this)

	fun putUnsignedVarLong(v: Long) {
		VarInt.writeUnsignedVarLong(this, v)
	}

	val blockVector3: BlockVector3
		get() = BlockVector3(varInt, unsignedVarInt.toInt(), varInt)

	val signedBlockPosition: BlockVector3
		get() = BlockVector3(varInt, varInt, varInt)

	fun putSignedBlockPosition(v: BlockVector3) {
		putVarInt(v.x)
		putVarInt(v.y)
		putVarInt(v.z)
	}

	fun putBlockVector3(v: BlockVector3) {
		this.putBlockVector3(v.x, v.y, v.z)
	}

	fun putBlockVector3(x: Int, y: Int, z: Int) {
		putVarInt(x)
		putUnsignedVarInt(y.toLong())
		putVarInt(z)
	}

	val vector3f: Vector3f
		get() = Vector3f(getLFloat(4), getLFloat(4), getLFloat(4))

	fun putVector3f(v: Vector3f) {
		this.putVector3f(v.x, v.y, v.z)
	}

	fun putVector3f(x: Float, y: Float, z: Float) {
		putLFloat(x)
		putLFloat(y)
		putLFloat(z)
	}

	fun putGameRules(gameRules: GameRules) {
		val rules = gameRules.getGameRules()
		putUnsignedVarInt(rules.size.toLong())
		rules.forEach { (gameRule: GameRule, value: GameRules.Value<*>) ->
			putString(gameRule.ruleName.toLowerCase())
			value.write(this)
		}
	}

	/**
	 * Reads and returns an EntityUniqueID
	 *
	 * @return int
	 */
	open val entityUniqueId: Long
		get() = varLong

	/**
	 * Writes an EntityUniqueID
	 */
	fun putEntityUniqueId(eid: Long) {
		putVarLong(eid)
	}

	/**
	 * Reads and returns an EntityRuntimeID
	 */
	open val entityRuntimeId: Long
		get() = unsignedVarLong

	/**
	 * Writes an EntityUniqueID
	 */
	fun putEntityRuntimeId(eid: Long) {
		putUnsignedVarLong(eid)
	}

	val blockFace: BlockFace?
		get() = BlockFace.fromIndex(varInt)

	fun putBlockFace(face: BlockFace) {
		putVarInt(face.index)
	}

	fun putEntityLink(link: EntityLink) {
		putEntityUniqueId(link.fromEntityUniquieId)
		putEntityUniqueId(link.toEntityUniquieId)
		putByte(link.type)
		putBoolean(link.immediate)
	}

	val entityLink: EntityLink
		get() = EntityLink(
				entityUniqueId,
				entityUniqueId,
				byte.toByte(),
				boolean
		)

	fun feof(): Boolean {
		return offset < 0 || offset >= buffer!!.size
	}

	private fun ensureCapacity(minCapacity: Int) {
		// overflow-conscious code
		if (minCapacity - buffer!!.size > 0) {
			grow(minCapacity)
		}
	}

	private fun grow(minCapacity: Int) {
		// overflow-conscious code
		val oldCapacity = buffer!!.size
		var newCapacity = oldCapacity shl 1
		if (newCapacity - minCapacity < 0) {
			newCapacity = minCapacity
		}
		if (newCapacity - MAX_ARRAY_SIZE > 0) {
			newCapacity = hugeCapacity(minCapacity)
		}
		buffer = Arrays.copyOf(buffer, newCapacity)
	}

	companion object {
		private const val MAX_ARRAY_SIZE = Int.MAX_VALUE - 8
		private fun hugeCapacity(minCapacity: Int): Int {
			if (minCapacity < 0) { // overflow
				throw OutOfMemoryError()
			}
			return if (minCapacity > MAX_ARRAY_SIZE) Int.MAX_VALUE else MAX_ARRAY_SIZE
		}
	}
}