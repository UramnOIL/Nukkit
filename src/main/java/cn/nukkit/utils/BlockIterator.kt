package cn.nukkit.utils

import cn.nukkit.block.Block
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.math.Vector3

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockIterator @JvmOverloads constructor(private val level: Level, start: Vector3, direction: Vector3, yOffset: Double = 0.0, private val maxDistance: Int = 0) : MutableIterator<Block?> {
	private var end = false
	private val blockQueue: Array<Block?>
	private var currentBlock = 0
	private var currentBlockObject: Block? = null
	private var currentDistance: Int
	private var maxDistanceInt = 0
	private var secondError: Int
	private var thirdError: Int
	private val secondStep: Int
	private val thirdStep: Int
	private var mainFace: BlockFace? = null
	private var secondFace: BlockFace? = null
	private var thirdFace: BlockFace? = null
	private fun blockEquals(a: Block?, b: Block): Boolean {
		return a.x === b.x && a.y === b.y && a.z === b.z
	}

	private fun getXFace(direction: Vector3): BlockFace {
		return if (direction.x > 0) BlockFace.EAST else BlockFace.WEST
	}

	private fun getYFace(direction: Vector3): BlockFace {
		return if (direction.y > 0) BlockFace.UP else BlockFace.DOWN
	}

	private fun getZFace(direction: Vector3): BlockFace {
		return if (direction.z > 0) BlockFace.SOUTH else BlockFace.NORTH
	}

	private fun getXLength(direction: Vector3): Double {
		return Math.abs(direction.x)
	}

	private fun getYLength(direction: Vector3): Double {
		return Math.abs(direction.y)
	}

	private fun getZLength(direction: Vector3): Double {
		return Math.abs(direction.z)
	}

	private fun getPosition(direction: Double, position: Double, blockPosition: Double): Double {
		return if (direction > 0) position - blockPosition else blockPosition + 1 - position
	}

	private fun getXPosition(direction: Vector3, position: Vector3, block: Block): Double {
		return getPosition(direction.x, position.x, block.x)
	}

	private fun getYPosition(direction: Vector3, position: Vector3, block: Block): Double {
		return getPosition(direction.y, position.y, block.y)
	}

	private fun getZPosition(direction: Vector3, position: Vector3, block: Block): Double {
		return getPosition(direction.z, position.z, block.z)
	}

	override fun next(): Block? {
		this.scan()
		if (currentBlock <= -1) {
			throw IndexOutOfBoundsException()
		} else {
			currentBlockObject = blockQueue[currentBlock--]
		}
		return currentBlockObject
	}

	override fun hasNext(): Boolean {
		this.scan()
		return currentBlock != -1
	}

	private fun scan() {
		if (currentBlock >= 0) {
			return
		}
		if (maxDistance != 0 && currentDistance > maxDistanceInt) {
			end = true
			return
		}
		if (end) {
			return
		}
		++currentDistance
		secondError += secondStep
		thirdError += thirdStep
		if (secondError > 0 && thirdError > 0) {
			blockQueue[2] = blockQueue[0]!!.getSide(mainFace!!)
			if (secondStep * thirdError < thirdStep * secondError) {
				blockQueue[1] = blockQueue[2]!!.getSide(secondFace!!)
				blockQueue[0] = blockQueue[1]!!.getSide(thirdFace!!)
			} else {
				blockQueue[1] = blockQueue[2]!!.getSide(thirdFace!!)
				blockQueue[0] = blockQueue[1]!!.getSide(secondFace!!)
			}
			thirdError -= gridSize
			secondError -= gridSize
			currentBlock = 2
		} else if (secondError > 0) {
			blockQueue[1] = blockQueue[0]!!.getSide(mainFace!!)
			blockQueue[0] = blockQueue[1]!!.getSide(secondFace!!)
			secondError -= gridSize
			currentBlock = 1
		} else if (thirdError > 0) {
			blockQueue[1] = blockQueue[0]!!.getSide(mainFace!!)
			blockQueue[0] = blockQueue[1]!!.getSide(thirdFace!!)
			thirdError -= gridSize
			currentBlock = 1
		} else {
			blockQueue[0] = blockQueue[0]!!.getSide(mainFace!!)
			currentBlock = 0
		}
	}

	companion object {
		private const val gridSize = 1 shl 24
	}

	init {
		blockQueue = arrayOfNulls(3)
		val startClone = Vector3(start.x, start.y, start.z)
		startClone.y += yOffset
		currentDistance = 0
		var mainDirection = 0.0
		var secondDirection = 0.0
		var thirdDirection = 0.0
		var mainPosition = 0.0
		var secondPosition = 0.0
		var thirdPosition = 0.0
		val pos = Vector3(startClone.x, startClone.y, startClone.z)
		val startBlock = level.getBlock(Vector3(Math.floor(pos.x), Math.floor(pos.y), Math.floor(pos.z)))
		if (getXLength(direction) > mainDirection) {
			mainFace = getXFace(direction)
			mainDirection = getXLength(direction)
			mainPosition = getXPosition(direction, startClone, startBlock)
			secondFace = getYFace(direction)
			secondDirection = getYLength(direction)
			secondPosition = getYPosition(direction, startClone, startBlock)
			thirdFace = getZFace(direction)
			thirdDirection = getZLength(direction)
			thirdPosition = getZPosition(direction, startClone, startBlock)
		}
		if (getYLength(direction) > mainDirection) {
			mainFace = getYFace(direction)
			mainDirection = getYLength(direction)
			mainPosition = getYPosition(direction, startClone, startBlock)
			secondFace = getZFace(direction)
			secondDirection = getZLength(direction)
			secondPosition = getZPosition(direction, startClone, startBlock)
			thirdFace = getXFace(direction)
			thirdDirection = getXLength(direction)
			thirdPosition = getXPosition(direction, startClone, startBlock)
		}
		if (getZLength(direction) > mainDirection) {
			mainFace = getZFace(direction)
			mainDirection = getZLength(direction)
			mainPosition = getZPosition(direction, startClone, startBlock)
			secondFace = getXFace(direction)
			secondDirection = getXLength(direction)
			secondPosition = getXPosition(direction, startClone, startBlock)
			thirdFace = getYFace(direction)
			thirdDirection = getYLength(direction)
			thirdPosition = getYPosition(direction, startClone, startBlock)
		}
		val d = mainPosition / mainDirection
		val secondd = secondPosition - secondDirection * d
		val thirdd = thirdPosition - thirdDirection * d
		secondError = Math.floor(secondd * gridSize).toInt()
		secondStep = Math.round(secondDirection / mainDirection * gridSize).toInt()
		thirdError = Math.floor(thirdd * gridSize).toInt()
		thirdStep = Math.round(thirdDirection / mainDirection * gridSize).toInt()
		if (secondError + secondStep <= 0) {
			secondError = -secondStep + 1
		}
		if (thirdError + thirdStep <= 0) {
			thirdError = -thirdStep + 1
		}
		var lastBlock = startBlock.getSide(mainFace!!.getOpposite()!!)
		if (secondError < 0) {
			secondError += gridSize
			lastBlock = lastBlock.getSide(secondFace!!.getOpposite()!!)
		}
		if (thirdError < 0) {
			thirdError += gridSize
			lastBlock = lastBlock.getSide(thirdFace!!.getOpposite()!!)
		}
		secondError -= gridSize
		thirdError -= gridSize
		blockQueue[0] = lastBlock
		currentBlock = -1
		this.scan()
		var startBlockFound = false
		for (cnt in currentBlock downTo 0) {
			if (blockEquals(blockQueue[cnt], startBlock)) {
				currentBlock = cnt
				startBlockFound = true
				break
			}
		}
		check(startBlockFound) { "Start block missed in BlockIterator" }
		maxDistanceInt = Math.round(maxDistance / (Math.sqrt(mainDirection * mainDirection + secondDirection * secondDirection + thirdDirection * thirdDirection) / mainDirection)).toInt()
	}
}