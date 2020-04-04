package cn.nukkit.item

import cn.nukkit.block.Block

/**
 * Created by Snake1999 on 2016/2/3.
 * Package cn.nukkit.item in project Nukkit.
 */
class ItemSkull @JvmOverloads constructor(meta: Int = 0, count: Int = 1) : Item(ItemID.Companion.SKULL, meta, count, getItemSkullName(meta)) {
	companion object {
		const val SKELETON_SKULL = 0
		const val WITHER_SKELETON_SKULL = 1
		const val ZOMBIE_HEAD = 2
		const val HEAD = 3
		const val CREEPER_HEAD = 4
		const val DRAGON_HEAD = 5
		fun getItemSkullName(meta: Int): String {
			return when (meta) {
				1 -> "Wither Skeleton Skull"
				2 -> "Zombie Head"
				3 -> "Head"
				4 -> "Creeper Head"
				5 -> "Dragon Head"
				0 -> "Skeleton Skull"
				else -> "Skeleton Skull"
			}
		}
	}

	init {
		block = Block[Block.SKULL_BLOCK]
	}
}