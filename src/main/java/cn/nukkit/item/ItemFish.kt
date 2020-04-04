package cn.nukkit.item

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class ItemFish : ItemEdible {
	@JvmOverloads
	constructor(meta: Int? = 0, count: Int = 1) : super(ItemID.Companion.RAW_FISH, meta, count, "Raw Fish") {
	}

	protected constructor(id: Int, meta: Int?, count: Int, name: String) : super(id, meta, count, name) {}
}