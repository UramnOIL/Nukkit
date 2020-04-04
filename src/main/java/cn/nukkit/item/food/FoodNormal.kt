package cn.nukkit.item.food

/**
 * Created by Snake1999 on 2016/1/13.
 * Package cn.nukkit.item.food in project nukkit.
 */
open class FoodNormal(restoreFood: Int, restoreSaturation: Float) : Food() {
	init {
		setRestoreFood(restoreFood)
		setRestoreSaturation(restoreSaturation)
	}
}