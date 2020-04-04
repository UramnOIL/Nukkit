package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemCarrot
import cn.nukkit.level.generator
import java.util.*

/**
 * @author Nukkit Project Team
 */
class BlockCarrot @JvmOverloads constructor(meta: Int = 0) : BlockCrops(meta) {
	override val name: String
		get() = "Carrot Block"

	override val id: Int
		get() = BlockID.Companion.CARROT_BLOCK

	override fun getDrops(item: Item): Array<Item?> {
		return if (damage >= 0x07) {
			arrayOf(
					ItemCarrot(0, Random().nextInt(3) + 1)
			)
		} else arrayOf(
				ItemCarrot()
		)
	}

	override fun toItem(): Item? {
		return ItemCarrot()
	}
}