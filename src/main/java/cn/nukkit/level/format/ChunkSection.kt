package cn.nukkit.level.format

import cn.nukkit.block.Block

/**
 * author: MagicDroidX
 * Nukkit Project
 */
interface ChunkSection {
	val y: Int
	fun getBlockId(x: Int, y: Int, z: Int): Int
	fun setBlockId(x: Int, y: Int, z: Int, id: Int)
	fun getBlockData(x: Int, y: Int, z: Int): Int
	fun setBlockData(x: Int, y: Int, z: Int, data: Int)
	fun getFullBlock(x: Int, y: Int, z: Int): Int
	fun getAndSetBlock(x: Int, y: Int, z: Int, block: Block?): Block?
	fun setFullBlockId(x: Int, y: Int, z: Int, fullId: Int): Boolean
	fun setBlock(x: Int, y: Int, z: Int, blockId: Int): Boolean
	fun setBlock(x: Int, y: Int, z: Int, blockId: Int, meta: Int): Boolean
	fun getBlockSkyLight(x: Int, y: Int, z: Int): Int
	fun setBlockSkyLight(x: Int, y: Int, z: Int, level: Int)
	fun getBlockLight(x: Int, y: Int, z: Int): Int
	fun setBlockLight(x: Int, y: Int, z: Int, level: Int)
	val idArray: ByteArray?
	val dataArray: ByteArray?
	val skyLightArray: ByteArray?
	val lightArray: ByteArray?
	val isEmpty: Boolean
	val bytes: ByteArray?
	fun copy(): ChunkSection?
}