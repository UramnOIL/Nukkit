package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator

/**
 * Created by Pub4Game on 27.12.2015.
 */
class BlockMobSpawner : BlockSolid() {
	override val name: String
		get() = "Monster Spawner"

	override val id: Int
		get() = BlockID.Companion.MONSTER_SPAWNER

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val hardness: Double
		get() = 5

	override val resistance: Double
		get() = 25

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOfNulls(0)
	}

	override fun canBePushed(): Boolean {
		return false
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}