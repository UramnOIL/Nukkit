package cn.nukkit.nbt.tag

import cn.nukkit.nbt.stream.NBTInputStream
import cn.nukkit.nbt.stream.NBTOutputStream
import java.io.IOException
import kotlin.jvm.Throws

class ShortTag : NumberTag<Integer?> {
	override var data = 0

	@Override
	fun getData(): Integer {
		return data
	}

	@Override
	fun setData(data: Integer?) {
		this.data = if (data == null) 0 else data
	}

	constructor(name: String?) : super(name) {}
	constructor(name: String?, data: Int) : super(name) {
		this.data = data
	}

	@Override
	@Throws(IOException::class)
	override fun write(dos: NBTOutputStream) {
		dos.writeShort(data)
	}

	@Override
	@Throws(IOException::class)
	override fun load(dis: NBTInputStream) {
		data = dis.readUnsignedShort()
	}

	@Override
	override fun parseValue(): Integer {
		return data
	}

	@get:Override
	override val id: Byte
		get() = TAG_Short

	@Override
	override fun toString(): String {
		return "" + data
	}

	@Override
	override fun copy(): Tag {
		return ShortTag(getName(), data)
	}

	@Override
	override fun equals(obj: Object): Boolean {
		if (super.equals(obj)) {
			val o = obj as ShortTag
			return data == o.data
		}
		return false
	}
}