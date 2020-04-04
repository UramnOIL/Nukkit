package cn.nukkit.permission

import cn.nukkit.plugin.Plugin

/**
 * author: MagicDroidX
 * Nukkit Project
 */
interface Permissible : ServerOperator {
	val permissions: Map<String, PermissionAttachmentInfo>
	fun hasPermission(name: String): Boolean
	fun hasPermission(permission: Permission): Boolean
	fun addAttachment(plugin: Plugin, name: String?, value: Boolean?): PermissionAttachment
	fun removeAttachment(attachment: PermissionAttachment)
	fun recalculatePermissions()
	val effectivePermissions: Map<String, Any>
}