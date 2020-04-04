package cn.nukkit.blockentity

import cn.nukkit.block.Block
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.utils.BannerPattern
import cn.nukkit.utils.DyeColor

class BlockEntityBanner(chunk: FullChunk?, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
	var color = 0
	override fun initBlockEntity() {
		if (!namedTag.contains("color")) {
			namedTag.putByte("color", 0)
		}
		color = namedTag.getByte("color")
		super.initBlockEntity()
	}

	override val isBlockEntityValid: Boolean
		get() = this.block.id == Block.WALL_BANNER || this.block.id == Block.STANDING_BANNER

	override fun saveNBT() {
		super.saveNBT()
		namedTag.putByte("color", color)
	}

	override fun getName(): String? {
		return "Banner"
	}

	val baseColor: Int
		get() = namedTag.getInt("Base")

	fun setBaseColor(color: DyeColor) {
		namedTag.putInt("Base", color.dyeData and 0x0f)
	}

	var type: Int
		get() = namedTag.getInt("Type")
		set(type) {
			namedTag.putInt("Type", type)
		}

	fun addPattern(pattern: BannerPattern) {
		val patterns = namedTag.getList("Patterns", CompoundTag::class.java)
		patterns.add(CompoundTag("").putInt("Color", pattern.color.dyeData and 0x0f).putString("Pattern", pattern.type.getName()))
		namedTag.putList(patterns)
	}

	fun getPattern(index: Int): BannerPattern {
		return BannerPattern.fromCompoundTag(if (namedTag.getList("Patterns").size() > index && index >= 0) namedTag.getList("Patterns", CompoundTag::class.java)[index] else CompoundTag())
	}

	fun removePattern(index: Int) {
		val patterns = namedTag.getList("Patterns", CompoundTag::class.java)
		if (patterns.size() > index && index >= 0) {
			patterns.remove(index)
		}
	}

	val patternsSize: Int
		get() = namedTag.getList("Patterns").size()

	override val spawnCompound: CompoundTag?
		get() = BlockEntity.Companion.getDefaultCompound(this, BlockEntity.Companion.BANNER)
				.putInt("Base", baseColor)
				.putList(namedTag.getList("Patterns"))
				.putInt("Type", type)
				.putByte("color", color)

	val dyeColor: DyeColor
		get() = DyeColor.getByWoolData(color)
}