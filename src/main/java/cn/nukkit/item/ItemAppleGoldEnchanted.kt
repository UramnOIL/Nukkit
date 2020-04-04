package cn.nukkit.item

import cn.nukkit.Player
import cn.nukkit.math.Vector3

/**
 * Created by Snake1999 on 2016/1/14.
 * Package cn.nukkit.item in project nukkit.
 */
class ItemAppleGoldEnchanted @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemEdible(ItemID.Companion.GOLDEN_APPLE_ENCHANTED, meta, count, "Enchanted Golden Apple") {
	override fun onClickAir(player: Player, directionVector: Vector3): Boolean {
		return true
	}
}