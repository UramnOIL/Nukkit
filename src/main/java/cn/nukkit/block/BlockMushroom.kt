package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.level.Level
import cn.nukkit.level.particle.BoneMealParticle
import cn.nukkit.math.BlockFace
import cn.nukkit.math.NukkitRandom
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.DyeColor
import java.util.concurrent.ThreadLocalRandom

abstract class BlockMushroom @JvmOverloads constructor(meta: Int = 0) : BlockFlowable(0) {
	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			if (!canStay()) {
				getLevel().useBreakOn(this)
				return Level.BLOCK_UPDATE_NORMAL
			}
		}
		return 0
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (canStay()) {
			getLevel().setBlock(block, this, true, true)
			return true
		}
		return false
	}

	override fun canBeActivated(): Boolean {
		return true
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (item.id == Item.DYE && item.damage == DyeColor.WHITE.dyeData) {
			if (player != null && player.gamemode and 0x01 == 0) {
				item.count--
			}
			if (ThreadLocalRandom.current().nextFloat() < 0.4) {
				this.grow()
			}
			level.addParticle(BoneMealParticle(this))
			return true
		}
		return false
	}

	fun grow(): Boolean {
		level.setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true, false)
		val generator = BigMushroom(type)
		return if (generator.generate(level, NukkitRandom(), this)) {
			true
		} else {
			level.setBlock(this, this, true, false)
			false
		}
	}

	fun canStay(): Boolean {
		val block = this.down()
		return block.id == BlockID.Companion.MYCELIUM || block.id == BlockID.Companion.PODZOL || !block!!.isTransparent && level.getFullLight(this) < 13
	}

	override val color: BlockColor
		get() = BlockColor.FOLIAGE_BLOCK_COLOR

	override fun canSilkTouch(): Boolean {
		return true
	}

	protected abstract val type: Int
}