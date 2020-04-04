package cn.nukkit.command.defaults

import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.event.player.PlayerKickEvent
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.set

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BanIpCommand(name: String) : VanillaCommand(name, "%nukkit.command.ban.ip.description", "%commands.banip.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (args.size == 0) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return false
		}
		var value = args[0]
		var reason = ""
		for (i in 1 until args.size) {
			reason += args[i].toString() + " "
		}
		if (reason.length > 0) {
			reason = reason.substring(0, reason.length - 1)
		}
		if (Pattern.matches("^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$", value)) {
			processIPBan(value, sender, reason)
			broadcastCommandMessage(sender, TranslationContainer("commands.banip.success", value))
		} else {
			val player = sender.server.getPlayer(value)
			if (player != null) {
				processIPBan(player.address, sender, reason)
				broadcastCommandMessage(sender, TranslationContainer("commands.banip.success.players", player.address, player.getName()))
			} else {
				val name = value!!.toLowerCase()
				val path = sender.server.dataPath + "players/"
				val file = File("$path$name.dat")
				var nbt: CompoundTag? = null
				if (file.exists()) {
					nbt = try {
						NBTIO.readCompressed(FileInputStream(file))
					} catch (e: IOException) {
						throw RuntimeException(e)
					}
				}
				if (nbt != null && nbt.contains("lastIP") && Pattern.matches("^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$", nbt.getString("lastIP").also { value = it })) {
					processIPBan(value, sender, reason)
					broadcastCommandMessage(sender, TranslationContainer("commands.banip.success", value))
				} else {
					sender.sendMessage(TranslationContainer("commands.banip.invalid"))
					return false
				}
			}
		}
		return true
	}

	private fun processIPBan(ip: String?, sender: CommandSender, reason: String) {
		sender.server.iPBans.addBan(ip, reason, null, sender.name)
		for (player in ArrayList(sender.server.onlinePlayers.values)) {
			if (player.address == ip) {
				player.kick(PlayerKickEvent.Reason.IP_BANNED, if (!reason.isEmpty()) reason else "IP banned")
			}
		}
		sender.server.network.blockAddress(ip, -1)
	}

	init {
		permission = "nukkit.command.ban.ip"
		aliases = arrayOf<String?>("banip")
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, false),
				CommandParameter("reason", CommandParamType.STRING, true)
		)
	}
}