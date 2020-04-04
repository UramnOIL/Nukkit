package cn.nukkit.item

class ItemHorseArmorDiamond @JvmOverloads constructor(meta: Int? = 0, count: Int = 0) : Item(ItemID.Companion.DIAMOND_HORSE_ARMOR, meta, count, "Diamond Horse Armor") {
	override val maxStackSize: Int
		get() = 1
}