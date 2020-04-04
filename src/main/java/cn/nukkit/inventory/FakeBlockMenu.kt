package cn.nukkit.inventory

import cn.nukkit.level.Position

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class FakeBlockMenu(override val inventory: Inventory, pos: Position) : Position(pos.x, pos.y, pos.z, pos.level), InventoryHolder