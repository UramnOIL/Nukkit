package cn.nukkit.item

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemBook @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.BOOK, meta, count, "Book") {
	override val enchantAbility: Int
		get() = 1
}