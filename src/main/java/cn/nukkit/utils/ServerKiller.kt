package cn.nukkit.utils

import java.util.concurrent.TimeUnit

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ServerKiller @JvmOverloads constructor(time: Long, unit: TimeUnit = TimeUnit.SECONDS) : Thread() {
	val sleepTime: Long
	override fun run() {
		try {
			sleep(sleepTime)
		} catch (e: InterruptedException) {
			// ignore
		}
		println("\nTook too long to stop, server was killed forcefully!\n")
		System.exit(1)
	}

	init {
		sleepTime = unit.toMillis(time)
		name = "Server Killer"
	}
}