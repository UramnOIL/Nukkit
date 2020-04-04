package cn.nukkit.lang

import cn.nukkit.Server

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class TextContainer(var text: String) : Cloneable {

	override fun toString(): String {
		return text
	}

	public override fun clone(): TextContainer {
		try {
			return super.clone() as TextContainer
		} catch (e: CloneNotSupportedException) {
			Server.instance!!.logger.logException(e)
		}
		return null
	}

}