package cn.nukkit.item.food

import cn.nukkit.Player
import cn.nukkit.item.ItemBowl

/**
 * Created by Snake1999 on 2016/1/14.
 * Package cn.nukkit.item.food in project nukkit.
 */
class FoodInBowl(restoreFood: Int, restoreSaturation: Float) : Food() {
	override fun onEatenBy(player: Player): Boolean {
		super.onEatenBy(player)
		player.getInventory().addItem(ItemBowl())
		return true
	}

	init {
		setRestoreFood(restoreFood)
		setRestoreSaturation(restoreSaturation)
	}
}