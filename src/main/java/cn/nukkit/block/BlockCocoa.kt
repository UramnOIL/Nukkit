package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.event.block.BlockGrowEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemDye
import cn.nukkit.item.ItemTool
import cn.nukkit.level.Level
import cn.nukkit.level.particle.BoneMealParticle
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.BlockFace
import cn.nukkit.math.SimpleAxisAlignedBB
import cn.nukkit.utils.DyeColor
import cn.nukkit.utils.Faceable
import java.util.concurrent.ThreadLocalRandom

/**
 * Created by CreeperFace on 27. 10. 2016.
 */
class BlockCocoa @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta), Faceable {
	override val id: Int
		get() = BlockID.Companion.COCOA

	override val name: String
		get() = "Cocoa"

	override var damage: Int
		get() = super.damage
		set(meta) {
			super.setDamage(meta)
		}

	override fun getMinX(): Double {
		return x + relativeBoundingBox.minX
	}

	override fun getMaxX(): Double {
		return x + relativeBoundingBox.maxX
	}

	override fun getMinY(): Double {
		return y + relativeBoundingBox.minY
	}

	override fun getMaxY(): Double {
		return y + relativeBoundingBox.maxY
	}

	override fun getMinZ(): Double {
		return z + relativeBoundingBox.minZ
	}

	override fun getMaxZ(): Double {
		return z + relativeBoundingBox.maxZ
	}

	private val relativeBoundingBox: AxisAlignedBB
		private get() {
			var damage = getDamage()
			if (damage > 11) {
				this.damage = 11.also { damage = it }
			}
			val boundingBox = ALL[damage]
			if (boundingBox != null) return boundingBox
			val bbs: Array<AxisAlignedBB>
			bbs = when (getDamage()) {
				0, 4, 8 -> NORTH
				1, 5, 9 -> EAST
				2, 6, 10 -> SOUTH
				3, 7, 11 -> WEST
				else -> NORTH
			}
			return bbs[getDamage() shr 2].also { ALL[damage] = it }
		}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (target.id == BlockID.Companion.WOOD && target.damage and 0x03 == BlockWood.Companion.JUNGLE) {
			if (face != BlockFace.DOWN && face != BlockFace.UP) {
				val faces = intArrayOf(
						0,
						0,
						0,
						2,
						3,
						1)
				damage = faces[face.index]
				level.setBlock(block, this, true, true)
				return true
			}
		}
		return false
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			val faces = intArrayOf(
					3, 4, 2, 5, 3, 4, 2, 5, 3, 4, 2, 5
			)
			val side = this.getSide(BlockFace.fromIndex(faces[getDamage()]))
			if (side.id != BlockID.Companion.WOOD && side.damage != BlockWood.Companion.JUNGLE) {
				getLevel().useBreakOn(this)
				return Level.BLOCK_UPDATE_NORMAL
			}
		} else if (type == Level.BLOCK_UPDATE_RANDOM) {
			if (ThreadLocalRandom.current().nextInt(2) == 1) {
				if (getDamage() / 4 < 2) {
					val block = clone() as BlockCocoa
					block.damage = block.getDamage() + 4
					val ev = BlockGrowEvent(this, block)
					Server.instance!!.pluginManager.callEvent(ev)
					if (!ev.isCancelled) {
						getLevel().setBlock(this, ev.newState, true, true)
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

	override fun canBeActivated(): Boolean {
		return true
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (item.id == Item.DYE && item.damage == 0x0f) {
			val block = clone()
			if (getDamage() / 4 < 2) {
				block.setDamage(block.damage + 4)
				val ev = BlockGrowEvent(this, block)
				Server.instance!!.pluginManager.callEvent(ev)
				if (ev.isCancelled) {
					return false
				}
				getLevel().setBlock(this, ev.newState, true, true)
				level.addParticle(BoneMealParticle(this))
				if (player != null && player.gamemode and 0x01 == 0) {
					item.count--
				}
			}
			return true
		}
		return false
	}

	override val resistance: Double
		get() = 15

	override val hardness: Double
		get() = 0.2

	override val toolType: Int
		get() = ItemTool.TYPE_AXE

	override fun toItem(): Item? {
		return ItemDye(DyeColor.BROWN.dyeData)
	}

	override fun getDrops(item: Item): Array<Item?> {
		return if (getDamage() >= 8) {
			arrayOf(
					ItemDye(3, 3)
			)
		} else {
			arrayOf(
					ItemDye(3, 1)
			)
		}
	}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(getDamage() and 0x07)
	}

	companion object {
		protected val EAST: Array<AxisAlignedBB> = arrayOf<SimpleAxisAlignedBB>(SimpleAxisAlignedBB(0.6875, 0.4375, 0.375, 0.9375, 0.75, 0.625), SimpleAxisAlignedBB(0.5625, 0.3125, 0.3125, 0.9375, 0.75, 0.6875), SimpleAxisAlignedBB(0.5625, 0.3125, 0.3125, 0.9375, 0.75, 0.6875))
		protected val WEST: Array<AxisAlignedBB> = arrayOf<SimpleAxisAlignedBB>(SimpleAxisAlignedBB(0.0625, 0.4375, 0.375, 0.3125, 0.75, 0.625), SimpleAxisAlignedBB(0.0625, 0.3125, 0.3125, 0.4375, 0.75, 0.6875), SimpleAxisAlignedBB(0.0625, 0.3125, 0.3125, 0.4375, 0.75, 0.6875))
		protected val NORTH: Array<AxisAlignedBB> = arrayOf<SimpleAxisAlignedBB>(SimpleAxisAlignedBB(0.375, 0.4375, 0.0625, 0.625, 0.75, 0.3125), SimpleAxisAlignedBB(0.3125, 0.3125, 0.0625, 0.6875, 0.75, 0.4375), SimpleAxisAlignedBB(0.3125, 0.3125, 0.0625, 0.6875, 0.75, 0.4375))
		protected val SOUTH: Array<AxisAlignedBB> = arrayOf<SimpleAxisAlignedBB>(SimpleAxisAlignedBB(0.375, 0.4375, 0.6875, 0.625, 0.75, 0.9375), SimpleAxisAlignedBB(0.3125, 0.3125, 0.5625, 0.6875, 0.75, 0.9375), SimpleAxisAlignedBB(0.3125, 0.3125, 0.5625, 0.6875, 0.75, 0.9375))
		protected val ALL = arrayOfNulls<AxisAlignedBB>(12)
	}
}