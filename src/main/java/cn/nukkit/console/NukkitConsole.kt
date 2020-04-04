package cn.nukkit.console

import cn.nukkit.Server
import cn.nukkit.event.server.ServerCommandEvent
import co.aikar.timings.Timings
import lombok.RequiredArgsConstructor
import net.minecrell.terminalconsole.SimpleTerminalConsole
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

@RequiredArgsConstructor
class NukkitConsole(private val server: Server) : SimpleTerminalConsole() {
	private val consoleQueue: BlockingQueue<String> = LinkedBlockingQueue()
	private val executingCommands = AtomicBoolean(false)
	override fun isRunning(): Boolean {
		return server.isRunning()
	}

	override fun runCommand(command: String) {
		if (executingCommands.get()) {
			Timings.serverCommandTimer.startTiming()
			val event = ServerCommandEvent(server.consoleSender, command)
			server.pluginManager.callEvent(event)
			if (!event.isCancelled) {
				Server.instance.scheduler.scheduleTask { server.dispatchCommand(event.sender, event.command) }
			}
			Timings.serverCommandTimer.stopTiming()
		} else {
			consoleQueue.add(command)
		}
	}

	fun readLine(): String {
		return try {
			consoleQueue.take()
		} catch (e: InterruptedException) {
			throw RuntimeException(e)
		}
	}

	override fun shutdown() {
		server.shutdown()
	}

	override fun buildReader(builder: LineReaderBuilder): LineReader {
		builder.completer(NukkitConsoleCompleter(server))
		builder.appName("Nukkit")
		builder.option(LineReader.Option.HISTORY_BEEP, false)
		builder.option(LineReader.Option.HISTORY_IGNORE_DUPS, true)
		builder.option(LineReader.Option.HISTORY_IGNORE_SPACE, true)
		return super.buildReader(builder)
	}

	fun isExecutingCommands(): Boolean {
		return executingCommands.get()
	}

	fun setExecutingCommands(executingCommands: Boolean) {
		if (this.executingCommands.compareAndSet(!executingCommands, executingCommands) && executingCommands) {
			consoleQueue.clear()
		}
	}
}