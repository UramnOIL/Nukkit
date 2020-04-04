package cn.nukkit.item

import cn.nukkit.block.Block

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemBlock @JvmOverloads constructor(block: Block, meta: Int? = 0, count: Int = 1) : Item(block.id, meta, count, block.name) {
	override var damage: Int?
		get() = super.damage
		set(meta) {
			if (meta != null) {
				this.meta = meta and 0xffff
			} else {
				hasMeta = false
			}
			block!!.setDamage(meta)
		}

	override fun clone(): ItemBlock {
		val block = super.clone() as ItemBlock
		block.block = this.block!!.clone()
		return block
	}

	override fun getBlock(): Block? {
		return block
	}

	//Shulker boxes don't stack!
	override val maxStackSize: Int
		get() =//Shulker boxes don't stack!
			if (getBlock()!!.id == Block.SHULKER_BOX || getBlock()!!.id == Block.UNDYED_SHULKER_BOX) {
				1
			} else super.getMaxStackSize()

	init {
		this.block = block
	}
}