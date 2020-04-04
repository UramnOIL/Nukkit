package cn.nukkit.scheduler

import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.equals
import cn.nukkit.level.Level
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.NukkitMath.floorDouble
import cn.nukkit.math.Vector3
import cn.nukkit.utils.BlockUpdateEntry
import com.google.common.collect.Maps
import java.util.*

class BlockUpdateScheduler(level: Level, currentTick: Long) {
	private val level: Level
	private var lastTick: Long
	private val queuedUpdates: MutableMap<Long, LinkedHashSet<BlockUpdateEntry>>
	private var pendingUpdates: Set<BlockUpdateEntry>? = null

	@Synchronized
	fun tick(currentTick: Long) {
		// Should only perform once, unless ticks were skipped
		if (currentTick - lastTick < Short.MAX_VALUE) { // Arbitrary
			for (tick in lastTick + 1..currentTick) {
				perform(tick)
			}
		} else {
			val times = ArrayList(queuedUpdates.keys)
			Collections.sort(times)
			for (tick in times) {
				if (tick <= currentTick) {
					perform(tick)
				} else {
					break
				}
			}
		}
		lastTick = currentTick
	}

	private fun perform(tick: Long) {
		try {
			lastTick = tick
			pendingUpdates = queuedUpdates.remove(tick)
			val updates = pendingUpdates
			if (updates != null) {
				for (entry in updates) {
					val pos = entry.pos
					if (level.isChunkLoaded(floorDouble(pos.x) shr 4, floorDouble(pos.z) shr 4)) {
						val block = level.getBlock(entry.pos)
						if (equals(block, entry.block, false)) {
							block.onUpdate(Level.BLOCK_UPDATE_SCHEDULED)
						}
					} else {
						level.scheduleUpdate(entry.block, entry.pos, 0)
					}
				}
			}
		} finally {
			pendingUpdates = null
		}
	}

	fun getPendingBlockUpdates(boundingBox: AxisAlignedBB): Set<BlockUpdateEntry?>? {
		var set: MutableSet<BlockUpdateEntry?>? = null
		for ((_, tickSet) in queuedUpdates) {
			for (update in tickSet) {
				val pos = update.pos
				if (pos.getX() >= boundingBox.minX && pos.getX() < boundingBox.maxX && pos.getZ() >= boundingBox.minZ && pos.getZ() < boundingBox.maxZ) {
					if (set == null) {
						set = LinkedHashSet()
					}
					set.add(update)
				}
			}
		}
		return set
	}

	fun isBlockTickPending(pos: Vector3?, block: Block?): Boolean {
		val tmpUpdates = pendingUpdates
		return if (tmpUpdates == null || tmpUpdates.isEmpty()) false else tmpUpdates.contains(BlockUpdateEntry(pos, block))
	}

	private fun getMinTime(entry: BlockUpdateEntry): Long {
		return Math.max(entry.delay, lastTick + 1)
	}

	fun add(entry: BlockUpdateEntry) {
		val time = getMinTime(entry)
		var updateSet = queuedUpdates[time]
		if (updateSet == null) {
			val tmp = queuedUpdates.putIfAbsent(time, LinkedHashSet<BlockUpdateEntry>().also { updateSet = it })
			if (tmp != null) updateSet = tmp
		}
		updateSet!!.add(entry)
	}

	operator fun contains(entry: BlockUpdateEntry): Boolean {
		for ((_, value) in queuedUpdates) {
			if (value.contains(entry)) {
				return true
			}
		}
		return false
	}

	fun remove(entry: BlockUpdateEntry): Boolean {
		for ((_, value) in queuedUpdates) {
			if (value.remove(entry)) {
				return true
			}
		}
		return false
	}

	fun remove(pos: Vector3?): Boolean {
		for ((_, value) in queuedUpdates) {
			if (value.remove(pos)) {
				return true
			}
		}
		return false
	}

	init {
		queuedUpdates = Maps.newHashMap() // Change to ConcurrentHashMap if this needs to be concurrent
		lastTick = currentTick
		this.level = level
	}
}