package cn.nukkit.item

import cn.nukkit.Player
import cn.nukkit.math.Vector3

/**
 * Created by Leonidius20 on 20.08.18.
 */
class ItemChorusFruit @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemEdible(ItemID.Companion.CHORUS_FRUIT, meta, count, "Chorus Fruit") {
	override fun onClickAir(player: Player, directionVector: Vector3): Boolean {
		return player.getServer().tick - player.lastChorusFruitTeleport >= 20
	}

	override fun onUse(player: Player, ticksUsed: Int): Boolean {
		val successful = super.onUse(player, ticksUsed)
		if (successful) {
			player.onChorusFruitTeleport()
		}
		return successful
	}
}