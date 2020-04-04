package cn.nukkit.nbt.stream

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import kotlin.jvm.Throws

class PGZIPState(parent: PGZIPOutputStream?) {
	val str: DeflaterOutputStream?
	val buf: ByteArrayOutputStream?
	val def: Deflater?

	init {
		def = parent!!.newDeflater()
		buf = ByteArrayOutputStream(PGZIPBlock.SIZE)
		str = PGZIPOutputStream.newDeflaterOutputStream(buf, def)
	}
}