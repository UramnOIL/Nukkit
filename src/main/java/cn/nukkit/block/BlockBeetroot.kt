package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemSeedsBeetroot
import cn.nukkit.level.generator

/**
 * Created on 2015/11/22 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockBeetroot @JvmOverloads constructor(meta: Int = 0) : BlockCrops(meta) {
	override val id: Int
		get() = BlockID.Companion.BEETROOT_BLOCK

	override val name: String
		get() = "Beetroot Block"

	override fun toItem(): Item? {
		return ItemSeedsBeetroot()
	}

	override fun getDrops(item: Item): Array<Item?> {
		return if (this.damage >= 0x07) {
			arrayOf(
					Item.get(Item.BEETROOT, 0, 1),
					Item.get(Item.BEETROOT_SEEDS, 0, (4.0 * Math.random()).toInt())
			)
		} else {
			arrayOf(
					Item.get(Item.BEETROOT_SEEDS, 0, 1)
			)
		}
	}
}