package cn.nukkit.nbt.tag

import cn.nukkit.nbt.stream.NBTInputStream
import cn.nukkit.nbt.stream.NBTOutputStream
import java.io.IOException
import kotlin.jvm.Throws

class StringTag : Tag {
	var data: String? = null

	constructor(name: String?) : super(name) {}
	constructor(name: String?, data: String?) : super(name) {
		this.data = data
		if (data == null) throw IllegalArgumentException("Empty string not allowed")
	}

	@Override
	@Throws(IOException::class)
	override fun write(dos: NBTOutputStream) {
		dos.writeUTF(data)
	}

	@Override
	@Throws(IOException::class)
	override fun load(dis: NBTInputStream) {
		data = dis.readUTF()
	}

	@Override
	override fun parseValue(): String? {
		return data
	}

	@get:Override
	override val id: Byte
		get() = TAG_String

	@Override
	override fun toString(): String {
		return "StringTag " + this.getName().toString() + " (data: " + data.toString() + ")"
	}

	@Override
	override fun copy(): Tag {
		return StringTag(getName(), data)
	}

	@Override
	override fun equals(obj: Object): Boolean {
		if (super.equals(obj)) {
			val o = obj as StringTag
			return data == null && o.data == null || data != null && data!!.equals(o.data)
		}
		return false
	}
}