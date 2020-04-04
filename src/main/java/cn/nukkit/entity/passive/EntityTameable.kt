package cn.nukkit.entity.passive

import cn.nukkit.Player
import cn.nukkit.entity.EntityOwnable
import cn.nukkit.entity.data.ByteEntityData
import cn.nukkit.entity.data.StringEntityData
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * Author: BeYkeRYkt
 * Nukkit Project
 */
abstract class EntityTameable(chunk: FullChunk?, nbt: CompoundTag?) : EntityAnimal(chunk, nbt), EntityOwnable {
	override fun initEntity() {
		super.initEntity()
		if (getDataProperty(DATA_TAMED_FLAG) == null) {
			setDataProperty(ByteEntityData(DATA_TAMED_FLAG, 0.toByte()))
		}
		if (getDataProperty(DATA_OWNER_NAME) == null) {
			setDataProperty(StringEntityData(DATA_OWNER_NAME, ""))
		}
		var ownerName = ""
		if (namedTag != null) {
			if (namedTag!!.contains("Owner")) {
				ownerName = namedTag!!.getString("Owner")
			}
			if (ownerName.length > 0) {
				setOwnerName(ownerName)
				this.tamed = true
			}
			this.sitting = namedTag!!.getBoolean("Sitting")
		}
	}

	override fun saveNBT() {
		super.saveNBT()
		if (this.ownerName == null) {
			namedTag!!.putString("Owner", "")
		} else {
			namedTag!!.putString("Owner", ownerName)
		}
		namedTag!!.putBoolean("Sitting", isSitting)
	}

	override fun getOwnerName(): String? {
		return getDataPropertyString(DATA_OWNER_NAME)
	}

	override fun setOwnerName(playerName: String) {
		setDataProperty(StringEntityData(DATA_OWNER_NAME, playerName))
	}

	override val owner: Player?
		get() = getServer().getPlayer(ownerName)

	override val name: String?
		get() = nameTag

	// ?
	var isTamed: Boolean
		get() = getDataPropertyByte(DATA_TAMED_FLAG) and 4 != 0
		set(flag) {
			val `var` = getDataPropertyByte(DATA_TAMED_FLAG) // ?
			if (flag) {
				setDataProperty(ByteEntityData(DATA_TAMED_FLAG, (`var` or 4).toByte()))
			} else {
				setDataProperty(ByteEntityData(DATA_TAMED_FLAG, (`var` and -5).toByte()))
			}
		}

	// ?
	var isSitting: Boolean
		get() = getDataPropertyByte(DATA_TAMED_FLAG) and 1 != 0
		set(flag) {
			val `var` = getDataPropertyByte(DATA_TAMED_FLAG) // ?
			if (flag) {
				setDataProperty(ByteEntityData(DATA_TAMED_FLAG, (`var` or 1).toByte()))
			} else {
				setDataProperty(ByteEntityData(DATA_TAMED_FLAG, (`var` and -2).toByte()))
			}
		}

	companion object {
		const val DATA_TAMED_FLAG = 16
		const val DATA_OWNER_NAME = 17
	}
}