package cn.nukkit.item

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemElytra @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemArmor(ItemID.Companion.ELYTRA, meta, count, "Elytra") {
	override val maxDurability: Int
		get() = 431

	override val isArmor: Boolean
		get() = true

	override val isChestplate: Boolean
		get() = true
}