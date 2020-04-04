package cn.nukkit.event

/**
 * Created by Nukkit Team.
 */
interface Cancellable {
	var isCancelled: Boolean
	fun setCancelled()
}