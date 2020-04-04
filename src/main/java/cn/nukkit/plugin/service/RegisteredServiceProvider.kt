package cn.nukkit.plugin.service

import cn.nukkit.plugin.Plugin

/**
 * Created on 16-11-20.
 */
class RegisteredServiceProvider<T> internal constructor(val service: Class<T>, val provider: T, val priority: ServicePriority, val plugin: Plugin?) : Comparable<RegisteredServiceProvider<T>> {
	/**
	 * Return the plugin provide this service.
	 *
	 * @return the plugin provide this service, or `null`
	 * only if this service provided by server
	 */
	/**
	 * Return the provided service.
	 *
	 * @return the provided service
	 */
	/**
	 * Return the service provider.
	 *
	 * @return the service provider
	 */

	override fun equals(o: Any?): Boolean {
		if (this === o) return true
		if (o == null || javaClass != o.javaClass) return false
		val that = o as RegisteredServiceProvider<*>
		return provider === that.provider || provider == that.provider
	}

	override fun hashCode(): Int {
		return provider.hashCode()
	}

	override fun compareTo(other: RegisteredServiceProvider<T>): Int {
		return other.priority.ordinal - priority.ordinal
	}

}