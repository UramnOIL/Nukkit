package cn.nukkit.command

import cn.nukkit.Server
import cn.nukkit.lang.TextContainer
import cn.nukkit.permission.PermissibleBase
import cn.nukkit.permission.Permission
import cn.nukkit.permission.PermissionAttachment
import cn.nukkit.permission.PermissionAttachmentInfo
import cn.nukkit.plugin.Plugin
import cn.nukkit.utils.MainLogger

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class ConsoleCommandSender : CommandSender {
	private val perm: PermissibleBase
	override fun isPermissionSet(name: String): Boolean {
		return perm.isPermissionSet(name)
	}

	override fun isPermissionSet(permission: Permission): Boolean {
		return perm.isPermissionSet(permission)
	}

	override fun hasPermission(name: String): Boolean {
		return perm.hasPermission(name)
	}

	override fun hasPermission(permission: Permission): Boolean {
		return perm.hasPermission(permission)
	}

	override fun addAttachment(plugin: Plugin): PermissionAttachment {
		return perm.addAttachment(plugin)
	}

	override fun addAttachment(plugin: Plugin, name: String): PermissionAttachment {
		return perm.addAttachment(plugin, name)
	}

	override fun addAttachment(plugin: Plugin, name: String, value: Boolean): PermissionAttachment {
		return perm.addAttachment(plugin, name, value)
	}

	override fun removeAttachment(attachment: PermissionAttachment) {
		perm.removeAttachment(attachment)
	}

	override fun recalculatePermissions() {
		perm.recalculatePermissions()
	}

	override fun getEffectivePermissions(): Map<String, PermissionAttachmentInfo> {
		return perm.effectivePermissions
	}

	override val isPlayer: Boolean
		get() = false

	override val server: Server?
		get() = Server.instance

	override fun sendMessage(message: String) {
		var message = message
		message = server!!.language.translateString(message)
		for (line in message.trim { it <= ' ' }.split("\n").toTypedArray()) {
			MainLogger.getLogger().info(line)
		}
	}

	override fun sendMessage(message: TextContainer?) {
		this.sendMessage(server!!.language.translate(message))
	}

	override val name: String
		get() = "CONSOLE"

	override fun isOp(): Boolean {
		return true
	}

	override fun setOp(value: Boolean) {}

	init {
		perm = PermissibleBase(this)
	}
}