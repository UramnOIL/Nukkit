package cn.nukkit.nbt

import cn.nukkit.item.Item
import cn.nukkit.nbt.stream.FastByteArrayOutputStream
import cn.nukkit.nbt.stream.NBTInputStream
import cn.nukkit.nbt.stream.NBTOutputStream
import cn.nukkit.nbt.stream.PGZIPOutputStream
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.Tag
import cn.nukkit.utils.ThreadCache
import java.io.*
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Collection
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import java.util.zip.GZIPInputStream
import kotlin.jvm.Throws

/**
 * A Named Binary Tag library for Nukkit Project
 */
object NBTIO {
	fun putItemHelper(item: Item): CompoundTag {
		return putItemHelper(item, null)
	}

	fun putItemHelper(item: Item, slot: Integer?): CompoundTag {
		val tag: CompoundTag = CompoundTag(null)
				.putShort("id", item.getId())
				.putByte("Count", item.getCount())
				.putShort("Damage", item.getDamage())
		if (slot != null) {
			tag.putByte("Slot", slot)
		}
		if (item.hasCompoundTag()) {
			tag.putCompound("tag", item.getNamedTag())
		}
		return tag
	}

	fun getItemHelper(tag: CompoundTag): Item {
		if (!tag.contains("id") || !tag.contains("Count")) {
			return Item.get(0)
		}
		var item: Item
		try {
			item = Item.get(tag.getShort("id"), if (!tag.contains("Damage")) 0 else tag.getShort("Damage"), tag.getByte("Count"))
		} catch (e: Exception) {
			item = Item.fromString(tag.getString("id"))
			item.setDamage(if (!tag.contains("Damage")) 0 else tag.getShort("Damage"))
			item.setCount(tag.getByte("Count"))
		}
		val tagTag: Tag = tag.get("tag")
		if (tagTag is CompoundTag) {
			item.setNamedTag(tagTag as CompoundTag)
		}
		return item
	}

	@Throws(IOException::class)
	fun read(file: File?): CompoundTag {
		return read(file, ByteOrder.BIG_ENDIAN)
	}

	@Throws(IOException::class)
	fun read(file: File, endianness: ByteOrder?): CompoundTag? {
		return if (!file.exists()) null else read(FileInputStream(file), endianness)
	}

	@Throws(IOException::class)
	fun read(inputStream: InputStream?): CompoundTag {
		return read(inputStream, ByteOrder.BIG_ENDIAN)
	}

	@Throws(IOException::class)
	fun read(inputStream: InputStream?, endianness: ByteOrder?): CompoundTag {
		return read(inputStream, endianness, false)
	}

	@Throws(IOException::class)
	fun read(inputStream: InputStream?, endianness: ByteOrder?, network: Boolean): CompoundTag {
		NBTInputStream(inputStream, endianness, network).use({ stream ->
			val tag: Tag = Tag.readNamedTag(stream)
			if (tag is CompoundTag) {
				return tag as CompoundTag
			}
			throw IOException("Root tag must be a named compound tag")
		})
	}

	@Throws(IOException::class)
	fun readTag(inputStream: InputStream?, endianness: ByteOrder?, network: Boolean): Tag {
		NBTInputStream(inputStream, endianness, network).use({ stream -> return Tag.readNamedTag(stream) })
	}

	@Throws(IOException::class)
	fun read(data: ByteArray?): CompoundTag {
		return read(data, ByteOrder.BIG_ENDIAN)
	}

	@Throws(IOException::class)
	fun read(data: ByteArray?, endianness: ByteOrder?): CompoundTag {
		return read(ByteArrayInputStream(data), endianness)
	}

	@Throws(IOException::class)
	fun read(data: ByteArray?, endianness: ByteOrder?, network: Boolean): CompoundTag {
		return read(ByteArrayInputStream(data), endianness, network)
	}

	@Throws(IOException::class)
	fun readCompressed(inputStream: InputStream?): CompoundTag {
		return readCompressed(inputStream, ByteOrder.BIG_ENDIAN)
	}

