package cn.nukkit.blockentity

import cn.nukkit.block.Block
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.utils.BlockColor

/**
 * author: CreeperFace
 * Nukkit Project
 */
class BlockEntityCauldron(chunk: FullChunk?, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
	override fun initBlockEntity() {
		if (!namedTag.contains("PotionId")) {
			namedTag.putShort("PotionId", 0xffff)
		}
		if (!namedTag.contains("SplashPotion")) {
			namedTag.putByte("SplashPotion", 0)
		}
		super.initBlockEntity()
	}

	var potionId: Int
		get() = namedTag.getShort("PotionId")
		set(potionId) {
			namedTag.putShort("PotionId", potionId)
			spawnToAll()
		}

	fun hasPotion(): Boolean {
		return potionId != 0xffff
	}

	var isSplashPotion: Boolean
		get() = namedTag.getByte("SplashPotion") > 0
		set(value) {
			namedTag.putByte("SplashPotion", if (value) 1 else 0)
		}

	val customColor: BlockColor?
		get() {
			if (isCustomColor()) {
				val color = namedTag.getInt("CustomColor")
				val red = color shr 16 and 0xff
				val green = color shr 8 and 0xff
				val blue = color and 0xff
				return BlockColor(red, green, blue)
			}
			return null
		}

	fun isCustomColor(): Boolean {
		return namedTag.contains("CustomColor")
	}

	fun setCustomColor(color: BlockColor) {
		setCustomColor(color.red, color.green, color.blue)
	}

	fun setCustomColor(r: Int, g: Int, b: Int) {
		val color = r shl 16 or (g shl 8) or b and 0xffffff
		namedTag.putInt("CustomColor", color)
		spawnToAll()
	}

	fun clearCustomColor() {
		namedTag.remove("CustomColor")
		spawnToAll()
	}

	override val isBlockEntityValid: Boolean
		get() = block.id == Block.CAULDRON_BLOCK

	override val spawnCompound: CompoundTag?
		get() = CompoundTag()
				.putString("id", BlockEntity.Companion.CAULDRON)
				.putInt("x", x.toInt())
				.putInt("y", y.toInt())
				.putInt("z", z.toInt())
				.putShort("PotionId", namedTag.getShort("PotionId"))
				.putByte("SplashPotion", namedTag.getByte("SplashPotion"))
}