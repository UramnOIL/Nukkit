package cn.nukkit.blockentity

import cn.nukkit.block.Block
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

class BlockEntityEnderChest(chunk: FullChunk?, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
	override val isBlockEntityValid: Boolean
		get() = this.block.id == Block.ENDER_CHEST

	override fun getName(): String? {
		return "EnderChest"
	}

	override val spawnCompound: CompoundTag?
		get() = CompoundTag()
				.putString("id", BlockEntity.Companion.ENDER_CHEST)
				.putInt("x", x.toInt())
				.putInt("y", y.toInt())
				.putInt("z", z.toInt())
}