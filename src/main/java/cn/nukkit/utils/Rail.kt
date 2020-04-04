package cn.nukkit.utils

import cn.nukkit.block.Block
import cn.nukkit.math.BlockFace
import java.util.*
import java.util.function.Predicate
import java.util.stream.Stream

/**
 * INTERNAL helper class of railway
 *
 *
 * By lmlstarqaq http://snake1999.com/
 * Creation time: 2017/7/1 17:42.
 */
object Rail {
	fun isRailBlock(block: Block): Boolean {
		Objects.requireNonNull(block, "Rail block predicate can not accept null block")
		return isRailBlock(block.id)
	}

	fun isRailBlock(blockId: Int): Boolean {
		return when (blockId) {
			Block.RAIL, Block.POWERED_RAIL, Block.ACTIVATOR_RAIL, Block.DETECTOR_RAIL -> true
			else -> false
		}
	}

	enum class Orientation(private val meta: Int, private val state: State, from: BlockFace, to: BlockFace, ascendingDirection: BlockFace?) {
		STRAIGHT_NORTH_SOUTH(0, State.STRAIGHT, BlockFace.NORTH, BlockFace.SOUTH, null), STRAIGHT_EAST_WEST(1, State.STRAIGHT, BlockFace.EAST, BlockFace.WEST, null), ASCENDING_EAST(2, State.ASCENDING, BlockFace.EAST, BlockFace.WEST, BlockFace.EAST), ASCENDING_WEST(3, State.ASCENDING, BlockFace.EAST, BlockFace.WEST, BlockFace.WEST), ASCENDING_NORTH(4, State.ASCENDING, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.NORTH), ASCENDING_SOUTH(5, State.ASCENDING, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.SOUTH), CURVED_SOUTH_EAST(6, State.CURVED, BlockFace.SOUTH, BlockFace.EAST, null), CURVED_SOUTH_WEST(7, State.CURVED, BlockFace.SOUTH, BlockFace.WEST, null), CURVED_NORTH_WEST(8, State.CURVED, BlockFace.NORTH, BlockFace.WEST, null), CURVED_NORTH_EAST(9, State.CURVED, BlockFace.NORTH, BlockFace.EAST, null);

		private val connectingDirections: List<BlockFace>
		private val ascendingDirection: BlockFace?
		fun metadata(): Int {
			return meta
		}

		fun hasConnectingDirections(vararg faces: BlockFace?): Boolean {
			return Stream.of(*faces).allMatch(Predicate { o: BlockFace -> connectingDirections.contains(o) })
		}

		fun connectingDirections(): List<BlockFace> {
			return connectingDirections
		}

		fun ascendingDirection(): Optional<BlockFace> {
			return Optional.ofNullable(ascendingDirection)
		}

		enum class State {
			STRAIGHT, ASCENDING, CURVED
		}

		val isStraight: Boolean
			get() = state == State.STRAIGHT

		val isAscending: Boolean
			get() = state == State.ASCENDING

		val isCurved: Boolean
			get() = state == State.CURVED

		companion object {
			private val META_LOOKUP = arrayOfNulls<Orientation>(values().size)
			fun byMetadata(meta: Int): Orientation? {
				var meta = meta
				if (meta < 0 || meta >= META_LOOKUP.size) {
					meta = 0
				}
				return META_LOOKUP[meta]
			}

			fun straight(face: BlockFace?): Orientation {
				when (face) {
					BlockFace.NORTH, BlockFace.SOUTH -> return STRAIGHT_NORTH_SOUTH
					BlockFace.EAST, BlockFace.WEST -> return STRAIGHT_EAST_WEST
				}
				return STRAIGHT_NORTH_SOUTH
			}

			fun ascending(face: BlockFace?): Orientation {
				when (face) {
					BlockFace.NORTH -> return ASCENDING_NORTH
					BlockFace.SOUTH -> return ASCENDING_SOUTH
					BlockFace.EAST -> return ASCENDING_EAST
					BlockFace.WEST -> return ASCENDING_WEST
				}
				return ASCENDING_EAST
			}

			fun curved(f1: BlockFace, f2: BlockFace): Orientation {
				for (o in arrayOf(CURVED_SOUTH_EAST, CURVED_SOUTH_WEST, CURVED_NORTH_WEST, CURVED_NORTH_EAST)) {
					if (o.connectingDirections.contains(f1) && o.connectingDirections.contains(f2)) {
						return o
					}
				}
				return CURVED_SOUTH_EAST
			}

			fun straightOrCurved(f1: BlockFace, f2: BlockFace): Orientation {
				for (o in arrayOf(STRAIGHT_NORTH_SOUTH, STRAIGHT_EAST_WEST, CURVED_SOUTH_EAST, CURVED_SOUTH_WEST, CURVED_NORTH_WEST, CURVED_NORTH_EAST)) {
					if (o.connectingDirections.contains(f1) && o.connectingDirections.contains(f2)) {
						return o
					}
				}
				return STRAIGHT_NORTH_SOUTH
			}

			init {
				for (o in values()) {
					META_LOOKUP[cn.nukkit.utils.o.meta] = cn.nukkit.utils.o
				}
			}
		}

		init {
			connectingDirections = Arrays.asList(from, to)
			this.ascendingDirection = ascendingDirection
		}
	}
}