package cn.nukkit.item

class ItemHorseArmorIron @JvmOverloads constructor(meta: Int? = 0, count: Int = 0) : Item(ItemID.Companion.IRON_HORSE_ARMOR, meta, count, "Iron Horse Armor") {
	override val maxStackSize: Int
		get() = 1
}