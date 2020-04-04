package cn.nukkit.event.inventory

import cn.nukkit.blockentity.BlockEntityFurnace
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.event.block.BlockEvent
import cn.nukkit.item.Item

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class FurnaceSmeltEvent(furnace: BlockEntityFurnace, source: Item, result: Item) : BlockEvent(furnace.block), Cancellable {
	val furnace: BlockEntityFurnace
	val source: Item
	var result: Item

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.source = source.clone()
		this.source.setCount(1)
		this.result = result
		this.furnace = furnace
	}
}