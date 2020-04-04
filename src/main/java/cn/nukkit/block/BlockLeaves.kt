package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.event.block.LeavesDecayEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.Hash
import it.unimi.dsi.fastutil.longs.LongArraySet
import it.unimi.dsi.fastutil.longs.LongSet
import java.util.concurrent.ThreadLocalRandom

/**
 * author: Angelic47
 * Nukkit Project
 */
open class BlockLeaves @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta) {
	override val id: Int
		get() = BlockID.Companion.LEAVES

	override val hardness: Double
		get() = 0.2

	override val toolType: Int
		get() = ItemTool.TYPE_SHEARS

	override val name: String
		get() {
			val names = arrayOf(
					"Oak Leaves",
					"Spruce Leaves",
					"Birch Leaves",
					"Jungle Leaves"
			)
			return names[this.damage and 0x03]
		}

	override val burnChance: Int
		get() = 30

	override val burnAbility: Int
		get() = 60

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		this.persistent = true
		getLevel().setBlock(this, this, true)
		return true
	}

	override fun toItem(): Item? {
		return ItemBlock(this, this.damage and 0x3, 1)
	}

	override fun getDrops(item: Item): Array<Item?> {
		if (item.isShears) {
			return arrayOf(
					toItem()
			)
		} else {
			if (canDropApple() && ThreadLocalRandom.current().nextInt(200) == 0) {
				return arrayOf(
						Item.get(Item.APPLE)
				)
			}
			if (ThreadLocalRandom.current().nextInt(20) == 0) {
				if (ThreadLocalRandom.current().nextBoolean()) {
					return arrayOf(
							Item.get(Item.STICK, 0, ThreadLocalRandom.current().nextInt(1, 2))
					)
				} else if (this.damage and 0x03 != JUNGLE || ThreadLocalRandom.current().nextInt(20) == 0) {
					return arrayOf(
							sapling
					)
				}
			}
		}
		return arrayOfNulls(0)
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_RANDOM && !isPersistent && !isCheckDecay) {
			checkDecay = true
			getLevel().setBlock(this, this, false, false)
		} else if (type == Level.BLOCK_UPDATE_RANDOM && isCheckDecay && !isPersistent) {
			setDamage(damage and 0x03)
			val check = 0
			val ev = LeavesDecayEvent(this)
			Server.instance!!.pluginManager.callEvent(ev)
			if (ev.isCancelled || findLog(this, LongArraySet(), 0, check)) {
				getLevel().setBlock(this, this, false, false)
			} else {
				getLevel().useBreakOn(this)
				return Level.BLOCK_UPDATE_NORMAL
			}
		}
		return 0
	}

	private fun findLog(pos: Block?, visited: LongSet, distance: Int, check: Int, fromSide: BlockFace? = null): Boolean {
		var check = check
		++check
		val index = Hash.hashBlock(pos!!.x.toInt(), pos.y.toInt(), pos.z.toInt())
		if (visited.contains(index)) return false
		if (pos.id == BlockID.Companion.WOOD || pos.id == BlockID.Companion.WOOD2) return true
		if ((pos.id == BlockID.Companion.LEAVES || pos.id == BlockID.Companion.LEAVES2) && distance <= 4) {
			visited.add(index)
			val down = pos.down().id
			if (down == BlockID.Companion.WOOD || down == BlockID.Companion.WOOD2) {
				return true
			}
			if (fromSide == null) {
				//North, East, South, West
				for (side in 2..5) {
					if (findLog(pos.getSide(BlockFace.fromIndex(side)), visited, distance + 1, check, BlockFace.fromIndex(side))) return true
				}
			} else { //No more loops
				when (fromSide) {
					BlockFace.NORTH -> {
						if (findLog(pos.getSide(BlockFace.NORTH), visited, distance + 1, check, fromSide)) return true
						if (findLog(pos.getSide(BlockFace.WEST), visited, distance + 1, check, fromSide)) return true
						if (findLog(pos.getSide(BlockFace.EAST), visited, distance + 1, check, fromSide)) return true
					}
					BlockFace.SOUTH -> {
						if (findLog(pos.getSide(BlockFace.SOUTH), visited, distance + 1, check, fromSide)) return true
						if (findLog(pos.getSide(BlockFace.WEST), visited, distance + 1, check, fromSide)) return true
						if (findLog(pos.getSide(BlockFace.EAST), visited, distance + 1, check, fromSide)) return true
					}
					BlockFace.WEST -> {
						if (findLog(pos.getSide(BlockFace.NORTH), visited, distance + 1, check, fromSide)) return true
						if (findLog(pos.getSide(BlockFace.SOUTH), visited, distance + 1, check, fromSide)) return true
						if (findLog(pos.getSide(BlockFace.WEST), visited, distance + 1, check, fromSide)) return true
						if (findLog(pos.getSide(BlockFace.NORTH), visited, distance + 1, check, fromSide)) return true
						if (findLog(pos.getSide(BlockFace.SOUTH), visited, distance + 1, check, fromSide)) return true
						if (findLog(pos.getSide(BlockFace.EAST), visited, distance + 1, check, fromSide)) return true
					}
					BlockFace.EAST -> {
						if (findLog(pos.getSide(BlockFace.NORTH), visited, distance + 1, check, fromSide)) return true
						if (findLog(pos.getSide(BlockFace.SOUTH), visited, distance + 1, check, fromSide)) return true
						if (findLog(pos.getSide(BlockFace.EAST), visited, distance + 1, check, fromSide)) return true
					}
				}
			}
		}
		return false
	}

	var isCheckDecay: Boolean
		get() = this.damage and 0x08 != 0
		set(checkDecay) {
			if (checkDecay) {
				this.setDamage(this.damage or 0x08)
			} else {
				this.setDamage(this.damage and 0x08.inv())
			}
		}

	var isPersistent: Boolean
		get() = this.damage and 0x04 != 0
		set(persistent) {
			if (persistent) {
				this.setDamage(this.damage or 0x04)
			} else {
				this.setDamage(this.damage and 0x04.inv())
			}
		}

	override val color: BlockColor
		get() = BlockColor.FOLIAGE_BLOCK_COLOR

	override fun canSilkTouch(): Boolean {
		return true
	}

	protected open fun canDropApple(): Boolean {
		return this.damage and 0x03 == OAK
	}

	protected open val sapling: Item?
		protected get() = Item.get(BlockID.Companion.SAPLING, this.damage and 0x03)

	companion object {
		const val OAK = 0
		const val SPRUCE = 1
		const val BIRCH = 2
		const val JUNGLE = 3
	}
}