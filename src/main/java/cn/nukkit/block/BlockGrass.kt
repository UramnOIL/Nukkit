package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.event.block.BlockSpreadEvent
import cn.nukkit.item.Item
import cn.nukkit.level.Level
import cn.nukkit.level.particle.BoneMealParticle
import cn.nukkit.math.NukkitRandom
import cn.nukkit.math.Vector3
import cn.nukkit.utils.BlockColor

/**
 * author: Angelic47
 * Nukkit Project
 */
open class BlockGrass @JvmOverloads constructor(meta: Int = 0) : BlockDirt(0) {
	override val id: Int
		get() = BlockID.Companion.GRASS

	override val hardness: Double
		get() = 0.6

	override val resistance: Double
		get() = 3

	override val name: String
		get() = "Grass Block"

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (item.id == Item.DYE && item.damage == 0x0F) {
			if (player != null && player.gamemode and 0x01 == 0) {
				item.count--
			}
			level.addParticle(BoneMealParticle(this))
			ObjectTallGrass.growGrass(getLevel(), this, NukkitRandom())
			return true
		} else if (item.isHoe) {
			item.useOn(this)
			getLevel().setBlock(this, Block.Companion.get(BlockID.Companion.FARMLAND))
			return true
		} else if (item.isShovel) {
			item.useOn(this)
			getLevel().setBlock(this, Block.Companion.get(BlockID.Companion.GRASS_PATH))
			return true
		}
		return false
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_RANDOM) {
			val random = NukkitRandom()
			x = random.nextRange(x.toInt() - 1, x.toInt() + 1).toDouble()
			y = random.nextRange(y.toInt() - 2, y.toInt() + 2).toDouble()
			z = random.nextRange(z.toInt() - 1, z.toInt() + 1).toDouble()
			val block = getLevel().getBlock(Vector3(x, y, z))
			if (block.id == BlockID.Companion.DIRT && block.damage == 0) {
				if (block.up() is BlockAir) {
					val ev = BlockSpreadEvent(block, this, Block.Companion.get(BlockID.Companion.GRASS))
					Server.instance!!.pluginManager.callEvent(ev)
					if (!ev.isCancelled) {
						getLevel().setBlock(block, ev.newState)
					}
				}
			} else if (block.id == BlockID.Companion.GRASS) {
				if (block.up() is BlockSolid) {
					val ev = BlockSpreadEvent(block, this, Block.Companion.get(BlockID.Companion.DIRT))
					Server.instance!!.pluginManager.callEvent(ev)
					if (!ev.isCancelled) {
						getLevel().setBlock(block, ev.newState)
					}
				}
			}
		}
		return 0
	}

	override val color: BlockColor
		get() = BlockColor.GRASS_BLOCK_COLOR

	override fun canSilkTouch(): Boolean {
		return true
	}

	override val fullId: Int
		get() = id shl 4

	override var damage: Int
		get() = super.damage
		set(meta) {}
}