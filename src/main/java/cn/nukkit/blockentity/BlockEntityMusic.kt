package cn.nukkit.blockentity

import cn.nukkit.block.Block
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

class BlockEntityMusic(chunk: FullChunk?, nbt: CompoundTag) : BlockEntity(chunk, nbt) {
	override fun initBlockEntity() {
		if (!namedTag.contains("note")) {
			namedTag.putByte("note", 0)
		}
		if (!namedTag.contains("powered")) {
			namedTag.putBoolean("powered", false)
		}
		super.initBlockEntity()
	}

	override val isBlockEntityValid: Boolean
		get() = this.block.id == Block.NOTEBLOCK

	fun changePitch() {
		namedTag.putByte("note", (namedTag.getByte("note") + 1) % 25)
	}

	val pitch: Int
		get() = namedTag.getByte("note")

	var isPowered: Boolean
		get() = namedTag.getBoolean("powered")
		set(powered) {
			namedTag.putBoolean("powered", powered)
		}
}