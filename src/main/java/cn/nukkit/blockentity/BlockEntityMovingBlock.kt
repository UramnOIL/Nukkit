package cn.nukkit.blockentity

import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.level.format.FullChunk
import cn.nukkit.math.BlockVector3
import cn.nukkit.nbt.tag.CompoundTag

/**
 * Created by CreeperFace on 11.4.2017.
 */
class BlockEntityMovingBlock(chunk: FullChunk?, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
	override var block: Block? = null
	var piston: BlockVector3? = null
	var progress = 0
	override fun initBlockEntity() {
		if (namedTag.contains("movingBlockData") && namedTag.contains("movingBlockId")) {
			block = get(namedTag.getInt("movingBlockId"), namedTag.getInt("movingBlockData"))
		} else {
			close()
		}
		if (namedTag.contains("pistonPosX") && namedTag.contains("pistonPosY") && namedTag.contains("pistonPosZ")) {
			piston = BlockVector3(namedTag.getInt("pistonPosX"), namedTag.getInt("pistonPosY"), namedTag.getInt("pistonPosZ"))
		} else {
			close()
		}
		super.initBlockEntity()
	}

	override val isBlockEntityValid: Boolean
		get() = true

	override val spawnCompound: CompoundTag?
		get() = BlockEntity.Companion.getDefaultCompound(this, BlockEntity.Companion.MOVING_BLOCK)
				.putFloat("movingBlockId", block!!.id.toFloat())
				.putFloat("movingBlockData", block!!.damage.toFloat())
				.putInt("pistonPosX", piston!!.x)
				.putInt("pistonPosY", piston!!.y)
				.putInt("pistonPosZ", piston!!.z)
}