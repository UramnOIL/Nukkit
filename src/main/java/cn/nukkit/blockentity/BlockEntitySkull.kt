package cn.nukkit.blockentity

import cn.nukkit.block.Block
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * Created by Snake1999 on 2016/2/3.
 * Package cn.nukkit.blockentity in project Nukkit.
 */
class BlockEntitySkull(chunk: FullChunk?, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
	override fun initBlockEntity() {
		if (!namedTag.contains("SkullType")) {
			namedTag.putByte("SkullType", 0)
		}
		if (!namedTag.contains("Rot")) {
			namedTag.putByte("Rot", 0)
		}
		super.initBlockEntity()
	}

	override fun saveNBT() {
		super.saveNBT()
		namedTag.remove("Creator")
	}

	override val isBlockEntityValid: Boolean
		get() = block.id == Block.SKULL_BLOCK

	override val spawnCompound: CompoundTag?
		get() = CompoundTag()
				.putString("id", BlockEntity.Companion.SKULL)
				.put("SkullType", namedTag["SkullType"])
				.putInt("x", x.toInt())
				.putInt("y", y.toInt())
				.putInt("z", z.toInt())
				.put("Rot", namedTag["Rot"])
}