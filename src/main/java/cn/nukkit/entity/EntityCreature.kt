package cn.nukkit.entity

import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class EntityCreature(chunk: FullChunk?, nbt: CompoundTag?) : EntityLiving(chunk, nbt)