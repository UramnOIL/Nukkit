package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemPotato
import cn.nukkit.level.generator
import java.util.*

/**
 * Created by Pub4Game on 15.01.2016.
 */
class BlockPotato @JvmOverloads constructor(meta: Int = 0) : BlockCrops(meta) {
	override val name: String
		get() = "Potato Block"

	override val id: Int
		get() = BlockID.Companion.POTATO_BLOCK

	override fun toItem(): Item? {
		return ItemPotato()
	}

	override fun getDrops(item: Item): Array<Item?> {
		return if (damage >= 0x07) {
			arrayOf(
					ItemPotato(0, Random().nextInt(3) + 1)
			)
		} else {
			arrayOf(
					ItemPotato()
			)
		}
	}
}