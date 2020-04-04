package cn.nukkit.level.generator.`object`.tree

import cn.nukkit.block.Block
import cn.nukkit.block.BlockWood
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.generator
import cn.nukkit.math.NukkitRandom

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class ObjectBirchTree : ObjectTree() {
	override var treeHeight = 7
		protected set
	override val trunkBlock: Int
		get() = Block.LOG

	override val leafBlock: Int
		get() = Block.LEAVES

	override val type: Int
		get() = BlockWood.BIRCH

	override fun placeObject(level: ChunkManager?, x: Int, y: Int, z: Int, random: NukkitRandom) {
		treeHeight = random.nextBoundedInt(2) + 5
		super.placeObject(level, x, y, z, random)
	}
}