package cn.nukkit.permission

import cn.nukkit.Server

/**
 * author: MagicDroidX
 * Nukkit Project
 */
object DefaultPermissions {
	const val ROOT = "nukkit"
	private fun registerPermission(perm: Permission): Permission {
		return registerPermission(perm, null)
	}

	private fun registerPermission(perm: Permission, parent: Permission?): Permission {
		parent?.let { it.children[perm.name] = true }
		Server.instance.pluginManager.addPermission(perm)
		return Server.instance.pluginManager.getPermission(perm.name)!!
	}

	fun registerCorePermissions() {
		val parent: Permission = registerPermission(Permission(ROOT, "Allows using all Nukkit commands and utilities"))
		val broadcasts: Permission = registerPermission(Permission("$ROOT.broadcast", "Allows the user to receive all broadcast messages"), parent)
		registerPermission(Permission("$ROOT.broadcast.admin", "Allows the user to receive administrative broadcasts"), broadcasts)
		registerPermission(Permission("$ROOT.broadcast.user", "Allows the user to receive user broadcasts"), broadcasts)
		broadcasts.recalculatePermissibles()
		val commands: Permission = registerPermission(Permission("$ROOT.command", "Allows using all Nukkit commands"), parent)
		val whitelist: Permission = registerPermission(Permission("$ROOT.command.whitelist", "Allows the user to modify the server whitelist"), commands)
		registerPermission(Permission("$ROOT.command.whitelist.add", "Allows the user to add a player to the server whitelist"), whitelist)
		registerPermission(Permission("$ROOT.command.whitelist.remove", "Allows the user to remove a player to the server whitelist"), whitelist)
		registerPermission(Permission("$ROOT.command.whitelist.reload", "Allows the user to reload the server whitelist"), whitelist)
		registerPermission(Permission("$ROOT.command.whitelist.enable", "Allows the user to enable the server whitelist"), whitelist)
		registerPermission(Permission("$ROOT.command.whitelist.disable", "Allows the user to disable the server whitelist"), whitelist)
		registerPermission(Permission("$ROOT.command.whitelist.list", "Allows the user to list all the players on the server whitelist"), whitelist)
		whitelist.recalculatePermissibles()
		val ban: Permission = registerPermission(Permission("$ROOT.command.ban", "Allows the user to ban people"), commands)
		registerPermission(Permission("$ROOT.command.ban.player", "Allows the user to ban players"), ban)
		registerPermission(Permission("$ROOT.command.ban.ip", "Allows the user to ban IP addresses"), ban)
		registerPermission(Permission("$ROOT.command.ban.list", "Allows the user to list all the banned ips or players"), ban)
		ban.recalculatePermissibles()
		val unban: Permission = registerPermission(Permission("$ROOT.command.unban", "Allows the user to unban people"), commands)
		registerPermission(Permission("$ROOT.command.unban.player", "Allows the user to unban players"), unban)
		registerPermission(Permission("$ROOT.command.unban.ip", "Allows the user to unban IP addresses"), unban)
		unban.recalculatePermissibles()
		val op: Permission = registerPermission(Permission("$ROOT.command.op", "Allows the user to change operators"), commands)
		registerPermission(Permission("$ROOT.command.op.give", "Allows the user to give a player operator status"), op)
		registerPermission(Permission("$ROOT.command.op.take", "Allows the user to take a players operator status"), op)
		op.recalculatePermissibles()
		val save: Permission = registerPermission(Permission("$ROOT.command.save", "Allows the user to save the worlds"), commands)
		registerPermission(Permission("$ROOT.command.save.enable", "Allows the user to enable automatic saving"), save)
		registerPermission(Permission("$ROOT.command.save.disable", "Allows the user to disable automatic saving"), save)
		registerPermission(Permission("$ROOT.command.save.perform", "Allows the user to perform a manual save"), save)
		save.recalculatePermissibles()
		val time: Permission = registerPermission(Permission("$ROOT.command.time", "Allows the user to alter the time"), commands)
		registerPermission(Permission("$ROOT.command.time.add", "Allows the user to fast-forward time"), time)
		registerPermission(Permission("$ROOT.command.time.set", "Allows the user to change the time"), time)
		registerPermission(Permission("$ROOT.command.time.start", "Allows the user to restart the time"), time)
		registerPermission(Permission("$ROOT.command.time.stop", "Allows the user to stop the time"), time)
		registerPermission(Permission("$ROOT.command.time.query", "Allows the user query the time"), time)
		time.recalculatePermissibles()
		val kill: Permission = registerPermission(Permission("$ROOT.command.kill", "Allows the user to kill players"), commands)
		registerPermission(Permission("$ROOT.command.kill.self", "Allows the user to commit suicide"), kill)
		registerPermission(Permission("$ROOT.command.kill.other", "Allows the user to kill other players"), kill)
		kill.recalculatePermissibles()
		val gamemode: Permission = registerPermission(Permission("$ROOT.command.gamemode", "Allows the user to change the gamemode of players"), commands)
		registerPermission(Permission("$ROOT.command.gamemode.survival", "Allows the user to change the gamemode to survival"), gamemode)
		registerPermission(Permission("$ROOT.command.gamemode.creative", "Allows the user to change the gamemode to creative"), gamemode)
		registerPermission(Permission("$ROOT.command.gamemode.adventure", "Allows the user to change the gamemode to adventure"), gamemode)
		registerPermission(Permission("$ROOT.command.gamemode.spectator", "Allows the user to change the gamemode to spectator"), gamemode)
		registerPermission(Permission("$ROOT.command.gamemode.other", "Allows the user to change the gamemode of other players"), gamemode)
		gamemode.recalculatePermissibles()
		registerPermission(Permission("$ROOT.command.me", "Allows the user to perform a chat action"), commands)
		registerPermission(Permission("$ROOT.command.tell", "Allows the user to privately message another player"), commands)
		registerPermission(Permission("$ROOT.command.say", "Allows the user to talk as the console"), commands)
		registerPermission(Permission("$ROOT.command.give", "Allows the user to give items to players"), commands)
		registerPermission(Permission("$ROOT.command.effect", "Allows the user to give/take potion effects"), commands)
		registerPermission(Permission("$ROOT.command.particle", "Allows the user to create particle effects"), commands)
		registerPermission(Permission("$ROOT.command.teleport", "Allows the user to teleport players"), commands)
		registerPermission(Permission("$ROOT.command.kick", "Allows the user to kick players"), commands)
		registerPermission(Permission("$ROOT.command.stop", "Allows the user to stop the server"), commands)
		registerPermission(Permission("$ROOT.command.list", "Allows the user to list all online players"), commands)
		registerPermission(Permission("$ROOT.command.help", "Allows the user to view the help menu"), commands)
		registerPermission(Permission("$ROOT.command.plugins", "Allows the user to view the list of plugins"), commands)
		registerPermission(Permission("$ROOT.command.reload", "Allows the user to reload the server settings"), commands)
		registerPermission(Permission("$ROOT.command.version", "Allows the user to view the version of the server"), commands)
		registerPermission(Permission("$ROOT.command.defaultgamemode", "Allows the user to change the default gamemode"), commands)
		registerPermission(Permission("$ROOT.command.seed", "Allows the user to view the seed of the world"), commands)
		registerPermission(Permission("$ROOT.command.status", "Allows the user to view the server performance"), commands)
		registerPermission(Permission("$ROOT.command.gc", "Allows the user to fire garbage collection tasks"), commands)
		//registerPermission(new Permission(ROOT + ".command.dumpmemory", "Allows the user to dump memory contents", Permission.DEFAULT_OP), commands); // this command is not implemented
		registerPermission(Permission("$ROOT.command.gamerule", "Sets or queries a game rule value"), commands)
		registerPermission(Permission("$ROOT.command.timings", "Allows the user to records timings for all plugin events"), commands)
		registerPermission(Permission("$ROOT.command.title", "Allows the user to send titles to players"), commands)
		registerPermission(Permission("$ROOT.command.spawnpoint", "Allows the user to change player's spawnpoint"), commands)
		registerPermission(Permission("$ROOT.command.setworldspawn", "Allows the user to change the world spawn"), commands)
		registerPermission(Permission("$ROOT.command.weather", "Allows the user to change the weather"), commands)
		registerPermission(Permission("$ROOT.command.xp", "Allows the user to give experience"), commands)
		registerPermission(Permission("$ROOT.command.enchant", "Allows the user to enchant items"), commands)
		registerPermission(Permission("$ROOT.command.difficulty", "Allows the user to change the difficulty"), commands)
		registerPermission(Permission("$ROOT.command.debug.perform", "Allows the user to use debugpaste command"), commands)
		registerPermission(Permission("$ROOT.textcolor", "Allows the user to write colored text"), commands)
		commands.recalculatePermissibles()
		parent.recalculatePermissibles()
	}
}