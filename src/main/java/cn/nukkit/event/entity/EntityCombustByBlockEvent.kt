package cn.nukkit.event.entity

import cn.nukkit.block.Block
import cn.nukkit.entity.Entity

/**
 * author: Box
 * Nukkit Project
 */
class EntityCombustByBlockEvent(val combuster: Block, combustee: Entity?, duration: Int) : EntityCombustEvent(combustee, duration)