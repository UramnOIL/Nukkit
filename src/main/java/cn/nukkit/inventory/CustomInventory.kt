package cn.nukkit.inventory

import cn.nukkit.item.Item

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class CustomInventory : ContainerInventory {
	constructor(holder: InventoryHolder?, type: InventoryType) : super(holder, type) {}
	constructor(holder: InventoryHolder?, type: InventoryType, items: Map<Int?, Item?>) : super(holder, type, items) {}
	constructor(holder: InventoryHolder?, type: InventoryType, items: Map<Int?, Item?>, overrideSize: Int?) : super(holder, type, items, overrideSize) {}
	constructor(holder: InventoryHolder?, type: InventoryType, items: Map<Int?, Item?>, overrideSize: Int?, overrideTitle: String?) : super(holder, type, items, overrideSize, overrideTitle) {}
}