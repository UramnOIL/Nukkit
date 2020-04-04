package cn.nukkit.network

import cn.nukkit.Server
import cn.nukkit.scheduler.AsyncTask
import cn.nukkit.utils.Zlib
import java.util.ArrayList
import java.util.List
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class CompressBatchedTask(var data: Array<ByteArray?>?, targets: List<String?>?, level: Int, channel: Int) : AsyncTask() {
	var level = 7
	var finalData: ByteArray?
	var channel = 0
	var targets: List<String?>? = ArrayList()

	constructor(data: Array<ByteArray?>?, targets: List<String?>?) : this(data, targets, 7) {}
	constructor(data: Array<ByteArray?>?, targets: List<String?>?, level: Int) : this(data, targets, level, 0) {}

	@Override
	fun onRun() {
		try {
			finalData = Zlib.deflate(data, level)
			data = null
		} catch (e: Exception) {
			//ignore
		}
	}

	@Override
	fun onCompletion(server: Server?) {
		server.broadcastPacketsCallback(finalData, targets)
	}

	init {
		this.targets = targets
		this.level = level
		this.channel = channel
	}
}