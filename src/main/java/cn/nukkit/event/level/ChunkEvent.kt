package cn.nukkit.event.level

import cn.nukkit.level.format.FullChunk

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class ChunkEvent(val chunk: FullChunk) : LevelEvent(chunk.provider.level)