	@Throws(IOException::class)
	fun readCompressed(inputStream: InputStream?, endianness: ByteOrder?): CompoundTag {
		return read(BufferedInputStream(GZIPInputStream(inputStream)), endianness)
	}

	@Throws(IOException::class)
	fun readCompressed(data: ByteArray?): CompoundTag {
		return readCompressed(data, ByteOrder.BIG_ENDIAN)
	}

	@Throws(IOException::class)
	fun readCompressed(data: ByteArray?, endianness: ByteOrder?): CompoundTag {
		return read(BufferedInputStream(GZIPInputStream(ByteArrayInputStream(data))), endianness, true)
	}

	@Throws(IOException::class)
	fun readNetworkCompressed(inputStream: InputStream?): CompoundTag {
		return readNetworkCompressed(inputStream, ByteOrder.BIG_ENDIAN)
	}

	@Throws(IOException::class)
	fun readNetworkCompressed(inputStream: InputStream?, endianness: ByteOrder?): CompoundTag {
		return read(BufferedInputStream(GZIPInputStream(inputStream)), endianness)
	}

	@Throws(IOException::class)
	fun readNetworkCompressed(data: ByteArray?): CompoundTag {
		return readNetworkCompressed(data, ByteOrder.BIG_ENDIAN)
	}

	@Throws(IOException::class)
	fun readNetworkCompressed(data: ByteArray?, endianness: ByteOrder?): CompoundTag {
		return read(BufferedInputStream(GZIPInputStream(ByteArrayInputStream(data))), endianness, true)
	}

	@Throws(IOException::class)
	fun write(tag: CompoundTag?): ByteArray {
		return write(tag, ByteOrder.BIG_ENDIAN)
	}

	@Throws(IOException::class)
	fun write(tag: CompoundTag?, endianness: ByteOrder?): ByteArray {
		return write(tag, endianness, false)
	}

	@Throws(IOException::class)
	fun write(tag: CompoundTag?, endianness: ByteOrder?, network: Boolean): ByteArray {
		return write(tag as Tag?, endianness, network)
	}

	@Throws(IOException::class)
	fun write(tag: Tag?, endianness: ByteOrder?, network: Boolean): ByteArray {
		val baos: FastByteArrayOutputStream = ThreadCache.fbaos.get().reset()
		NBTOutputStream(baos, endianness, network).use({ stream ->
			Tag.writeNamedTag(tag, stream)
			return baos.toByteArray()
		})
	}

	@Throws(IOException::class)
	fun write(tags: Collection<CompoundTag?>?): ByteArray {
		return write(tags, ByteOrder.BIG_ENDIAN)
	}

	@Throws(IOException::class)
	fun write(tags: Collection<CompoundTag?>?, endianness: ByteOrder?): ByteArray {
		return write(tags, endianness, false)
	}

	@Throws(IOException::class)
	fun write(tags: Collection<CompoundTag?>, endianness: ByteOrder?, network: Boolean): ByteArray {
		val baos: FastByteArrayOutputStream = ThreadCache.fbaos.get().reset()
		NBTOutputStream(baos, endianness, network).use({ stream ->
			for (tag in tags) {
				Tag.writeNamedTag(tag, stream)
			}
			return baos.toByteArray()
		})
	}

	@Throws(IOException::class)
	fun write(tag: CompoundTag?, file: File?) {
		write(tag, file, ByteOrder.BIG_ENDIAN)
	}

	@Throws(IOException::class)
	fun write(tag: CompoundTag?, file: File?, endianness: ByteOrder?) {
		write(tag, FileOutputStream(file), endianness)
	}

	@Throws(IOException::class)
	fun write(tag: CompoundTag?, outputStream: OutputStream?) {
		write(tag, outputStream, ByteOrder.BIG_ENDIAN)
	}

	@Throws(IOException::class)
	fun write(tag: CompoundTag?, outputStream: OutputStream?, endianness: ByteOrder?) {
		write(tag, outputStream, endianness, false)
	}

