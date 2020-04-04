package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemRedstoneRepeater
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace

/**
 * Created by CreeperFace on 10.4.2017.
 */
class BlockRedstoneRepeaterUnpowered @JvmOverloads constructor(meta: Int = 0) : BlockRedstoneDiode(meta) {
	override val id: Int
		get() = BlockID.Companion.UNPOWERED_REPEATER

	override val name: String
		get() = "Unpowered Repeater"

	override fun onActivate(item: Item, player: Player?): Boolean {
		this.setDamage(this.damage + 4)
		if (this.damage > 15) this.setDamage(this.damage % 4)
		level.setBlock(this, this, true, false)
		return true
	}

	override val facing: BlockFace
		get() = BlockFace.fromHorizontalIndex(damage)

	override fun isAlternateInput(block: Block): Boolean {
		return BlockRedstoneDiode.Companion.isDiode(block)
	}

	override fun toItem(): Item? {
		return ItemRedstoneRepeater()
	}

	protected override val delay: Int
		protected get() = (1 + (damage shr 2)) * 2

	override fun getPowered(): Block {
		return Block.Companion.get(BlockID.Companion.POWERED_REPEATER, this.damage)
	}

	protected override val unpowered: Block
		protected get() = this

	override val isLocked: Boolean
		get() = this.powerOnSides > 0

	init {
		isPowered = false
	}
}