package cn.nukkit.event.entity

import cn.nukkit.entity.Entity

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EntityDamageByChildEntityEvent(damager: Entity, val child: Entity, entity: Entity, cause: DamageCause?, damage: Float) : EntityDamageByEntityEvent(damager, entity, cause, damage)