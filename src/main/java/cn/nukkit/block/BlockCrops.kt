package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.event.block.BlockGrowEvent
import cn.nukkit.item.Item
import cn.nukkit.level.Level
import cn.nukkit.level.particle.BoneMealParticle
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor
import java.util.concurrent.ThreadLocalRandom

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class BlockCrops protected constructor(meta: Int) : BlockFlowable(meta) {
	override fun canBeActivated(): Boolean {
		return true
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (block.down().id == BlockID.Companion.FARMLAND) {
			getLevel().setBlock(block, this, true, true)
			return true
		}
		return false
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		//Bone meal
		if (item.id == Item.DYE && item.damage == 0x0f) {
			if (this.damage < 7) {
				val block = clone() as BlockCrops
				block.setDamage(block.damage + ThreadLocalRandom.current().nextInt(3) + 2)
				if (block.damage > 7) {
					block.setDamage(7)
				}
				val ev = BlockGrowEvent(this, block)
				Server.instance!!.pluginManager.callEvent(ev)
				if (ev.isCancelled) {
					return false
				}
				getLevel().setBlock(this, ev.newState, false, true)
				level.addParticle(BoneMealParticle(this))
				if (player != null && player.gamemode and 0x01 == 0) {
					item.count--
				}
			}
			return true
		}
		return false
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			if (this.down().id != BlockID.Companion.FARMLAND) {
				getLevel().useBreakOn(this)
				return Level.BLOCK_UPDATE_NORMAL
			}
		} else if (type == Level.BLOCK_UPDATE_RANDOM) {
			if (ThreadLocalRandom.current().nextInt(2) == 1) {
				if (this.damage < 0x07) {
					val block = clone() as BlockCrops
					block.setDamage(block.damage + 1)
					val ev = BlockGrowEvent(this, block)
					Server.instance!!.pluginManager.callEvent(ev)
					if (!ev.isCancelled) {
						getLevel().setBlock(this, ev.newState, false, true)
					} else {
						return Level.BLOCK_UPDATE_RANDOM
					}
				}
			} else {
				return Level.BLOCK_UPDATE_RANDOM
			}
		}
		return 0
	}

	override val color: BlockColor
		get() = BlockColor.FOLIAGE_BLOCK_COLOR
}