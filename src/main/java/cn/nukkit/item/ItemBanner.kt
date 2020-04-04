package cn.nukkit.item

import cn.nukkit.block.Block
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.IntTag
import cn.nukkit.utils.BannerPattern
import cn.nukkit.utils.DyeColor

/**
 * Created by PetteriM1
 */
class ItemBanner @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.BANNER, meta, count, "Banner") {
	override val maxStackSize: Int
		get() = 16

	val baseColor: Int
		get() = this.namedTag.getInt("Base")

	fun setBaseColor(color: DyeColor) {
		val tag = if (hasCompoundTag()) this.namedTag else CompoundTag()
		tag!!.putInt("Base", color.dyeData and 0x0f)
		this.damage = color.dyeData and 0x0f
		this.namedTag = tag
	}

	var type: Int
		get() = this.namedTag.getInt("Type")
		set(type) {
			val tag = if (hasCompoundTag()) this.namedTag else CompoundTag()
			tag!!.putInt("Type", type)
			this.namedTag = tag
		}

	fun addPattern(pattern: BannerPattern) {
		val tag = if (hasCompoundTag()) this.namedTag else CompoundTag()
		val patterns = tag!!.getList("Patterns", CompoundTag::class.java)
		patterns.add(CompoundTag("").putInt("Color", pattern.color.dyeData and 0x0f).putString("Pattern", pattern.type.getName()))
		tag.putList(patterns)
		this.namedTag = tag
	}

	fun getPattern(index: Int): BannerPattern {
		val tag = if (hasCompoundTag()) this.namedTag else CompoundTag()
		return BannerPattern.fromCompoundTag(if (tag!!.getList("Patterns").size() > index && index >= 0) tag.getList("Patterns", CompoundTag::class.java)[index] else CompoundTag())
	}

	fun removePattern(index: Int) {
		val tag = if (hasCompoundTag()) this.namedTag else CompoundTag()
		val patterns = tag!!.getList("Patterns", CompoundTag::class.java)
		if (patterns.size() > index && index >= 0) {
			patterns.remove(index)
		}
		this.namedTag = tag
	}

	val patternsSize: Int
		get() = (if (hasCompoundTag()) this.namedTag else CompoundTag()).getList("Patterns").size()

	fun correctNBT() {
		val tag = if (this.namedTag != null) this.namedTag else CompoundTag()
		if (!tag!!.contains("Base") || tag["Base"] !is IntTag) {
			tag.putInt("Base", meta)
		}
		this.namedTag = tag
	}

	init {
		block = Block[Block.STANDING_BANNER]
		//this.correctNBT(); // this was added by KCodeYT in pr #1019 but it seems to be causing crashes when crafting banners
	}
}