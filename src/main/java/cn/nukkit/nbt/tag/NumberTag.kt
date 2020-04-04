package cn.nukkit.nbt.tag

import kotlin.jvm.Throws

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class NumberTag<T : Number?> protected constructor(name: String?) : Tag(name) {
	abstract var data: T
}