package cn.nukkit.permission

import cn.nukkit.Server
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class Permission(val name: String, val description: String = DEFAULT_PERMISSION, var defaultValue: String = "", val children: MutableMap<String, Boolean> = mutableMapOf()) {
	companion object {
		const val DEFAULT_OP = "op"
		const val DEFAULT_NOT_OP = "notop"
		const val DEFAULT_TRUE = "true"
		const val DEFAULT_FALSE = "false"
		const val DEFAULT_PERMISSION = DEFAULT_OP
		fun getByName(value: String): String {
			return when (value.toLowerCase()) {
				"op", "isop", "operator", "isoperator", "admin", "isadmin" -> DEFAULT_OP
				"!op", "notop", "!operator", "notoperator", "!admin", "notadmin" -> DEFAULT_NOT_OP
				"true" -> DEFAULT_TRUE
				else -> DEFAULT_FALSE
			}
		}

		fun loadPermissions(data: Map<String, Any>): List<Permission> {
			return loadPermissions(data, DEFAULT_OP)
		}

		fun loadPermissions(data: Map<String, Any>, defaultValue: String): List<Permission> {
			val result = mutableListOf<Permission>()
			data.forEach { (t, u) ->
				result.add(loadPermission(t, u as Map<String, Any>, defaultValue, result))
			}
			return result
		}

		fun loadPermission(name: String, data: Map<String, Any>): Permission {
			return loadPermission(name, data, DEFAULT_OP, ArrayList())
		}

		fun loadPermission(name: String, data: Map<String, Any>, defaultValue: String): Permission {
			return loadPermission(name, data, defaultValue, ArrayList())
		}

		private fun loadPermission(name: String, data: Map<String, Any>, defaultValue: String, output: MutableList<Permission>): Permission {
			var defaultValue = defaultValue
			var desc: String? = null
			val children = mutableMapOf<String, Boolean>()
			if (data.containsKey("default")) {
				val value = getByName(data["default"].toString())
				defaultValue = value
			}
			if (data.containsKey("children")) {
				if (data["children"] is Map<*, *>) {
					(data["children"] as Map<String, Object>).forEach { t, u ->
						if (u is Map<*, *>) {
							val permission = loadPermission(t, u as Map<String, Object>, defaultValue, output)
							output.add(permission)
						}
						children[t] = true
					}
				} else {
					throw IllegalStateException("'children' key is of wrong type")
				}
			}
			if (data.containsKey("description")) {
				desc = data["description"] as String
			}
			return Permission(name, desc ?: "", defaultValue, children)
		}
	}

	init {
		recalculatePermissibles()
	}
	var default: String
		get() = defaultValue
		set(value) {
			if (value != defaultValue){
				defaultValue = value
				recalculatePermissibles()
			}
		}

	val permissibles: Set<Permissible>
		get() = Server.instance.pluginManager.getPermissionSubscriptions(name)

	fun recalculatePermissibles() {
		val perms: Set<Permissible> = permissibles
		Server.instance.pluginManager.recalculatePermissionDefaults(this)
		perms.forEach {
			it.recalculatePermissions()
		}
	}

	fun addParent(permission: Permission, value: Boolean) {
		children[name] = value
		permission.recalculatePermissibles()
	}

	fun addParent(name: String, value: Boolean): Permission {
		val perm = Server.instance.pluginManager.getPermission(name) ?: run {
			val perm = Permission(name)
			Server.instance.pluginManager.addPermission(perm)
			perm
		}
		this.addParent(perm, value)
		return perm
	}


}