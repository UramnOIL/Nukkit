package cn.nukkit.event.block

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class SignChangeEvent(block: Block?, val player: Player, lines: Array<String?>) : BlockEvent(block), Cancellable {
	val lines = arrayOfNulls<String>(4)

	fun getLine(index: Int): String? {
		return lines[index]
	}

	fun setLine(index: Int, line: String?) {
		lines[index] = line
	}

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.lines = lines
	}
}