package cn.nukkit.utils

import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.equals
import cn.nukkit.math.Vector3

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockUpdateEntry : Comparable<BlockUpdateEntry> {
	var priority = 0
	var delay: Long = 0
	val pos: Vector3
	val block: Block
	val id: Long

	constructor(pos: Vector3, block: Block) {
		this.pos = pos
		this.block = block
		id = entryID++
	}

	constructor(pos: Vector3, block: Block, delay: Long, priority: Int) {
		id = entryID++
		this.pos = pos
		this.priority = priority
		this.delay = delay
		this.block = block
	}

	override fun compareTo(entry: BlockUpdateEntry): Int {
		return if (delay < entry.delay) -1 else if (delay > entry.delay) 1 else if (priority != entry.priority) priority - entry.priority else java.lang.Long.compare(id, entry.id)
	}

	override fun equals(`object`: Any?): Boolean {
		return if (`object` !is BlockUpdateEntry) {
			if (`object` is Vector3) {
				pos.equals(`object`)
			} else false
		} else {
			val entry = `object`
			pos.equals(entry.pos) && equals(block, entry.block, false)
		}
	}

	override fun hashCode(): Int {
		return pos.hashCode()
	}

	companion object {
		private var entryID: Long = 0
	}
}