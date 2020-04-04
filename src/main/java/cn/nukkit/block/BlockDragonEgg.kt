package cn.nukkit.block

import cn.nukkit.level.Level
import cn.nukkit.network.protocol.LevelEventPacket
import cn.nukkit.utils.BlockColor
import java.util.concurrent.ThreadLocalRandom

class BlockDragonEgg : BlockFallable() {
	override val name: String
		get() = "Dragon Egg"

	override val id: Int
		get() = BlockID.Companion.DRAGON_EGG

	override val hardness: Double
		get() = 3

	override val resistance: Double
		get() = 45

	override val lightLevel: Int
		get() = 1

	override val color: BlockColor
		get() = BlockColor.OBSIDIAN_BLOCK_COLOR

	override val isTransparent: Boolean
		get() = true

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_TOUCH) {
			teleport()
		}
		return super.onUpdate(type)
	}

	fun teleport() {
		for (i in 0..999) {
			val t = getLevel().getBlock(this.add(ThreadLocalRandom.current().nextInt(-16, 16).toDouble(), ThreadLocalRandom.current().nextInt(-16, 16).toDouble(), ThreadLocalRandom.current().nextInt(-16, 16).toDouble()))
			if (t.id == BlockID.Companion.AIR) {
				val diffX = this.floorX - t.floorX
				val diffY = this.floorY - t.floorY
				val diffZ = this.floorZ - t.floorZ
				val pk = LevelEventPacket()
				pk.evid = LevelEventPacket.EVENT_PARTICLE_DRAGON_EGG_TELEPORT
				pk.data = Math.abs(diffX) shl 16 or (Math.abs(diffY) shl 8) or Math.abs(diffZ) or ((if (diffX < 0) 1 else 0) shl 24) or ((if (diffY < 0) 1 else 0) shl 25) or ((if (diffZ < 0) 1 else 0) shl 26)
				pk.x = this.floorX.toFloat()
				pk.y = this.floorY.toFloat()
				pk.z = this.floorZ.toFloat()
				getLevel().addChunkPacket(this.floorX shr 4, this.floorZ shr 4, pk)
				getLevel().setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true)
				getLevel().setBlock(t, this, true)
				return
			}
		}
	}
}