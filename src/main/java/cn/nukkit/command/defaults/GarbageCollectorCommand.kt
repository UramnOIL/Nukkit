package cn.nukkit.command.defaults

import cn.nukkit.command.CommandSender
import cn.nukkit.math.NukkitMath
import cn.nukkit.utils.TextFormat
import cn.nukkit.utils.ThreadCache

/**
 * Created on 2015/11/11 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class GarbageCollectorCommand(name: String) : VanillaCommand(name, "%nukkit.command.gc.description", "%nukkit.command.gc.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		var chunksCollected = 0
		var entitiesCollected = 0
		var tilesCollected = 0
		val memory = Runtime.getRuntime().freeMemory()
		for (level in sender.server.getLevels().values) {
			val chunksCount = level.chunks.size
			val entitiesCount = level.entities.size
			val tilesCount = level.blockEntities.size
			level.doChunkGarbageCollection()
			level.unloadChunks(true)
			chunksCollected += chunksCount - level.chunks.size
			entitiesCollected += entitiesCount - level.entities.size
			tilesCollected += tilesCount - level.blockEntities.size
		}
		ThreadCache.clean()
		System.gc()
		val freedMemory = Runtime.getRuntime().freeMemory() - memory
		sender.sendMessage(TextFormat.GREEN.toString() + "---- " + TextFormat.WHITE + "Garbage collection result" + TextFormat.GREEN + " ----")
		sender.sendMessage(TextFormat.GOLD.toString() + "Chunks: " + TextFormat.RED + chunksCollected)
		sender.sendMessage(TextFormat.GOLD.toString() + "Entities: " + TextFormat.RED + entitiesCollected)
		sender.sendMessage(TextFormat.GOLD.toString() + "Block Entities: " + TextFormat.RED + tilesCollected)
		sender.sendMessage(TextFormat.GOLD.toString() + "Memory freed: " + TextFormat.RED + NukkitMath.round(freedMemory / 1024.0 / 1024.0, 2) + " MB")
		return true
	}

	init {
		permission = "nukkit.command.gc"
		commandParameters.clear()
	}
}