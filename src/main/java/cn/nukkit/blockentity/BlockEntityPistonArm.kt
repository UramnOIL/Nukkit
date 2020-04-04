package cn.nukkit.blockentity

import cn.nukkit.level.format.FullChunk
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.BlockFace
import cn.nukkit.math.SimpleAxisAlignedBB
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.IntTag
import cn.nukkit.nbt.tag.ListTag

/**
 * @author CreeperFace
 */
class BlockEntityPistonArm(chunk: FullChunk?, nbt: CompoundTag) : BlockEntity(chunk, nbt) {
	var progress = 1.0f
	var lastProgress = 1.0f
	var facing: BlockFace? = null
	var extending = false
	var sticky = false
	var state: Byte = 1
	var newState: Byte = 1
	var attachedBlock: Vector3? = null
	override var isMovable = true
	var powered = false
	override fun initBlockEntity() {
		if (namedTag.contains("Progress")) {
			progress = namedTag.getFloat("Progress")
		}
		if (namedTag.contains("LastProgress")) {
			lastProgress = namedTag.getInt("LastProgress").toFloat()
		}
		if (namedTag.contains("Sticky")) {
			sticky = namedTag.getBoolean("Sticky")
		}
		if (namedTag.contains("Extending")) {
			extending = namedTag.getBoolean("Extending")
		}
		if (namedTag.contains("powered")) {
			powered = namedTag.getBoolean("powered")
		}
		if (namedTag.contains("AttachedBlocks")) {
			val blocks: ListTag<*>? = namedTag.getList("AttachedBlocks", IntTag::class.java)
			if (blocks != null && blocks.size() > 0) {
				attachedBlock = Vector3((blocks[0] as IntTag).getData().toDouble(), (blocks[1] as IntTag).getData().toDouble(), (blocks[2] as IntTag).getData().toDouble())
			}
		} else {
			namedTag.putList(ListTag<Any?>("AttachedBlocks"))
		}
		super.initBlockEntity()
	}

	private fun pushEntities() {
		val lastProgress = getExtendedProgress(lastProgress)
		val x = (lastProgress * facing!!.xOffset.toFloat()).toDouble()
		val y = (lastProgress * facing!!.yOffset.toFloat()).toDouble()
		val z = (lastProgress * facing!!.zOffset.toFloat()).toDouble()
		val bb: AxisAlignedBB = SimpleAxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0)
		val entities = level.getCollidingEntities(bb)
		if (entities.size != 0) {
		}
	}

	private fun getExtendedProgress(progress: Float): Float {
		return if (extending) progress - 1.0f else 1.0f - progress
	}

	override val isBlockEntityValid: Boolean
		get() = true

	override fun saveNBT() {
		super.saveNBT()
		namedTag.putBoolean("isMovable", isMovable)
		namedTag.putByte("State", state.toInt())
		namedTag.putByte("NewState", newState.toInt())
		namedTag.putFloat("Progress", progress)
		namedTag.putFloat("LastProgress", lastProgress)
		namedTag.putBoolean("powered", powered)
	}

	val spawnCompound: CompoundTag
		get() = CompoundTag().putString("id", "PistonArm").putInt("x", x.toInt()).putInt("y", y.toInt()).putInt("z", z.toInt())
}