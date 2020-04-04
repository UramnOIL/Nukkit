package cn.nukkit.blockentity

import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.utils.DyeColor

/**
 * Created by CreeperFace on 2.6.2017.
 */
class BlockEntityBed(chunk: FullChunk?, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
	var color = 0
	override fun initBlockEntity() {
		if (!namedTag.contains("color")) {
			namedTag.putByte("color", 0)
		}
		color = namedTag.getByte("color")
		super.initBlockEntity()
	}

	override val isBlockEntityValid: Boolean
		get() = level.getBlockIdAt(this.floorX, this.floorY, this.floorZ) == Item.BED_BLOCK

	override fun saveNBT() {
		super.saveNBT()
		namedTag.putByte("color", color)
	}

	override val spawnCompound: CompoundTag?
		get() = CompoundTag()
				.putString("id", BlockEntity.Companion.BED)
				.putInt("x", x.toInt())
				.putInt("y", y.toInt())
				.putInt("z", z.toInt())
				.putByte("color", color)

	val dyeColor: DyeColor
		get() = DyeColor.getByWoolData(color)
}