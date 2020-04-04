package cn.nukkit.item.food

import cn.nukkit.Player
import cn.nukkit.item.ItemBucket

/**
 * Created by Snake1999 on 2016/1/21.
 * Package cn.nukkit.item.food in project nukkit.
 */
class FoodMilk : Food() {
	override fun onEatenBy(player: Player): Boolean {
		super.onEatenBy(player)
		player.getInventory().addItem(ItemBucket())
		player.removeAllEffects()
		return true
	}
}