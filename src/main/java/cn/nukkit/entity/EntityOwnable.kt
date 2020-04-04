package cn.nukkit.entity

import cn.nukkit.Player

/**
 * Author: BeYkeRYkt
 * Nukkit Project
 */
interface EntityOwnable {
	fun getOwnerName(): String?
	fun setOwnerName(playerName: String)
	val owner: Player?
}