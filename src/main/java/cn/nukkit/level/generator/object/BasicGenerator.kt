package cn.nukkit.level.generator.`object`

import cn.nukkit.block.Block
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.generator
import cn.nukkit.math.BlockVector3
import cn.nukkit.math.NukkitRandom
import cn.nukkit.math.Vector3

abstract class BasicGenerator {
	//also autism, see below
	abstract fun generate(level: ChunkManager, rand: NukkitRandom, position: Vector3?): Boolean
	fun setDecorationDefaults() {}
	protected fun setBlockAndNotifyAdequately(level: ChunkManager, pos: BlockVector3, state: Block) {
		setBlock(level, Vector3(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()), state)
	}

	protected fun setBlockAndNotifyAdequately(level: ChunkManager, pos: Vector3, state: Block) {
		setBlock(level, pos, state)
	}

	//what autism is this? why are we using floating-point vectors for setting block IDs?
	protected fun setBlock(level: ChunkManager, v: Vector3, b: Block) {
		level.setBlockAt(v.x as Int, v.y as Int, v.z as Int, b.id, b.damage)
	}
}