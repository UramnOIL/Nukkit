package cn.nukkit

import cn.nukkit.utils.TextFormat
import java.util.*

/**
 * Created by CreeperFace on 9. 11. 2016.
 */
class Achievement(val message: String, vararg requires: String) {
	@kotlin.jvm.JvmField
    val requires: Array<String>

	fun broadcast(player: Player) {
		val translation = Server.instance.language.translateString("chat.type.achievement", player.getDisplayName(), TextFormat.GREEN.toString() + message, null)
		if (Server.instance.getPropertyBoolean("announce-player-achievements", true)) {
			Server.instance.broadcastMessage(translation)
		} else {
			player.sendMessage(translation)
		}
	}

	companion object {
		@kotlin.jvm.JvmField
        val achievements: HashMap<String, Achievement> = object : HashMap<String?, Achievement?>() {
			init {
				put("mineWood", Achievement("Getting Wood"))
				put("buildWorkBench", Achievement("Benchmarking", "mineWood"))
				put("buildPickaxe", Achievement("Time to Mine!", "buildWorkBench"))
				put("buildFurnace", Achievement("Hot Topic", "buildPickaxe"))
				put("acquireIron", Achievement("Acquire hardware", "buildFurnace"))
				put("buildHoe", Achievement("Time to Farm!", "buildWorkBench"))
				put("makeBread", Achievement("Bake Bread", "buildHoe"))
				put("bakeCake", Achievement("The Lie", "buildHoe"))
				put("buildBetterPickaxe", Achievement("Getting an Upgrade", "buildPickaxe"))
				put("buildSword", Achievement("Time to Strike!", "buildWorkBench"))
				put("diamonds", Achievement("DIAMONDS!", "acquireIron"))
			}
		}

		@kotlin.jvm.JvmStatic
        fun broadcast(player: Player, achievementId: String): Boolean {
			if (!achievements.containsKey(achievementId)) {
				return false
			}
			val translation = Server.instance.language.translateString("chat.type.achievement", player.getDisplayName(), TextFormat.GREEN.toString() + achievements[achievementId]!!.message + TextFormat.RESET)
			if (Server.instance.getPropertyBoolean("announce-player-achievements", true)) {
				Server.instance.broadcastMessage(translation)
			} else {
				player.sendMessage(translation)
			}
			return true
		}

		fun add(name: String, achievement: Achievement): Boolean {
			if (achievements.containsKey(name)) {
				return false
			}
			achievements[name] = achievement
			return true
		}
	}

	init {
		this.requires = requires
	}
}