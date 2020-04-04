package cn.nukkit.item.food

import cn.nukkit.Player
import cn.nukkit.block.BlockLiquid
import cn.nukkit.event.player.PlayerTeleportEvent
import cn.nukkit.item.ItemID
import cn.nukkit.level.Sound
import cn.nukkit.math.NukkitRandom
import cn.nukkit.math.Vector3

/**
 * Created by Leonidius20 on 20.08.18.
 */
class FoodChorusFruit : FoodNormal(4, 2.4f) {
	override fun onEatenBy(player: Player): Boolean {
		super.onEatenBy(player)
		// Teleportation
		val minX = player.floorX - 8
		val minY = player.floorY - 8
		val minZ = player.floorZ - 8
		val maxX = minX + 16
		val maxY = minY + 16
		val maxZ = minZ + 16
		val level = player.level ?: return false
		val random = NukkitRandom()
		for (attempts in 0..127) {
			val x = random.nextRange(minX, maxX)
			var y = random.nextRange(minY, maxY)
			val z = random.nextRange(minZ, maxZ)
			if (y < 0) continue
			while (y >= 0 && !level.getBlock(Vector3(x.toDouble(), (y + 1).toDouble(), z.toDouble())).isSolid) {
				y--
			}
			y++ // Back up to non solid
			val blockUp = level.getBlock(Vector3(x.toDouble(), (y + 1).toDouble(), z.toDouble()))
			val blockUp2 = level.getBlock(Vector3(x.toDouble(), (y + 2).toDouble(), z.toDouble()))
			if (blockUp.isSolid || blockUp is BlockLiquid ||
					blockUp2.isSolid || blockUp2 is BlockLiquid) {
				continue
			}

			// Sounds are broadcast at both source and destination
			level.addSound(player.asBlockVector3().asVector3(), Sound.MOB_ENDERMEN_PORTAL)
			player.teleport(Vector3(x + 0.5, (y + 1).toDouble(), z + 0.5), PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT)
			level.addSound(player.asBlockVector3().asVector3(), Sound.MOB_ENDERMEN_PORTAL)
			break
		}
		return true
	}

	init {
		addRelative(ItemID.Companion.CHORUS_FRUIT)
	}
}