package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.level.GlobalBlockPalette
import cn.nukkit.level.Level
import cn.nukkit.level.Sound
import cn.nukkit.level.particle.SmokeParticle
import cn.nukkit.math.BlockFace
import cn.nukkit.network.protocol.LevelEventPacket
import cn.nukkit.utils.BlockColor
import java.util.*

/**
 * author: Angelic47
 * Nukkit Project
 */
class BlockSponge @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta) {
	override val id: Int
		get() = BlockID.Companion.SPONGE

	override val hardness: Double
		get() = 0.6

	override val resistance: Double
		get() = 3

	override val name: String
		get() = NAMES[this.damage and 1]

	override val color: BlockColor
		get() = BlockColor.YELLOW_BLOCK_COLOR

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val level = block.getLevel()
		val blockSet = level.setBlock(block, this)
		if (blockSet) {
			if (this.damage == WET && level.dimension == Level.DIMENSION_NETHER) {
				level.setBlock(block, Block.Companion.get(BlockID.Companion.SPONGE, DRY))
				getLevel().addSound(block.location, Sound.RANDOM_FIZZ)
				for (i in 0..7) {
					getLevel().addParticle( //TODO: Use correct smoke particle
							SmokeParticle(block.location.add(Math.random(), 1.0, Math.random())))
				}
			} else if (this.damage == DRY && performWaterAbsorb(block)) {
				level.setBlock(block, Block.Companion.get(BlockID.Companion.SPONGE, WET))
				for (i in 0..3) {
					val packet = LevelEventPacket()
					packet.evid = 2001
					packet.x = block.getX().toFloat()
					packet.y = block.getY().toFloat()
					packet.z = block.getZ().toFloat()
					packet.data = GlobalBlockPalette.getOrCreateRuntimeId(BlockID.Companion.WATER, 0)
					level.addChunkPacket(chunkX, chunkZ, packet)
				}
			}
		}
		return blockSet
	}

	private fun performWaterAbsorb(block: Block): Boolean {
		val entries: Queue<Entry> = ArrayDeque()
		entries.add(Entry(block, 0))
		var entry: Entry
		var waterRemoved = 0
		while (waterRemoved < 64 && entries.poll().also { entry = it } != null) {
			for (face in BlockFace.values()) {
				val faceBlock = entry.block!!.getSide(face)
				if (faceBlock.id == BlockID.Companion.WATER || faceBlock.id == BlockID.Companion.STILL_WATER) {
					level.setBlock(faceBlock, Block.Companion.get(BlockID.Companion.AIR))
					++waterRemoved
					if (entry.distance < 6) {
						entries.add(Entry(faceBlock, entry.distance + 1))
					}
				} else if (faceBlock.id == BlockID.Companion.AIR) {
					if (entry.distance < 6) {
						entries.add(Entry(faceBlock, entry.distance + 1))
					}
				}
			}
		}
		return waterRemoved > 0
	}

	private class Entry(val block: Block?, val distance: Int)

	companion object {
		const val DRY = 0
		const val WET = 1
		private val NAMES = arrayOf(
				"Sponge",
				"Wet sponge"
		)
	}
}