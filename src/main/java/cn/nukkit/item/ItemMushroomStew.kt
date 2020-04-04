package cn.nukkit.item

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemMushroomStew @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemEdible(ItemID.Companion.MUSHROOM_STEW, 0, count, "Mushroom Stew") {
	override val maxStackSize: Int
		get() = 1
}