package cn.nukkit.dispenser

import cn.nukkit.block.BlockDispenser
import cn.nukkit.item.Item

/**
 * @author CreeperFace
 */
interface DispenseBehavior {
	fun dispense(block: BlockDispenser, item: Item?)
}