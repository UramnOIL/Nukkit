package cn.nukkit.event

import cn.nukkit.plugin.Plugin
import cn.nukkit.plugin.RegisteredListener
import java.util.*
import kotlin.collections.Collection
import kotlin.collections.MutableList
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.collections.set
import kotlin.collections.toTypedArray

/**
 * Created by Nukkit Team.
 */
class HandlerList {
	@Volatile
	private var handlers: Array<RegisteredListener>? = null
	private val handlerslots: EnumMap<EventPriority, ArrayList<RegisteredListener>>

	@Synchronized
	fun register(listener: RegisteredListener) {
		check(!handlerslots[listener.priority]!!.contains(listener)) { "This listener is already registered to priority " + listener.priority.toString() }
		handlers = null
		handlerslots[listener.priority]!!.add(listener)
	}

	fun registerAll(listeners: Collection<RegisteredListener>) {
		for (listener in listeners) {
			register(listener)
		}
	}

	@Synchronized
	fun unregister(listener: RegisteredListener) {
		if (handlerslots[listener.priority]!!.remove(listener)) {
			handlers = null
		}
	}

	@Synchronized
	fun unregister(plugin: Plugin) {
		var changed = false
		for (list in handlerslots.values) {
			val i = list.listIterator()
			while (i.hasNext()) {
				if (i.next().plugin == plugin) {
					i.remove()
					changed = true
				}
			}
		}
		if (changed) handlers = null
	}

	@Synchronized
	fun unregister(listener: Listener) {
		var changed = false
		for (list in handlerslots.values) {
			val i = list.listIterator()
			while (i.hasNext()) {
				if (i.next().listener == listener) {
					i.remove()
					changed = true
				}
			}
		}
		if (changed) handlers = null
	}

	@Synchronized
	fun bake() {
		if (handlers != null) return  // don't re-bake when still valid
		val entries: MutableList<RegisteredListener> = ArrayList()
		for ((_, value) in handlerslots) {
			entries.addAll(value)
		}
		handlers = entries.toTypedArray()
	}

	// This prevents fringe cases of returning null
	val registeredListeners: Array<RegisteredListener>
		get() {
			var handlers: Array<RegisteredListener>
			while (this.handlers.also { handlers = it!! } == null) {
				bake()
			} // This prevents fringe cases of returning null
			return handlers
		}

	companion object {
		private val allLists = ArrayList<HandlerList>()
		fun bakeAll() {
			synchronized(allLists) {
				for (h in allLists) {
					h.bake()
				}
			}
		}

		fun unregisterAll() {
			synchronized(allLists) {
				for (h in allLists) {
					synchronized(h) {
						for (list in h.handlerslots.values) {
							list.clear()
						}
						h.handlers = null
					}
				}
			}
		}

		fun unregisterAll(plugin: Plugin) {
			synchronized(allLists) {
				for (h in allLists) {
					h.unregister(plugin)
				}
			}
		}

		fun unregisterAll(listener: Listener) {
			synchronized(allLists) {
				for (h in allLists) {
					h.unregister(listener)
				}
			}
		}

		fun getRegisteredListeners(plugin: Plugin): ArrayList<RegisteredListener> {
			val listeners = ArrayList<RegisteredListener>()
			synchronized(allLists) {
				for (h in allLists) {
					synchronized(h) {
						for (list in h.handlerslots.values) {
							for (listener in list) {
								if (listener.plugin == plugin) {
									listeners.add(listener)
								}
							}
						}
					}
				}
			}
			return listeners
		}

		val handlerLists: ArrayList<HandlerList>
			get() {
				synchronized(allLists) { return ArrayList(allLists) }
			}
	}

	init {
		handlerslots = EnumMap(EventPriority::class.java)
		for (o in EventPriority.values()) {
			handlerslots[o] = ArrayList()
		}
		synchronized(allLists) { allLists.add(this) }
	}
}