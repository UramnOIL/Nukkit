package cn.nukkit.event.entity

import cn.nukkit.block.Block
import cn.nukkit.entity.Entity

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EntityDamageByBlockEvent(val damager: Block, entity: Entity, cause: DamageCause?, damage: Float) : EntityDamageEvent(entity, cause, damage)