package cn.nukkit.item

class ItemPotionLingering @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.POTION, meta, count, "Lingering Potion") {
	override val maxStackSize: Int
		get() = 1
}