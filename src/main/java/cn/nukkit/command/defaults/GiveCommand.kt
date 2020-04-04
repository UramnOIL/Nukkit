package cn.nukkit.command.defaults

import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.item.Item
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.utils.TextFormat
import kotlin.collections.set

/**
 * Created on 2015/12/9 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class GiveCommand(name: String) : VanillaCommand(name, "%nukkit.command.give.description", "%nukkit.command.give.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (args.size < 2) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return true
		}
		val player = sender.server.getPlayer(args[0])
		val item: Item
		item = try {
			Item.fromString(args[1])
		} catch (e: Exception) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return true
		}
		try {
			item.setCount(args[2]!!.toInt())
		} catch (e: Exception) {
			item.setCount(item.maxStackSize)
		}
		if (player != null) {
			if (item.id == 0) {
				sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.give.item.notFound", args[1]))
				return true
			}
			player.inventory.addItem(item.clone())
		} else {
			sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.player.notFound"))
			return true
		}
		broadcastCommandMessage(sender, TranslationContainer(
				"%commands.give.success",
				item.name + " (" + item.id + ":" + item.damage + ")", item.getCount().toString(),
				player.getName()))
		return true
	}

	init {
		permission = "nukkit.command.give"
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, false),
				CommandParameter("item", false, CommandParameter.Companion.ENUM_TYPE_ITEM_LIST),
				CommandParameter("amount", CommandParamType.INT, true),
				CommandParameter("meta", CommandParamType.INT, true),
				CommandParameter("tags...", CommandParamType.RAWTEXT, true)
		)
		commandParameters["toPlayerById"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, false),
				CommandParameter("item ID", CommandParamType.INT, false),
				CommandParameter("amount", CommandParamType.INT, true),
				CommandParameter("tags...", CommandParamType.RAWTEXT, true)
		)
		commandParameters["toPlayerByIdMeta"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, false),
				CommandParameter("item ID:meta", CommandParamType.RAWTEXT, false),
				CommandParameter("amount", CommandParamType.INT, true),
				CommandParameter("tags...", CommandParamType.RAWTEXT, true)
		)
	}
}