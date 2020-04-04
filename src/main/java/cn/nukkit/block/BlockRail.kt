package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.Level
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.Faceable
import cn.nukkit.utils.Rail
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * Created by Snake1999 on 2016/1/11.
 * Package cn.nukkit.block in project nukkit
 */
open class BlockRail @JvmOverloads constructor(meta: Int = 0) : BlockFlowable(meta), Faceable {
	// 0x8: Set the block active
	// 0x7: Reset the block to normal
	// If the rail can be powered. So its a complex rail!
	protected var canBePowered = false
	override val name: String
		get() = "Rail"

	override val id: Int
		get() = BlockID.Companion.RAIL

	override val hardness: Double
		get() = 0.7

	override val resistance: Double
		get() = 3.5

	override fun canPassThrough(): Boolean {
		return true
	}

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			val ascendingDirection = orientation.ascendingDirection()
			if (this.down().isTransparent || ascendingDirection.isPresent && this.getSide(ascendingDirection.get()).isTransparent) {
				getLevel().useBreakOn(this)
				return Level.BLOCK_UPDATE_NORMAL
			}
		}
		return 0
	}

	override fun getMaxY(): Double {
		return y + 0.125
	}

	public override fun recalculateBoundingBox(): AxisAlignedBB? {
		return this
	}

	override val color: BlockColor
		get() = BlockColor.AIR_BLOCK_COLOR

	//Information from http://minecraft.gamepedia.com/Rail
	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val down = this.down()
		if (down == null || down.isTransparent) {
			return false
		}
		val railsAround = checkRailsAroundAffected()
		val rails: List<BlockRail> = ArrayList(railsAround.keys)
		val faces: List<BlockFace> = ArrayList(railsAround.values)
		if (railsAround.size == 1) {
			val other = rails[0]
			this.setDamage(this.connect(other, railsAround[other]).metadata())
		} else if (railsAround.size == 4) {
			if (isAbstract) {
				this.setDamage(this.connect(rails[faces.indexOf(BlockFace.SOUTH)], BlockFace.SOUTH, rails[faces.indexOf(BlockFace.EAST)], BlockFace.EAST).metadata())
			} else {
				this.setDamage(this.connect(rails[faces.indexOf(BlockFace.EAST)], BlockFace.EAST, rails[faces.indexOf(BlockFace.WEST)], BlockFace.WEST).metadata())
			}
		} else if (!railsAround.isEmpty()) {
			if (isAbstract) {
				if (railsAround.size == 2) {
					val rail1 = rails[0]
					val rail2 = rails[1]
					this.setDamage(this.connect(rail1, railsAround[rail1], rail2, railsAround[rail2]).metadata())
				} else {
					val cd = Stream.of(Rail.Orientation.CURVED_SOUTH_EAST, Rail.Orientation.CURVED_NORTH_EAST, Rail.Orientation.CURVED_SOUTH_WEST)
							.filter { o: Rail.Orientation -> faces.containsAll(o.connectingDirections()) }
							.findFirst().get().connectingDirections()
					val f1 = cd[0]
					val f2 = cd[1]
					this.setDamage(this.connect(rails[faces.indexOf(f1)], f1, rails[faces.indexOf(f2)], f2).metadata())
				}
			} else {
				val f = faces.stream().min { f1: BlockFace, f2: BlockFace -> if (f1.index < f2.index) 1 else if (x == y) 0 else -1 }.get()
				val fo = f.opposite
				if (faces.contains(fo)) { //Opposite connectable
					this.setDamage(this.connect(rails[faces.indexOf(f)], f, rails[faces.indexOf(fo)], fo).metadata())
				} else {
					this.setDamage(this.connect(rails[faces.indexOf(f)], f).metadata())
				}
			}
		}
		level.setBlock(this, this, true, true)
		if (!isAbstract) {
			level.scheduleUpdate(this, this, 0)
		}
		return true
	}

	private fun connect(rail1: BlockRail, face1: BlockFace?, rail2: BlockRail, face2: BlockFace?): Rail.Orientation {
		this.connect(rail1, face1)
		this.connect(rail2, face2)
		if (face1!!.opposite == face2) {
			val delta1 = (y - rail1.y).toInt()
			val delta2 = (y - rail2.y).toInt()
			if (delta1 == -1) {
				return Rail.Orientation.ascending(face1)
			} else if (delta2 == -1) {
				return Rail.Orientation.ascending(face2)
			}
		}
		return Rail.Orientation.straightOrCurved(face1, face2)
	}

	private fun connect(other: BlockRail, face: BlockFace?): Rail.Orientation {
		val delta = (y - other.y).toInt()
		val rails = other.checkRailsConnected()
		if (rails.isEmpty()) { //Only one
			other.orientation = if (delta == 1) Rail.Orientation.ascending(face!!.opposite) else Rail.Orientation.straight(face)
			return if (delta == -1) Rail.Orientation.ascending(face) else Rail.Orientation.straight(face)
		} else if (rails.size == 1) { //Already connected
			val faceConnected = rails.values.iterator().next()
			if (other.isAbstract && faceConnected != face) { //Curve!
				other.orientation = Rail.Orientation.curved(face!!.opposite, faceConnected)
				return if (delta == -1) Rail.Orientation.ascending(face) else Rail.Orientation.straight(face)
			} else if (faceConnected == face) { //Turn!
				if (!other.orientation.isAscending) {
					other.orientation = if (delta == 1) Rail.Orientation.ascending(face.opposite) else Rail.Orientation.straight(face)
				}
				return if (delta == -1) Rail.Orientation.ascending(face) else Rail.Orientation.straight(face)
			} else if (other.orientation.hasConnectingDirections(BlockFace.NORTH, BlockFace.SOUTH)) { //North-south
				other.orientation = if (delta == 1) Rail.Orientation.ascending(face!!.opposite) else Rail.Orientation.straight(face)
				return if (delta == -1) Rail.Orientation.ascending(face) else Rail.Orientation.straight(face)
			}
		}
		return Rail.Orientation.STRAIGHT_NORTH_SOUTH
	}

	private fun checkRailsAroundAffected(): Map<BlockRail, BlockFace> {
		val railsAround = checkRailsAround(Arrays.asList(BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH))
		return railsAround.keys.stream()
				.filter { r: BlockRail -> r.checkRailsConnected().size != 2 }
				.collect(Collectors.toMap({ r: BlockRail? -> r }) { key: BlockRail -> railsAround[key] })
	}

	private fun checkRailsAround(faces: Collection<BlockFace>): Map<BlockRail, BlockFace> {
		val result: MutableMap<BlockRail, BlockFace> = HashMap()
		faces.forEach(Consumer { f: BlockFace ->
			val b = this.getSide(f)
			Stream.of(b, b!!.up(), b.down())
					.filter { block: Block? -> Rail.isRailBlock(block) }
					.forEach { block: Block -> result[block as BlockRail] = f }
		})
		return result
	}

	protected fun checkRailsConnected(): Map<BlockRail, BlockFace> {
		val railsAround = checkRailsAround(orientation.connectingDirections())
		return railsAround.keys.stream()
				.filter { r: BlockRail -> r.orientation.hasConnectingDirections(railsAround[r]!!.opposite) }
				.collect(Collectors.toMap({ r: BlockRail? -> r }) { key: BlockRail -> railsAround[key] })
	}

	val isAbstract: Boolean
		get() = id == BlockID.Companion.RAIL

	fun canPowered(): Boolean {
		return canBePowered
	}

	var orientation: Rail.Orientation
		get() = Rail.Orientation.byMetadata(realMeta)
		set(o) {
			if (o.metadata() != realMeta) {
				this.setDamage(o.metadata())
				level.setBlock(this, this, false, true)
			}
		}

	// Check if this can be powered
	// Avoid modifying the value from meta (The rail orientation may be false)
	// Reason: When the rail is curved, the meta will return STRAIGHT_NORTH_SOUTH.
	// OR Null Pointer Exception
	// Return the default: This meta
	val realMeta: Int
		get() =// Check if this can be powered
		// Avoid modifying the value from meta (The rail orientation may be false)
		// Reason: When the rail is curved, the meta will return STRAIGHT_NORTH_SOUTH.
				// OR Null Pointer Exception
			if (!isAbstract) {
				damage and 0x7
			} else damage
	// Return the default: This meta

	var isActive: Boolean
		get() = damage and 0x8 != 0
		set(active) {
			if (active) {
				setDamage(damage or 0x8)
			} else {
				setDamage(damage and 0x7)
			}
			level.setBlock(this, this, true, true)
		}

	override fun toItem(): Item? {
		return ItemBlock(this, 0)
	}

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOf(
				Item.get(Item.RAIL, 0, 1)
		)
	}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x07)
	}
}