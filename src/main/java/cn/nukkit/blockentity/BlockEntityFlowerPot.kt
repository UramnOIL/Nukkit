package cn.nukkit.blockentity

import cn.nukkit.block.Block
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * Created by Snake1999 on 2016/2/4.
 * Package cn.nukkit.blockentity in project Nukkit.
 */
class BlockEntityFlowerPot(chunk: FullChunk?, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
	override fun initBlockEntity() {
		if (!namedTag.contains("item")) {
			namedTag.putShort("item", 0)
		}
		if (!namedTag.contains("data")) {
			if (namedTag.contains("mData")) {
				namedTag.putInt("data", namedTag.getInt("mData"))
				namedTag.remove("mData")
			} else {
				namedTag.putInt("data", 0)
			}
		}
		super.initBlockEntity()
	}

	override val isBlockEntityValid: Boolean
		get() {
			val blockID = block.id
			return blockID == Block.FLOWER_POT_BLOCK
		}

	override val spawnCompound: CompoundTag?
		get() {
			val tag = CompoundTag()
					.putString("id", BlockEntity.Companion.FLOWER_POT)
					.putInt("x", x.toInt())
					.putInt("y", y.toInt())
					.putInt("z", z.toInt())
			val item = namedTag.getShort("item")
			if (item != Block.AIR) {
				tag.putShort("item", namedTag.getShort("item"))
						.putInt("mData", namedTag.getInt("data"))
			}
			return tag
		}
}