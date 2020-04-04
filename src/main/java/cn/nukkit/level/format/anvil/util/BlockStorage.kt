package cn.nukkit.level.format.anvil.util

import com.google.common.base.Preconditions
import java.util.*

class BlockStorage {
	private val blockIds: ByteArray
	private val blockData: NibbleArray?

	constructor() {
		blockIds = ByteArray(SECTION_SIZE)
		blockData = NibbleArray(SECTION_SIZE)
	}

	private constructor(blockIds: ByteArray, blockData: NibbleArray?) {
		this.blockIds = blockIds
		this.blockData = blockData
	}

	fun getBlockData(x: Int, y: Int, z: Int): Int {
		return blockData!![getIndex(x, y, z)] and 0xf
	}

	fun getBlockId(x: Int, y: Int, z: Int): Int {
		return blockIds[getIndex(x, y, z)] and 0xFF
	}

	fun setBlockId(x: Int, y: Int, z: Int, id: Int) {
		blockIds[getIndex(x, y, z)] = (id and 0xff).toByte()
	}

	fun setBlockData(x: Int, y: Int, z: Int, data: Int) {
		blockData!![getIndex(x, y, z)] = data.toByte()
	}

	fun getFullBlock(x: Int, y: Int, z: Int): Int {
		return getFullBlock(getIndex(x, y, z))
	}

	fun setFullBlock(x: Int, y: Int, z: Int, value: Int) {
		this.setFullBlock(getIndex(x, y, z), value.toShort())
	}

	fun getAndSetFullBlock(x: Int, y: Int, z: Int, value: Int): Int {
		return getAndSetFullBlock(getIndex(x, y, z), value.toShort())
	}

	private fun getAndSetFullBlock(index: Int, value: Short): Int {
		Preconditions.checkArgument(value < 0xfff, "Invalid full block")
		val oldBlock = blockIds[index]
		val oldData = blockData!![index]
		val newBlock = (value and 0xff0 shr 4) as Byte
		val newData = (value and 0xf) as Byte
		if (oldBlock != newBlock) {
			blockIds[index] = newBlock
		}
		if (oldData != newData) {
			blockData[index] = newData
		}
		return oldBlock and 0xff shl 4 or oldData
	}

	private fun getFullBlock(index: Int): Int {
		val block = blockIds[index]
		val data = blockData!![index]
		return block and 0xff shl 4 or data
	}

	private fun setFullBlock(index: Int, value: Short) {
		Preconditions.checkArgument(value < 0xfff, "Invalid full block")
		val block = (value and 0xff0 shr 4) as Byte
		val data = (value and 0xf) as Byte
		blockIds[index] = block
		blockData!![index] = data
	}

	fun getBlockIds(): ByteArray {
		return Arrays.copyOf(blockIds, blockIds.size)
	}

	fun getBlockData(): ByteArray? {
		return blockData.getData()
	}

	fun copy(): BlockStorage {
		return BlockStorage(blockIds.clone(), blockData!!.copy())
	}

	companion object {
		private const val SECTION_SIZE = 4096
		private fun getIndex(x: Int, y: Int, z: Int): Int {
			val index = (x shl 8) + (z shl 4) + y // XZY = Bedrock format
			Preconditions.checkArgument(index >= 0 && index < SECTION_SIZE, "Invalid index")
			return index
		}
	}
}