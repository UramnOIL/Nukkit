package cn.nukkit.entity

/**
 * @author Adam Matthew
 */
interface EntityInteractable {
	// Todo: Passive entity?? i18n and boat leaving text
	val interactButtonText: String
	fun canDoInteraction(): Boolean
}