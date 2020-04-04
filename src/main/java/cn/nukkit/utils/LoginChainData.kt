package cn.nukkit.utils

import java.util.*

/**
 * @author CreeperFace
 */
interface LoginChainData {
	fun getUsername(): String?
	fun getClientUUID(): UUID?
	fun getIdentityPublicKey(): String?
	fun getClientId(): Long
	fun getServerAddress(): String?
	fun getDeviceModel(): String?
	fun getDeviceOS(): Int
	fun getDeviceId(): String?
	fun getGameVersion(): String?
	fun getGuiScale(): Int
	fun getLanguageCode(): String?
	fun getXUID(): String?
	fun isXboxAuthed(): Boolean
	fun getCurrentInputMode(): Int
	fun getDefaultInputMode(): Int
	fun getCapeData(): String?
	fun getUIProfile(): Int
}