package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemStick
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor
import java.util.*

/**
 * Created on 2015/12/2 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockDeadBush @JvmOverloads constructor(meta: Int = 0) : BlockFlowable(0) {
	override val name: String
		get() = "Dead Bush"

	override val id: Int
		get() = BlockID.Companion.DEAD_BUSH

	override fun canBeReplaced(): Boolean {
		return true
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val down = this.down()
		if (down.id == BlockID.Companion.SAND || down.id == BlockID.Companion.TERRACOTTA || down.id == BlockID.Companion.STAINED_TERRACOTTA || down.id == BlockID.Companion.DIRT || down.id == BlockID.Companion.PODZOL) {
			getLevel().setBlock(block, this, true, true)
			return true
		}
		return false
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			if (this.down().isTransparent) {
				getLevel().useBreakOn(this)
				return Level.BLOCK_UPDATE_NORMAL
			}
		}
		return 0
	}

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isShears) {
			arrayOf(
					toItem()
			)
		} else {
			arrayOf(
					ItemStick(0, Random().nextInt(3))
			)
		}
	}

	override val color: BlockColor
		get() = BlockColor.FOLIAGE_BLOCK_COLOR
}