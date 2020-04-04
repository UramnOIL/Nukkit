package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.event.block.BlockGrowEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemSugarcane
import cn.nukkit.level.Level
import cn.nukkit.level.particle.BoneMealParticle
import cn.nukkit.math.BlockFace
import cn.nukkit.math.Vector3
import cn.nukkit.utils.BlockColor

/**
 * Created by Pub4Game on 09.01.2016.
 */
class BlockSugarcane @JvmOverloads constructor(meta: Int = 0) : BlockFlowable(meta) {
	override val name: String
		get() = "Sugarcane"

	override val id: Int
		get() = BlockID.Companion.SUGARCANE_BLOCK

	override fun toItem(): Item? {
		return ItemSugarcane()
	}

	override fun canBeActivated(): Boolean {
		return true
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (item.id == Item.DYE && item.damage == 0x0F) { //Bonemeal
			var count = 1
			for (i in 1..2) {
				val id = level.getBlockIdAt(this.floorX, this.floorY - i, this.floorZ)
				if (id == BlockID.Companion.SUGARCANE_BLOCK) {
					count++
				}
			}
			if (count < 3) {
				var success = false
				val toGrow = 3 - count
				for (i in 1..toGrow) {
					val block = this.up(i)
					if (block.id == 0) {
						val ev = BlockGrowEvent(block, Block.Companion.get(BlockID.Companion.SUGARCANE_BLOCK))
						Server.instance!!.pluginManager.callEvent(ev)
						if (!ev.isCancelled) {
							getLevel().setBlock(block, ev.newState, true)
							success = true
						}
					} else if (block.id != BlockID.Companion.SUGARCANE_BLOCK) {
						break
					}
				}
				if (success) {
					if (player != null && player.gamemode and 0x01 == 0) {
						item.count--
					}
					level.addParticle(BoneMealParticle(this))
				}
			}
			return true
		}
		return false
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			val down = this.down()
			if (down!!.isTransparent && down.id != BlockID.Companion.SUGARCANE_BLOCK) {
				getLevel().useBreakOn(this)
				return Level.BLOCK_UPDATE_NORMAL
			}
		} else if (type == Level.BLOCK_UPDATE_RANDOM) {
			if (this.down().id != BlockID.Companion.SUGARCANE_BLOCK) {
				if (this.damage == 0x0F) {
					for (y in 1..2) {
						val b = getLevel().getBlock(Vector3(x, this.y + y, z))
						if (b.id == BlockID.Companion.AIR) {
							getLevel().setBlock(b, Block.Companion.get(BlockID.Companion.SUGARCANE_BLOCK), false)
							break
						}
					}
					this.setDamage(0)
					getLevel().setBlock(this, this, false)
				} else {
					this.setDamage(this.damage + 1)
					getLevel().setBlock(this, this, false)
				}
				return Level.BLOCK_UPDATE_RANDOM
			}
		}
		return 0
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (block.id != BlockID.Companion.AIR) {
			return false
		}
		val down = this.down()
		if (down.id == BlockID.Companion.SUGARCANE_BLOCK) {
			getLevel().setBlock(block, Block.Companion.get(BlockID.Companion.SUGARCANE_BLOCK), true)
			return true
		} else if (down.id == BlockID.Companion.GRASS || down.id == BlockID.Companion.DIRT || down.id == BlockID.Companion.SAND) {
			val block0 = down!!.north()
			val block1 = down.south()
			val block2 = down.west()
			val block3 = down.east()
			if (block0 is BlockWater || block1 is BlockWater || block2 is BlockWater || block3 is BlockWater) {
				getLevel().setBlock(block, Block.Companion.get(BlockID.Companion.SUGARCANE_BLOCK), true)
				return true
			}
		}
		return false
	}

	override val color: BlockColor
		get() = BlockColor.FOLIAGE_BLOCK_COLOR
}