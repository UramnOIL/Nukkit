package cn.nukkit.item

/**
 * Created by Leonidius20 on 18.08.18.
 */
class ItemSpiderEyeFermented(meta: Int?, count: Int) : Item(ItemID.Companion.FERMENTED_SPIDER_EYE, meta, count, "Fermented Spider Eye") {
	@JvmOverloads
	constructor(meta: Int? = 0) : this(0, 1) {
	}
}