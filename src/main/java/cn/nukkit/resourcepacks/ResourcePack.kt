package cn.nukkit.resourcepacks

import java.util.*

interface ResourcePack {
	val packName: String?
	val packId: UUID?
	val packVersion: String
	val packSize: Int
	val sha256: ByteArray?
	fun getPackChunk(off: Int, len: Int): ByteArray
}