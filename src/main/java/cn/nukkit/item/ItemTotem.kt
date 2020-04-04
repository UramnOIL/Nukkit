package cn.nukkit.item

class ItemTotem @JvmOverloads constructor(meta: Int?, count: Int = 1) : Item(ItemID.Companion.TOTEM, meta, count, "Totem of Undying") {
	override val maxStackSize: Int
		get() = 1
}