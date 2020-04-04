package cn.nukkit.blockentity

import cn.nukkit.block.Block
import cn.nukkit.item.Item
import cn.nukkit.item.ItemRecord
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.LevelSoundEventPacket
import java.util.*

/**
 * @author CreeperFace
 */
class BlockEntityJukebox(chunk: FullChunk?, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
	private var recordItem: Item? = null
	override fun initBlockEntity() {
		if (namedTag.contains("RecordItem")) {
			recordItem = NBTIO.getItemHelper(namedTag.getCompound("RecordItem"))
		} else {
			recordItem = Item.get(0)
		}
		super.initBlockEntity()
	}

	override val isBlockEntityValid: Boolean
		get() = getLevel().getBlockIdAt(floorX, floorY, floorZ) == Block.JUKEBOX

	fun setRecordItem(recordItem: Item?) {
		Objects.requireNonNull(recordItem, "Record item cannot be null")
		this.recordItem = recordItem
	}

	fun getRecordItem(): Item? {
		return recordItem
	}

	fun play() {
		if (recordItem is ItemRecord) {
			when (recordItem.getId()) {
				Item.RECORD_13 -> getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_RECORD_13)
				Item.RECORD_CAT -> getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_RECORD_CAT)
				Item.RECORD_BLOCKS -> getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_RECORD_BLOCKS)
				Item.RECORD_CHIRP -> getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_RECORD_CHIRP)
				Item.RECORD_FAR -> getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_RECORD_FAR)
				Item.RECORD_MALL -> getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_RECORD_MALL)
				Item.RECORD_MELLOHI -> getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_RECORD_MELLOHI)
				Item.RECORD_STAL -> getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_RECORD_STAL)
				Item.RECORD_STRAD -> getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_RECORD_STRAD)
				Item.RECORD_WARD -> getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_RECORD_WARD)
				Item.RECORD_11 -> getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_RECORD_11)
				Item.RECORD_WAIT -> getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_RECORD_WAIT)
			}
		}
	}

	fun stop() {
		getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_STOP_RECORD)
	}

	fun dropItem() {
		if (recordItem!!.id != 0) {
			stop()
			level.dropItem(this.up(), recordItem)
			recordItem = Item.get(0)
		}
	}

	override fun saveNBT() {
		super.saveNBT()
		namedTag.putCompound("RecordItem", NBTIO.putItemHelper(recordItem))
	}

	override val spawnCompound: CompoundTag?
		get() = BlockEntity.Companion.getDefaultCompound(this, BlockEntity.Companion.JUKEBOX)
				.putCompound("RecordItem", NBTIO.putItemHelper(recordItem))
}