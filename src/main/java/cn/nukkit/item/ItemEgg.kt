package cn.nukkit.item

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemEgg @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ProjectileItem(ItemID.Companion.EGG, meta, count, "Egg") {
	override val projectileEntityType: String
		get() = "Egg"

	override val throwForce: Float
		get() = 1.5f

	override val maxStackSize: Int
		get() = 16
}