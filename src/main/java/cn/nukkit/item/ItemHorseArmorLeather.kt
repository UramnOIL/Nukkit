package cn.nukkit.item

class ItemHorseArmorLeather @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.LEATHER_HORSE_ARMOR, meta, count, "Leather Horse Armor") {
	override val maxStackSize: Int
		get() = 1
}