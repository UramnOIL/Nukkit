package cn.nukkit.dispenser

import cn.nukkit.block.BlockDispenser
import cn.nukkit.entity.Entity
import cn.nukkit.item.Item
import cn.nukkit.level.Position
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author CreeperFace
 */
class ProjectileDispenseBehavior : DispenseBehavior {
	protected var entityType: String? = null
		private set

	constructor() {}
	constructor(entity: String?) {
		entityType = entity
	}

	override fun dispense(source: BlockDispenser, item: Item?) {
		val dispensePos = Position.fromObject(source.dispensePosition, source.getLevel())
		val nbt = Entity.getDefaultNBT(dispensePos)
		correctNBT(nbt)
		val face = source.facing
		val projectile = Entity.createEntity(entityType, dispensePos.getLevel().getChunk(dispensePos.floorX, dispensePos.floorZ), nbt)
				?: return
		projectile.motion = Vector3(face.xOffset.toDouble(), (face.yOffset + 0.1f).toDouble(), face.zOffset.toDouble()).multiply(6.0)
		projectile.spawnToAll()
	}

	/**
	 * you can add extra data of projectile here
	 *
	 * @param nbt tag
	 */
	protected fun correctNBT(nbt: CompoundTag?) {}
}