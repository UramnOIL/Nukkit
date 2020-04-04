package cn.nukkit.item

/**
 * Created by lion on 21.03.17.
 */
class ItemCarrotOnAStick @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemTool(ItemID.Companion.CARROT_ON_A_STICK, meta, count, "Carrot on a stick") {
	override val maxStackSize: Int
		get() = 1
}