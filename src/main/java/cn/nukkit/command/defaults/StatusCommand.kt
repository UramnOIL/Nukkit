package cn.nukkit.command.defaults

import cn.nukkit.Nukkit
import cn.nukkit.command.CommandSender
import cn.nukkit.math.NukkitMath
import cn.nukkit.utils.TextFormat
import java.util.concurrent.TimeUnit

/**
 * Created on 2015/11/11 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class StatusCommand(name: String) : VanillaCommand(name, "%nukkit.command.status.description", "%nukkit.command.status.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		val server = sender.server
		sender.sendMessage(TextFormat.GREEN.toString() + "---- " + TextFormat.WHITE + "Server status" + TextFormat.GREEN + " ----")
		val time = System.currentTimeMillis() - Nukkit.START_TIME
		sender.sendMessage(TextFormat.GOLD.toString() + "Uptime: " + formatUptime(time))
		var tpsColor = TextFormat.GREEN
		val tps = server!!.ticksPerSecond
		if (tps < 17) {
			tpsColor = TextFormat.GOLD
		} else if (tps < 12) {
			tpsColor = TextFormat.RED
		}
		sender.sendMessage(TextFormat.GOLD.toString() + "Current TPS: " + tpsColor + NukkitMath.round(tps.toDouble(), 2))
		sender.sendMessage(TextFormat.GOLD.toString() + "Load: " + tpsColor + server.tickUsage + "%")
		sender.sendMessage(TextFormat.GOLD.toString() + "Network upload: " + TextFormat.GREEN + NukkitMath.round(server.network.upload / 1024 * 1000, 2) + " kB/s")
		sender.sendMessage(TextFormat.GOLD.toString() + "Network download: " + TextFormat.GREEN + NukkitMath.round(server.network.download / 1024 * 1000, 2) + " kB/s")
		sender.sendMessage(TextFormat.GOLD.toString() + "Thread count: " + TextFormat.GREEN + Thread.getAllStackTraces().size)
		val runtime = Runtime.getRuntime()
		val totalMB = NukkitMath.round(runtime.totalMemory().toDouble() / 1024 / 1024, 2)
		val usedMB = NukkitMath.round((runtime.totalMemory() - runtime.freeMemory()).toDouble() / 1024 / 1024, 2)
		val maxMB = NukkitMath.round(runtime.maxMemory().toDouble() / 1024 / 1024, 2)
		val usage = usedMB / maxMB * 100
		var usageColor = TextFormat.GREEN
		if (usage > 85) {
			usageColor = TextFormat.GOLD
		}
		sender.sendMessage(TextFormat.GOLD.toString() + "Used memory: " + usageColor + usedMB + " MB. (" + NukkitMath.round(usage, 2) + "%)")
		sender.sendMessage(TextFormat.GOLD.toString() + "Total memory: " + TextFormat.RED + totalMB + " MB.")
		sender.sendMessage(TextFormat.GOLD.toString() + "Maximum VM memory: " + TextFormat.RED + maxMB + " MB.")
		sender.sendMessage(TextFormat.GOLD.toString() + "Available processors: " + TextFormat.GREEN + runtime.availableProcessors())
		var playerColor = TextFormat.GREEN
		if (server.onlinePlayers.size.toFloat() / server.maxPlayers.toFloat() > 0.85) {
			playerColor = TextFormat.GOLD
		}
		sender.sendMessage(TextFormat.GOLD.toString() + "Players: " + playerColor + server.onlinePlayers.size + TextFormat.GREEN + " online, " +
				TextFormat.RED + server.maxPlayers + TextFormat.GREEN + " max. ")
		for (level in server.getLevels().values) {
			sender.sendMessage(
					TextFormat.GOLD.toString() + "World \"" + level.folderName + "\"" + (if (level.folderName != level.name) " (" + level.name + ")" else "") + ": " +
							TextFormat.RED + level.chunks.size + TextFormat.GREEN + " chunks, " +
							TextFormat.RED + level.entities.size + TextFormat.GREEN + " entities, " +
							TextFormat.RED + level.blockEntities.size + TextFormat.GREEN + " blockEntities." +
							" Time " + (if (level.tickRate > 1 || level.getTickRateTime() > 40) TextFormat.RED else TextFormat.YELLOW) + NukkitMath.round(level.getTickRateTime().toDouble(), 2) + "ms" +
							if (level.tickRate > 1) " (tick rate " + level.tickRate + ")" else ""
			)
		}
		return true
	}

	companion object {
		private val UPTIME_FORMAT = TextFormat.RED.toString() + "%d" + TextFormat.GOLD + " days " +
				TextFormat.RED + "%d" + TextFormat.GOLD + " hours " +
				TextFormat.RED + "%d" + TextFormat.GOLD + " minutes " +
				TextFormat.RED + "%d" + TextFormat.GOLD + " seconds"

		private fun formatUptime(uptime: Long): String {
			var uptime = uptime
			val days = TimeUnit.MILLISECONDS.toDays(uptime)
			uptime -= TimeUnit.DAYS.toMillis(days)
			val hours = TimeUnit.MILLISECONDS.toHours(uptime)
			uptime -= TimeUnit.HOURS.toMillis(hours)
			val minutes = TimeUnit.MILLISECONDS.toMinutes(uptime)
			uptime -= TimeUnit.MINUTES.toMillis(minutes)
			val seconds = TimeUnit.MILLISECONDS.toSeconds(uptime)
			return String.format(UPTIME_FORMAT, days, hours, minutes, seconds)
		}
	}

	init {
		permission = "nukkit.command.status"
		commandParameters.clear()
	}
}