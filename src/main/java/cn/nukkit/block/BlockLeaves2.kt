package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.level.generator

/**
 * Created on 2015/12/1 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockLeaves2 @JvmOverloads constructor(meta: Int = 0) : BlockLeaves(meta) {
	override val name: String
		get() {
			val names = arrayOf(
					"Acacia Leaves",
					"Dark Oak Leaves"
			)
			return names[this.damage and 0x01]
		}

	override val id: Int
		get() = BlockID.Companion.LEAVES2

	override fun canDropApple(): Boolean {
		return this.damage and 0x01 != 0
	}

	protected override val sapling: Item?
		protected get() = Item.get(BlockID.Companion.SAPLING, (this.damage and 0x01) + 4)

	companion object {
		const val ACACIA = 0
		const val DARK_OAK = 1
	}
}