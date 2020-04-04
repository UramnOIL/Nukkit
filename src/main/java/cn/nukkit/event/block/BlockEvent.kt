package cn.nukkit.event.block

import cn.nukkit.block.Block
import cn.nukkit.event.Event

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class BlockEvent(val block: Block?) : Event()