	@Throws(IOException::class)
	fun write(tag: CompoundTag?, outputStream: OutputStream?, endianness: ByteOrder?, network: Boolean) {
		NBTOutputStream(outputStream, endianness, network).use({ stream -> Tag.writeNamedTag(tag, stream) })
	}

	@Throws(IOException::class)
	fun writeNetwork(tag: Tag?): ByteArray {
		val baos: FastByteArrayOutputStream = ThreadCache.fbaos.get().reset()
		NBTOutputStream(baos, ByteOrder.LITTLE_ENDIAN, true).use({ stream -> Tag.writeNamedTag(tag, stream) })
		return baos.toByteArray()
	}

	@Throws(IOException::class)
	fun writeGZIPCompressed(tag: CompoundTag?): ByteArray {
		return writeGZIPCompressed(tag, ByteOrder.BIG_ENDIAN)
	}

	@Throws(IOException::class)
	fun writeGZIPCompressed(tag: CompoundTag?, endianness: ByteOrder?): ByteArray {
		val baos: FastByteArrayOutputStream = ThreadCache.fbaos.get().reset()
		writeGZIPCompressed(tag, baos, endianness)
		return baos.toByteArray()
	}

	@Throws(IOException::class)
	fun writeGZIPCompressed(tag: CompoundTag?, outputStream: OutputStream?) {
		writeGZIPCompressed(tag, outputStream, ByteOrder.BIG_ENDIAN)
	}

	@Throws(IOException::class)
	fun writeGZIPCompressed(tag: CompoundTag?, outputStream: OutputStream?, endianness: ByteOrder?) {
		write(tag, PGZIPOutputStream(outputStream), endianness)
	}

	@Throws(IOException::class)
	fun writeNetworkGZIPCompressed(tag: CompoundTag?): ByteArray {
		return writeNetworkGZIPCompressed(tag, ByteOrder.BIG_ENDIAN)
	}

	@Throws(IOException::class)
	fun writeNetworkGZIPCompressed(tag: CompoundTag?, endianness: ByteOrder?): ByteArray {
		val baos: FastByteArrayOutputStream = ThreadCache.fbaos.get().reset()
		writeNetworkGZIPCompressed(tag, baos, endianness)
		return baos.toByteArray()
	}

	@Throws(IOException::class)
	fun writeNetworkGZIPCompressed(tag: CompoundTag?, outputStream: OutputStream?) {
		writeNetworkGZIPCompressed(tag, outputStream, ByteOrder.BIG_ENDIAN)
	}

	@Throws(IOException::class)
	fun writeNetworkGZIPCompressed(tag: CompoundTag?, outputStream: OutputStream?, endianness: ByteOrder?) {
		write(tag, PGZIPOutputStream(outputStream), endianness, true)
	}

	@Throws(IOException::class)
	fun writeZLIBCompressed(tag: CompoundTag?, outputStream: OutputStream?) {
		writeZLIBCompressed(tag, outputStream, ByteOrder.BIG_ENDIAN)
	}

	@Throws(IOException::class)
	fun writeZLIBCompressed(tag: CompoundTag?, outputStream: OutputStream?, endianness: ByteOrder?) {
		writeZLIBCompressed(tag, outputStream, Deflater.DEFAULT_COMPRESSION, endianness)
	}

	@Throws(IOException::class)
	fun writeZLIBCompressed(tag: CompoundTag?, outputStream: OutputStream?, level: Int) {
		writeZLIBCompressed(tag, outputStream, level, ByteOrder.BIG_ENDIAN)
	}

	@Throws(IOException::class)
	fun writeZLIBCompressed(tag: CompoundTag?, outputStream: OutputStream?, level: Int, endianness: ByteOrder?) {
		write(tag, DeflaterOutputStream(outputStream, Deflater(level)), endianness)
	}

	@Throws(IOException::class)
	fun safeWrite(tag: CompoundTag?, file: File) {
		val tmpFile = File(file.getAbsolutePath().toString() + "_tmp")
		if (tmpFile.exists()) {
			tmpFile.delete()
		}
		write(tag, tmpFile)
		Files.move(tmpFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
	}
}