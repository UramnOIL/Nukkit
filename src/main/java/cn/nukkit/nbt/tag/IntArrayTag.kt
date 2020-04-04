package cn.nukkit.nbt.tag

import cn.nukkit.nbt.stream.NBTInputStream
import cn.nukkit.nbt.stream.NBTOutputStream
import java.io.IOException
import java.util.Arrays
import kotlin.jvm.Throws

class IntArrayTag : Tag {
	var data: IntArray?

	constructor(name: String?) : super(name) {}
	constructor(name: String?, data: IntArray?) : super(name) {
		this.data = data
	}

	@Override
	@Throws(IOException::class)
	override fun write(dos: NBTOutputStream) {
		dos.writeInt(data!!.size)
		for (aData in data!!) {
			dos.writeInt(aData)
		}
	}

	@Override
	@Throws(IOException::class)
	override fun load(dis: NBTInputStream) {
		val length: Int = dis.readInt()
		data = IntArray(length)
		for (i in 0 until length) {
			data!![i] = dis.readInt()
		}
	}

	@Override
	override fun parseValue(): IntArray? {
		return data
	}

	@get:Override
	override val id: Byte
		get() = TAG_Int_Array

	@Override
	override fun toString(): String {
		return "IntArrayTag " + this.getName().toString() + " [" + data!!.size.toString() + " bytes]"
	}

	@Override
	override fun equals(obj: Object): Boolean {
		if (super.equals(obj)) {
			val intArrayTag = obj as IntArrayTag
			return data == null && intArrayTag.data == null || data != null && Arrays.equals(data, intArrayTag.data)
		}
		return false
	}

	@Override
	override fun copy(): Tag {
		val cp = IntArray(data!!.size)
		System.arraycopy(data, 0, cp, 0, data!!.size)
		return IntArrayTag(getName(), cp)
	}
}