package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/11/22 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockPodzol @JvmOverloads constructor(meta: Int = 0) : BlockDirt(0) {
	override val id: Int
		get() = BlockID.Companion.PODZOL

	override val name: String
		get() = "Podzol"

	override fun canSilkTouch(): Boolean {
		return true
	}

	override fun canBeActivated(): Boolean {
		return false
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		return false
	}

	override val fullId: Int
		get() = id shl 4

	override var damage: Int
		get() = super.damage
		set(meta) {}
	override val color: BlockColor
		get() = BlockColor.SPRUCE_BLOCK_COLOR
}