package cn.nukkit.blockentity

import cn.nukkit.Player
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.BlockEntityDataPacket
import java.io.IOException
import java.nio.ByteOrder

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class BlockEntitySpawnable(chunk: FullChunk?, nbt: CompoundTag) : BlockEntity(chunk, nbt) {
	override fun initBlockEntity() {
		super.initBlockEntity()
		spawnToAll()
	}

	abstract val spawnCompound: CompoundTag?
	fun spawnTo(player: Player) {
		if (closed) {
			return
		}
		val tag = spawnCompound
		val pk = BlockEntityDataPacket()
		pk.x = x.toInt()
		pk.y = y.toInt()
		pk.z = z.toInt()
		try {
			pk.namedTag = NBTIO.write(tag, ByteOrder.LITTLE_ENDIAN, true)
		} catch (e: IOException) {
			throw RuntimeException(e)
		}
		player.dataPacket(pk)
	}

	fun spawnToAll() {
		if (closed) {
			return
		}
		for (player in getLevel().getChunkPlayers(chunk!!.x, chunk!!.z).values) {
			if (player.spawned) {
				spawnTo(player)
			}
		}
	}

	/**
	 * Called when a player updates a block entity's NBT data
	 * for example when writing on a sign.
	 *
	 * @param nbt tag
	 * @param player player
	 * @return bool indication of success, will respawn the tile to the player if false.
	 */
	open fun updateCompoundTag(nbt: CompoundTag, player: Player): Boolean {
		return false
	}
}