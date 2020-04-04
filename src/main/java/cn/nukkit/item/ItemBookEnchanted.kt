package cn.nukkit.item

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemBookEnchanted @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.ENCHANTED_BOOK, meta, count, "Enchanted Book") {
	override val maxStackSize: Int
		get() = 1
}