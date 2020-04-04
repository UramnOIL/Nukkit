package cn.nukkit.nbt.tag

import cn.nukkit.nbt.stream.NBTInputStream
import cn.nukkit.nbt.stream.NBTOutputStream
import java.io.IOException
import kotlin.jvm.Throws

class EndTag : Tag(null) {
	@Override
	@Throws(IOException::class)
	override fun load(dis: NBTInputStream?) {
	}

	@Override
	@Throws(IOException::class)
	override fun write(dos: NBTOutputStream?) {
	}

	@get:Override
	override val id: Byte
		get() = TAG_End

	@Override
	override fun toString(): String {
		return "EndTag"
	}

	@Override
	override fun copy(): Tag {
		return EndTag()
	}

	@Override
	override fun parseValue(): Object? {
		return null
	}
}