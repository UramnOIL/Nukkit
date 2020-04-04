package cn.nukkit.permission

import cn.nukkit.plugin.Plugin
import cn.nukkit.utils.PluginException

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class PermissionAttachment(val plugin: Plugin, val permissible: Permissible) {
	var removalCallback: PermissionRemovedExecutor? = null
	val permissions: MutableMap<String, Boolean> = HashMap()

	init {
		if (!plugin.isEnabled) {
			throw PluginException("Plugin " + plugin.description.name + " is disabled")
		}
	}

	fun clearPermissions() {
		permissions.clear()
		permissible.recalculatePermissions()
	}

	fun setPermissions(permissions: Map<String, Boolean>) {
		permissions.forEach { (t, u) ->
			this.permissions[t] = u
		}
		permissible.recalculatePermissions()
	}

	fun unsetPermissions(permissions: List<String>) {
		permissions.forEach {
			this.permissions.remove(it)
		}
		permissible.recalculatePermissions()
	}

	fun setPermission(permission: Permission, value: Boolean) {
		this.setPermission(permission.name, value)
	}

	fun setPermission(name: String, value: Boolean) {
		if (permissions.containsKey(name)) {
			if (permissions[name]!!.equals(value)) {
				return
			}
			permissions.remove(name)
		}
		permissions[name] = value
		permissible.recalculatePermissions()
	}

	fun unsetPermission(permission: Permission, value: Boolean) {
		this.unsetPermission(permission.name, value)
	}

	fun unsetPermission(name: String, value: Boolean) {
		if (permissions.containsKey(name)) {
			permissions.remove(name)
			permissible.recalculatePermissions()
		}
	}

	fun remove() {
		permissible.removeAttachment(this)
	}
}