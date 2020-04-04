package cn.nukkit.event.entity

import cn.nukkit.entity.Entity

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EntityCombustByEntityEvent(val combuster: Entity, combustee: Entity?, duration: Int) : EntityCombustEvent(combustee, duration)