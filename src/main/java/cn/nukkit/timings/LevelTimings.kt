package cn.nukkit.timings

import cn.nukkit.level.Level
import co.aikar.timings.Timing
import co.aikar.timings.TimingsManager

/**
 * @author Pub4Game
 * @author Tee7even
 */
class LevelTimings(level: Level) {
	@JvmField
	val doChunkUnload: Timing
	@JvmField
	val doTickPending: Timing
	@JvmField
	val doChunkGC: Timing
	@JvmField
	val doTick: Timing
	@JvmField
	val tickChunks: Timing
	@JvmField
	val entityTick: Timing
	@JvmField
	val blockEntityTick: Timing
	@JvmField
	val syncChunkSendTimer: Timing
	@JvmField
	val syncChunkSendPrepareTimer: Timing
	@JvmField
	val syncChunkLoadTimer: Timing
	val syncChunkLoadDataTimer: Timing
	val syncChunkLoadEntitiesTimer: Timing
	val syncChunkLoadBlockEntitiesTimer: Timing

	init {
		val name = level.folderName + " - "
		doChunkUnload = TimingsManager.getTiming(name + "doChunkUnload")
		doTickPending = TimingsManager.getTiming(name + "doTickPending")
		doChunkGC = TimingsManager.getTiming(name + "doChunkGC")
		doTick = TimingsManager.getTiming(name + "doTick")
		tickChunks = TimingsManager.getTiming(name + "tickChunks")
		entityTick = TimingsManager.getTiming(name + "entityTick")
		blockEntityTick = TimingsManager.getTiming(name + "blockEntityTick")
		syncChunkSendTimer = TimingsManager.getTiming(name + "syncChunkSend")
		syncChunkSendPrepareTimer = TimingsManager.getTiming(name + "syncChunkSendPrepare")
		syncChunkLoadTimer = TimingsManager.getTiming(name + "syncChunkLoad")
		syncChunkLoadDataTimer = TimingsManager.getTiming(name + "syncChunkLoad - Data")
		syncChunkLoadEntitiesTimer = TimingsManager.getTiming(name + "syncChunkLoad - Entities")
		syncChunkLoadBlockEntitiesTimer = TimingsManager.getTiming(name + "syncChunkLoad - BlockEntities")
	}
}