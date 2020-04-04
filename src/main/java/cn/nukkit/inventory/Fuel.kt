package cn.nukkit.inventory

import cn.nukkit.item.Item
import java.util.*
import kotlin.collections.MutableMap
import kotlin.collections.set

/**
 * author: MagicDroidX
 * Nukkit Project
 */
object Fuel {
	@kotlin.jvm.JvmField
	val duration: MutableMap<Int, Short> = TreeMap()

	init {
		duration[Item.COAL] = 1600.toShort()
		duration[Item.COAL_BLOCK] = 16000.toShort()
		duration[Item.TRUNK] = 300.toShort()
		duration[Item.WOODEN_PLANKS] = 300.toShort()
		duration[Item.SAPLING] = 100.toShort()
		duration[Item.WOODEN_AXE] = 200.toShort()
		duration[Item.WOODEN_PICKAXE] = 200.toShort()
		duration[Item.WOODEN_SWORD] = 200.toShort()
		duration[Item.WOODEN_SHOVEL] = 200.toShort()
		duration[Item.WOODEN_HOE] = 200.toShort()
		duration[Item.STICK] = 100.toShort()
		duration[Item.FENCE] = 300.toShort()
		duration[Item.FENCE_GATE] = 300.toShort()
		duration[Item.FENCE_GATE_SPRUCE] = 300.toShort()
		duration[Item.FENCE_GATE_BIRCH] = 300.toShort()
		duration[Item.FENCE_GATE_JUNGLE] = 300.toShort()
		duration[Item.FENCE_GATE_ACACIA] = 300.toShort()
		duration[Item.FENCE_GATE_DARK_OAK] = 300.toShort()
		duration[Item.WOODEN_STAIRS] = 300.toShort()
		duration[Item.SPRUCE_WOOD_STAIRS] = 300.toShort()
		duration[Item.BIRCH_WOOD_STAIRS] = 300.toShort()
		duration[Item.JUNGLE_WOOD_STAIRS] = 300.toShort()
		duration[Item.TRAPDOOR] = 300.toShort()
		duration[Item.WORKBENCH] = 300.toShort()
		duration[Item.BOOKSHELF] = 300.toShort()
		duration[Item.CHEST] = 300.toShort()
		duration[Item.BUCKET] = 20000.toShort()
		duration[Item.LADDER] = 300.toShort()
		duration[Item.BOW] = 200.toShort()
		duration[Item.BOWL] = 200.toShort()
		duration[Item.WOOD2] = 300.toShort()
		duration[Item.WOODEN_PRESSURE_PLATE] = 300.toShort()
		duration[Item.ACACIA_WOOD_STAIRS] = 300.toShort()
		duration[Item.DARK_OAK_WOOD_STAIRS] = 300.toShort()
		duration[Item.TRAPPED_CHEST] = 300.toShort()
		duration[Item.DAYLIGHT_DETECTOR] = 300.toShort()
		duration[Item.DAYLIGHT_DETECTOR_INVERTED] = 300.toShort()
		duration[Item.JUKEBOX] = 300.toShort()
		duration[Item.NOTEBLOCK] = 300.toShort()
		duration[Item.WOOD_SLAB] = 300.toShort()
		duration[Item.DOUBLE_WOOD_SLAB] = 300.toShort()
		duration[Item.BOAT] = 1200.toShort()
		duration[Item.BLAZE_ROD] = 2400.toShort()
		duration[Item.BROWN_MUSHROOM_BLOCK] = 300.toShort()
		duration[Item.RED_MUSHROOM_BLOCK] = 300.toShort()
		duration[Item.FISHING_ROD] = 300.toShort()
		duration[Item.WOODEN_BUTTON] = 100.toShort()
		duration[Item.WOODEN_DOOR] = 200.toShort()
		duration[Item.SPRUCE_DOOR] = 200.toShort()
		duration[Item.BIRCH_DOOR] = 200.toShort()
		duration[Item.JUNGLE_DOOR] = 200.toShort()
		duration[Item.ACACIA_DOOR] = 200.toShort()
		duration[Item.DARK_OAK_DOOR] = 200.toShort()
		duration[Item.BANNER] = 300.toShort()
	}
}