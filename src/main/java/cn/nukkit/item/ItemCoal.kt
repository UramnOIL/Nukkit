package cn.nukkit.item

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemCoal @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.COAL, meta, count, "Coal") {
	init {
		if (this.meta == 1) {
			name = "Charcoal"
		}
	}
}