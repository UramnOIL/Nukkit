package cn.nukkit.plugin

import cn.nukkit.event.Event
import cn.nukkit.event.Listener
import cn.nukkit.utils.EventException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class MethodEventExecutor(private val method: Method) : EventExecutor {
	@Throws(EventException::class)
	override fun execute(listener: Listener?, event: Event) {
		try {
			val params = method.parameterTypes as Array<Class<Event>>
			for (param in params) {
				if (param.isAssignableFrom(event.javaClass)) {
					method.invoke(listener, event)
					break
				}
			}
		} catch (ex: InvocationTargetException) {
			throw EventException(ex.cause)
		} catch (ex: ClassCastException) {
			// We are going to ignore ClassCastException because EntityDamageEvent can't be cast to EntityDamageByEntityEvent
		} catch (t: Throwable) {
			throw EventException(t)
		}
	}

	fun getMethod(): Method {
		return method
	}

}