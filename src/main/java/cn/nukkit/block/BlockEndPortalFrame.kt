package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.network.protocol.LevelSoundEventPacket
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.Faceable

/**
 * Created by Pub4Game on 26.12.2015.
 */
class BlockEndPortalFrame @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta), Faceable {
	override val id: Int
		get() = BlockID.Companion.END_PORTAL_FRAME

	override val resistance: Double
		get() = 18000000

	override val hardness: Double
		get() = (-1).toDouble()

	override val lightLevel: Int
		get() = 1

	override val name: String
		get() = "End Portal Frame"

	override fun isBreakable(item: Item?): Boolean {
		return false
	}

	override fun getMaxY(): Double {
		return y + if (this.damage and 0x04 > 0) 1 else 0.8125
	}

	override fun canBePushed(): Boolean {
		return false
	}

	override fun hasComparatorInputOverride(): Boolean {
		return true
	}

	override val comparatorInputOverride: Int
		get() = if (damage and 4 != 0) 15 else 0

	override fun canBeActivated(): Boolean {
		return true
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (this.damage and 0x04 == 0 && player != null && item.id == Item.ENDER_EYE) {
			this.setDamage(this.damage + 4)
			getLevel().setBlock(this, this, true, true)
			getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_BLOCK_END_PORTAL_FRAME_FILL)
			//TODO: create portal
			return true
		}
		return false
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override fun toItem(): Item? {
		return ItemBlock(this, 0)
	}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x07)
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		this.setDamage(FACES[player?.direction?.horizontalIndex ?: 0])
		getLevel().setBlock(block, this, true)
		return true
	}

	override val color: BlockColor
		get() = BlockColor.GREEN_BLOCK_COLOR

	companion object {
		private val FACES = intArrayOf(2, 3, 0, 1)
	}
}