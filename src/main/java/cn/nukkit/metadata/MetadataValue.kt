package cn.nukkit.metadata

import cn.nukkit.plugin.Plugin
import java.lang.ref.WeakReference
import kotlin.jvm.Throws

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class MetadataValue protected constructor(owningPlugin: Plugin?) {
	protected val owningPlugin: WeakReference<Plugin?>?
	fun getOwningPlugin(): Plugin? {
		return owningPlugin.get()
	}

	abstract fun value(): Object?
	abstract fun invalidate()

	init {
		this.owningPlugin = WeakReference(owningPlugin)
	}
}