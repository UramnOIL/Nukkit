package cn.nukkit.blockentity

import cn.nukkit.block.BlockRedstoneComparator
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author CreeperFace
 */
class BlockEntityComparator(chunk: FullChunk?, nbt: CompoundTag) : BlockEntity(chunk, nbt) {
	var outputSignal: Int
	override val isBlockEntityValid: Boolean
		get() = this.levelBlock is BlockRedstoneComparator

	override fun saveNBT() {
		super.saveNBT()
		namedTag.putInt("OutputSignal", outputSignal)
	}

	init {
		if (!nbt.contains("OutputSignal")) {
			nbt.putInt("OutputSignal", 0)
		}
		outputSignal = nbt.getInt("OutputSignal")
	}
}