package cn.nukkit.command.defaults

import cn.nukkit.Server
import cn.nukkit.command.CommandSender
import cn.nukkit.network.protocol.ProtocolInfo
import cn.nukkit.scheduler.AsyncTask
import cn.nukkit.utils.HastebinUtility
import cn.nukkit.utils.MainLogger
import cn.nukkit.utils.Utils
import java.io.File
import java.io.IOException
import java.lang.management.ManagementFactory

class DebugPasteCommand(name: String) : VanillaCommand(name, "%nukkit.command.debug.description", "%nukkit.command.debug.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		val server = Server.instance
		server!!.scheduler.scheduleAsyncTask(object : AsyncTask() {
			override fun onRun() {
				try {
					StatusCommand("status").execute(server.consoleSender, "status", arrayOf())
					val dataPath = server.dataPath
					val nukkitYML = HastebinUtility.upload(File(dataPath, "nukkit.yml"))
					val serverProperties = HastebinUtility.upload(File(dataPath, "server.properties"))
					val latestLog = HastebinUtility.upload(File(dataPath, "/logs/server.log"))
					val threadDump = HastebinUtility.upload(Utils.getAllThreadDumps())
					val b = StringBuilder()
					b.append("# Files\n")
					b.append("links.nukkit_yml: ").append(nukkitYML).append('\n')
					b.append("links.server_properties: ").append(serverProperties).append('\n')
					b.append("links.server_log: ").append(latestLog).append('\n')
					b.append("links.thread_dump: ").append(threadDump).append('\n')
					b.append("\n# Server Information\n")
					b.append("version.api: ").append(server.apiVersion).append('\n')
					b.append("version.nukkit: ").append(server.nukkitVersion).append('\n')
					b.append("version.minecraft: ").append(server.version).append('\n')
					b.append("version.protocol: ").append(ProtocolInfo.CURRENT_PROTOCOL).append('\n')
					b.append("plugins:")
					for (plugin in server.pluginManager.plugins.values) {
						val enabled = plugin.isEnabled
						val name = plugin.name
						val desc = plugin.description
						val version = desc.version
						b.append("\n  ")
								.append(name)
								.append(":\n    ")
								.append("version: '")
								.append(version)
								.append('\'')
								.append("\n    enabled: ")
								.append(enabled)
					}
					b.append("\n\n# Java Details\n")
					val runtime = Runtime.getRuntime()
					b.append("memory.free: ").append(runtime.freeMemory()).append('\n')
					b.append("memory.max: ").append(runtime.maxMemory()).append('\n')
					b.append("cpu.runtime: ").append(ManagementFactory.getRuntimeMXBean().uptime).append('\n')
					b.append("cpu.processors: ").append(runtime.availableProcessors()).append('\n')
					b.append("java.specification.version: '").append(System.getProperty("java.specification.version")).append("'\n")
					b.append("java.vendor: '").append(System.getProperty("java.vendor")).append("'\n")
					b.append("java.version: '").append(System.getProperty("java.version")).append("'\n")
					b.append("os.arch: '").append(System.getProperty("os.arch")).append("'\n")
					b.append("os.name: '").append(System.getProperty("os.name")).append("'\n")
					b.append("os.version: '").append(System.getProperty("os.version")).append("'\n\n")
					b.append("\n# Create a ticket: https://github.com/NukkitX/Nukkit/issues/new")
					val link = HastebinUtility.upload(b.toString())
					sender.sendMessage(link)
				} catch (e: IOException) {
					MainLogger.getLogger().logException(e)
				}
			}
		})
		return true
	}

	init {
		permission = "nukkit.command.debug.perform"
	}
}