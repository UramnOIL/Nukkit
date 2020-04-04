package cn.nukkit.potion

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class InstantEffect : Effect {
	constructor(id: Int, name: String, r: Int, g: Int, b: Int) : super(id, name, r, g, b) {}
	constructor(id: Int, name: String, r: Int, g: Int, b: Int, isBad: Boolean) : super(id, name, r, g, b, isBad) {}
}