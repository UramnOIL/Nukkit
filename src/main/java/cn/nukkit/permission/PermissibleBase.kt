package cn.nukkit.permission

import cn.nukkit.Server
import cn.nukkit.plugin.Plugin
import cn.nukkit.plugin.PluginBase
import cn.nukkit.utils.PluginException
import co.aikar.timings.Timings
import java.util.HashMap
import java.util.HashSet

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class PermissibleBase(private val opable: ServerOperator) : Permissible {
	private var parent: Permissible? = null
	private val attachments: MutableSet<PermissionAttachment> = HashSet()
	override val permissions: MutableMap<String, PermissionAttachmentInfo> = HashMap()

	init {
		if(opable is Permissible) {
			parent = opable
		}
	}

	override var isOp: Boolean
		get() = opable.isOp
		set(value) {
			opable.isOp = value
		}

	override val effectivePermissions: Map<String, Any>
		get() = permissions

	override fun hasPermission(name: String): Boolean {
		if(permissions.contains(name)) {
			return permissions[name]!!.value
		}
		val perm = Server.instance.pluginManager.getPermission(name)
		return if (perm != null) {
			val permission: String = perm.default
			Permission.DEFAULT_TRUE == permission || isOp && Permission.DEFAULT_OP == permission || !isOp && Permission.DEFAULT_NOT_OP == permission
		} else {
			Permission.DEFAULT_TRUE == Permission.DEFAULT_PERMISSION || isOp && Permission.DEFAULT_OP == Permission.DEFAULT_PERMISSION || !isOp && Permission.DEFAULT_NOT_OP.equals(Permission.DEFAULT_PERMISSION)
		}
	}

	override fun hasPermission(permission: Permission): Boolean {
		return this.hasPermission(permission.name)
	}

	override fun addAttachment(plugin: Plugin, name: String?, value: Boolean?): PermissionAttachment {
		if (!plugin.isEnabled) {
			throw PluginException("Plugin " + plugin.description.name + " is disabled")
		}
		val result = PermissionAttachment(plugin, parent ?: this)
		attachments.add(result)
		if (name != null && value != null) {
			result.setPermission(name, value)
		}
		recalculatePermissions()
		return result
	}

	@Override
	override fun removeAttachment(attachment: PermissionAttachment) {
		if (attachments.contains(attachment)) {
			attachments.remove(attachment)
			val ex = attachment.removalCallback
			ex?.attachmentRemoved(attachment)
			recalculatePermissions()
		}
	}

	@Override
	override fun recalculatePermissions() {
		Timings.permissibleCalculationTimer.startTiming()
		clearPermissions()
		val defaults: Map<String, Permission> = Server.instance.pluginManager.getDefaultPermissions(isOp)
		Server.instance.pluginManager.subscribeToDefaultPerms(isOp, parent ?: this)
		defaults.values.forEach {
			val name: String = it.name
			permissions[name] = PermissionAttachmentInfo(parent ?: this, name, null, true)
			Server.instance.pluginManager.subscribeToPermission(name, parent ?: this)
			calculateChildPermissions(it.children, false, null)
		}
		for (attachment in attachments) {
			calculateChildPermissions(attachment.permissions, false, attachment)
		}
		Timings.permissibleCalculationTimer.stopTiming()
	}

	fun clearPermissions() {
		permissions.keys.forEach { name ->
			Server.instance.pluginManager.unsubscribeFromPermission(name, parent ?: this)
		}
		Server.instance.pluginManager.unsubscribeFromDefaultPerms(false, parent ?: this)
		Server.instance.pluginManager.unsubscribeFromDefaultPerms(true, parent ?: this)
		permissions.clear()
	}

	private fun calculateChildPermissions(children: Map<String, Boolean>, invert: Boolean, attachment: PermissionAttachment?) {
		children.forEach { (name, u) ->
			val perm = Server.instance.pluginManager.getPermission(name)
			val value = u xor invert
			permissions[name] = PermissionAttachmentInfo(parent ?: this, name, attachment, value)
			Server.instance.pluginManager.subscribeToPermission(name, parent ?: this)
			if (perm != null) {
				calculateChildPermissions(perm.children, !value, attachment)
			}
		}
	}
}