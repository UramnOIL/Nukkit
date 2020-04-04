package cn.nukkit.item

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.block.BlockID
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace

class ItemGlassBottle @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.GLASS_BOTTLE, meta, count, "Glass Bottle") {
	override fun canBeActivated(): Boolean {
		return true
	}

	override fun onActivate(level: Level, player: Player, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double): Boolean {
		if (target.id == BlockID.WATER || target.id == BlockID.STILL_WATER) {
			val potion: Item = ItemPotion()
			if (count == 1) {
				player.getInventory().setItemInHand(potion)
			} else if (count > 1) {
				count--
				player.getInventory().setItemInHand(this)
				if (player.getInventory().canAddItem(potion)) {
					player.getInventory().addItem(potion)
				} else {
					player.level.dropItem(player.add(0.0, 1.3, 0.0), potion, player.directionVector.multiply(0.4))
				}
			}
		}
		return false
	}
}