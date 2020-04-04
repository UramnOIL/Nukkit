package cn.nukkit.item

class ItemHorseArmorGold @JvmOverloads constructor(meta: Int? = 0, count: Int = 0) : Item(ItemID.Companion.GOLD_HORSE_ARMOR, meta, count, "Gold Horse Armor") {
	override val maxStackSize: Int
		get() = 1
}