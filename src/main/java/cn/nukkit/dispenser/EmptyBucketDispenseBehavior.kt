package cn.nukkit.dispenser

import cn.nukkit.block.BlockDispenser
import cn.nukkit.item.Item

/**
 * @author CreeperFace
 */
class EmptyBucketDispenseBehavior : DispenseBehavior {
	override fun dispense(block: BlockDispenser, item: Item?) {}
}