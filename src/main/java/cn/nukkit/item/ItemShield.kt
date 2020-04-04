package cn.nukkit.item

class ItemShield @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.SHIELD, meta, count, "Shield") {
	override val maxStackSize: Int
		get() = 1
}