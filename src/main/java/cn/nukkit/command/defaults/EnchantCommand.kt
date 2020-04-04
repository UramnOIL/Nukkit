package cn.nukkit.command.defaults

import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.utils.TextFormat
import kotlin.collections.set

/**
 * Created by Pub4Game on 23.01.2016.
 */
class EnchantCommand(name: String) : VanillaCommand(name, "%nukkit.command.enchant.description", "%commands.enchant.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (args.size < 2) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return true
		}
		val player = sender.server.getPlayer(args[0])
		if (player == null) {
			sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.player.notFound"))
			return true
		}
		val enchantId: Int
		val enchantLevel: Int
		try {
			enchantId = getIdByName(args[1])
			enchantLevel = if (args.size == 3) args[2]!!.toInt() else 1
		} catch (e: NumberFormatException) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return true
		}
		val enchantment = Enchantment.getEnchantment(enchantId)
		if (enchantment == null) {
			sender.sendMessage(TranslationContainer("commands.enchant.notFound", enchantId.toString()))
			return true
		}
		enchantment.level = enchantLevel
		val item = player.inventory.itemInHand
		if (item.id <= 0) {
			sender.sendMessage(TranslationContainer("commands.enchant.noItem"))
			return true
		}
		item.addEnchantment(enchantment)
		player.inventory.itemInHand = item
		broadcastCommandMessage(sender, TranslationContainer("%commands.enchant.success"))
		return true
	}

	@Throws(NumberFormatException::class)
	fun getIdByName(value: String?): Int {
		return when (value) {
			"protection" -> 0
			"fire_protection" -> 1
			"feather_falling" -> 2
			"blast_protection" -> 3
			"projectile_projection" -> 4
			"thorns" -> 5
			"respiration" -> 6
			"aqua_affinity" -> 7
			"depth_strider" -> 8
			"sharpness" -> 9
			"smite" -> 10
			"bane_of_arthropods" -> 11
			"knockback" -> 12
			"fire_aspect" -> 13
			"looting" -> 14
			"efficiency" -> 15
			"silk_touch" -> 16
			"durability" -> 17
			"fortune" -> 18
			"power" -> 19
			"punch" -> 20
			"flame" -> 21
			"infinity" -> 22
			"luck_of_the_sea" -> 23
			"lure" -> 24
			"frost_walker" -> 25
			"mending" -> 26
			"binding_curse" -> 27
			"vanishing_curse" -> 28
			"impaling" -> 29
			"loyality" -> 30
			"riptide" -> 31
			"channeling" -> 32
			else -> value!!.toInt()
		}
	}

	init {
		permission = "nukkit.command.enchant"
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, false),
				CommandParameter("enchantment ID", CommandParamType.INT, false),
				CommandParameter("level", CommandParamType.INT, true)
		)
		commandParameters["byName"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, false),
				CommandParameter("id", false, CommandParameter.Companion.ENUM_TYPE_ENCHANTMENT_LIST),
				CommandParameter("level", CommandParamType.INT, true)
		)
	}
}