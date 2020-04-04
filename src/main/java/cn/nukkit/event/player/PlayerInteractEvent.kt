package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.item.Item
import cn.nukkit.level.Position
import cn.nukkit.math.BlockFace
import cn.nukkit.math.Vector3

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class PlayerInteractEvent @JvmOverloads constructor(player: Player, item: Item, block: Vector3?, face: BlockFace, action: Action = Action.RIGHT_CLICK_BLOCK) : PlayerEvent(), Cancellable {
	val block: Block? = null
	var touchVector: Vector3? = null
	val face: BlockFace
	val item: Item
	val action: Action

	enum class Action {
		LEFT_CLICK_BLOCK, RIGHT_CLICK_BLOCK, LEFT_CLICK_AIR, RIGHT_CLICK_AIR, PHYSICAL
	}

	companion object {
		val handlers = HandlerList()
	}

	init {
		if (block is Block) {
			this.block = block
			touchVector = Vector3(0, 0, 0)
		} else {
			touchVector = block
			this.block = get(Block.AIR, 0, Position(0, 0, 0, player.level))
		}
		this.player = player
		this.item = item
		this.face = face
		this.action = action
	}
}