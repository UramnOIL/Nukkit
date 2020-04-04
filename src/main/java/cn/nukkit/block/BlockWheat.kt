package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemSeedsWheat
import cn.nukkit.item.ItemWheat
import cn.nukkit.level.generator

/**
 * Created on 2015/12/2 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockWheat @JvmOverloads constructor(meta: Int = 0) : BlockCrops(meta) {
	override val name: String
		get() = "Wheat Block"

	override val id: Int
		get() = BlockID.Companion.WHEAT_BLOCK

	override fun toItem(): Item? {
		return ItemSeedsWheat()
	}

	override fun getDrops(item: Item): Array<Item?> {
		return if (this.damage >= 0x07) {
			arrayOf(
					ItemWheat(),
					ItemSeedsWheat(0, (4.0 * Math.random()).toInt())
			)
		} else {
			arrayOf(
					ItemSeedsWheat()
			)
		}
	}
}