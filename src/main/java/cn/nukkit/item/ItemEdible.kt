package cn.nukkit.item

import cn.nukkit.Player
import cn.nukkit.event.player.PlayerItemConsumeEvent
import cn.nukkit.item.food.Food
import cn.nukkit.math.Vector3

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class ItemEdible : Item {
	constructor(id: Int, meta: Int?, count: Int, name: String) : super(id, meta, count, name) {}
	constructor(id: Int) : super(id) {}
	constructor(id: Int, meta: Int?) : super(id, meta) {}
	constructor(id: Int, meta: Int?, count: Int) : super(id, meta, count) {}

	override fun onClickAir(player: Player, directionVector: Vector3): Boolean {
		if (player.foodData!!.level < player.foodData!!.maxLevel || player.isCreative) {
			return true
		}
		player.foodData!!.sendFoodLevel()
		return false
	}

	override fun onUse(player: Player, ticksUsed: Int): Boolean {
		val consumeEvent = PlayerItemConsumeEvent(player, this)
		player.getServer().pluginManager.callEvent(consumeEvent)
		if (consumeEvent.isCancelled) {
			player.getInventory().sendContents(player)
			return false
		}
		val food: Food = Food.Companion.getByRelative(this)
		if (player.isSurvival && food != null && food.eatenBy(player)) {
			--count
			player.getInventory().setItemInHand(this)
		}
		return true
	}
}