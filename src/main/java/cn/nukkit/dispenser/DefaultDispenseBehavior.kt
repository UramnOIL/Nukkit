package cn.nukkit.dispenser

import cn.nukkit.block.BlockDispenser
import cn.nukkit.item.Item
import cn.nukkit.math.BlockFace

/**
 * @author CreeperFace
 */
class DefaultDispenseBehavior : DispenseBehavior {
	override fun dispense(block: BlockDispenser, stack: Item?) {}
	private fun getParticleMetadataForFace(face: BlockFace): Int {
		return face.xOffset + 1 + (face.zOffset + 1) * 3
	}
}