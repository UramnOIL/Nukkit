package cn.nukkit.blockentity

import cn.nukkit.block.Block
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockEntityEnchantTable(chunk: FullChunk?, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt), BlockEntityNameable {
	override val isBlockEntityValid: Boolean
		get() = block.id == Block.ENCHANT_TABLE

	override fun getName(): String? {
		return if (hasName()) namedTag.getString("CustomName") else "Enchanting Table"
	}

	override fun hasName(): Boolean {
		return namedTag.contains("CustomName")
	}

	override fun setName(name: String?) {
		if (name == null || name == "") {
			namedTag.remove("CustomName")
			return
		}
		namedTag.putString("CustomName", name)
	}

	override val spawnCompound: CompoundTag?
		get() {
			val c = CompoundTag()
					.putString("id", BlockEntity.Companion.ENCHANT_TABLE)
					.putInt("x", x.toInt())
					.putInt("y", y.toInt())
					.putInt("z", z.toInt())
			if (hasName()) {
				c.put("CustomName", namedTag["CustomName"])
			}
			return c
		}
}