package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityBed
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBed
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.math.Vector3
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.DyeColor
import cn.nukkit.utils.Faceable

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockBed @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta), Faceable {
	override val id: Int
		get() = BlockID.Companion.BED_BLOCK

	override fun canBeActivated(): Boolean {
		return true
	}

	override val resistance: Double
		get() = 1

	override val hardness: Double
		get() = 0.2

	override val name: String
		get() = dyeColor.name + " Bed Block"

	override fun getMaxY(): Double {
		return y + 0.5625
	}

	override fun onActivate(item: Item): Boolean {
		return this.onActivate(item, null)
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		val time = getLevel().time % Level.TIME_FULL
		val isNight = time >= Level.TIME_NIGHT && time < Level.TIME_SUNRISE
		if (player != null && !isNight) {
			player.sendMessage(TranslationContainer("tile.bed.noSleep"))
			return true
		}
		val blockNorth = this.north()
		val blockSouth = this.south()
		val blockEast = this.east()
		val blockWest = this.west()
		val b: Block?
		b = if (this.damage and 0x08 == 0x08) {
			this
		} else {
			if (blockNorth.id == id && blockNorth.damage and 0x08 == 0x08) {
				blockNorth
			} else if (blockSouth.id == id && blockSouth.damage and 0x08 == 0x08) {
				blockSouth
			} else if (blockEast.id == id && blockEast.damage and 0x08 == 0x08) {
				blockEast
			} else if (blockWest.id == id && blockWest.damage and 0x08 == 0x08) {
				blockWest
			} else {
				player?.sendMessage(TranslationContainer("tile.bed.notValid"))
				return true
			}
		}
		if (player != null && !player.sleepOn(b!!)) {
			player.sendMessage(TranslationContainer("tile.bed.occupied"))
		}
		return true
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val down = this.down()
		if (!down!!.isTransparent) {
			val next = this.getSide(player!!.direction)
			val downNext = next!!.down()
			if (next.canBeReplaced() && !downNext!!.isTransparent) {
				val meta = player.direction.horizontalIndex
				getLevel().setBlock(block, Block.Companion.get(id, meta), true, true)
				getLevel().setBlock(next, Block.Companion.get(id, meta or 0x08), true, true)
				createBlockEntity(this, item.damage)
				createBlockEntity(next, item.damage)
				return true
			}
		}
		return false
	}

	override fun onBreak(item: Item): Boolean {
		val blockNorth = this.north() //Gets the blocks around them
		val blockSouth = this.south()
		val blockEast = this.east()
		val blockWest = this.west()
		if (this.damage and 0x08 == 0x08) { //This is the Top part of bed
			if (blockNorth.id == BlockID.Companion.BED_BLOCK && blockNorth.damage and 0x08 != 0x08) { //Checks if the block ID&&meta are right
				getLevel().setBlock(blockNorth, Block.Companion.get(BlockID.Companion.AIR), true, true)
			} else if (blockSouth.id == BlockID.Companion.BED_BLOCK && blockSouth.damage and 0x08 != 0x08) {
				getLevel().setBlock(blockSouth, Block.Companion.get(BlockID.Companion.AIR), true, true)
			} else if (blockEast.id == BlockID.Companion.BED_BLOCK && blockEast.damage and 0x08 != 0x08) {
				getLevel().setBlock(blockEast, Block.Companion.get(BlockID.Companion.AIR), true, true)
			} else if (blockWest.id == BlockID.Companion.BED_BLOCK && blockWest.damage and 0x08 != 0x08) {
				getLevel().setBlock(blockWest, Block.Companion.get(BlockID.Companion.AIR), true, true)
			}
		} else { //Bottom Part of Bed
			if (blockNorth.id == id && blockNorth.damage and 0x08 == 0x08) {
				getLevel().setBlock(blockNorth, Block.Companion.get(BlockID.Companion.AIR), true, true)
			} else if (blockSouth.id == id && blockSouth.damage and 0x08 == 0x08) {
				getLevel().setBlock(blockSouth, Block.Companion.get(BlockID.Companion.AIR), true, true)
			} else if (blockEast.id == id && blockEast.damage and 0x08 == 0x08) {
				getLevel().setBlock(blockEast, Block.Companion.get(BlockID.Companion.AIR), true, true)
			} else if (blockWest.id == id && blockWest.damage and 0x08 == 0x08) {
				getLevel().setBlock(blockWest, Block.Companion.get(BlockID.Companion.AIR), true, true)
			}
		}
		getLevel().setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true, false) // Do not update both parts to prevent duplication bug if there is two fallable blocks top of the bed
		return true
	}

	private fun createBlockEntity(pos: Vector3?, color: Int) {
		val nbt = BlockEntity.getDefaultCompound(pos, BlockEntity.BED)
		nbt.putByte("color", color)
		BlockEntity.createBlockEntity(BlockEntity.BED, level.getChunk(pos!!.floorX shr 4, pos.floorZ shr 4), nbt)
	}

	override fun toItem(): Item? {
		return ItemBed(dyeColor.woolData)
	}

	override val color: BlockColor
		get() = dyeColor.color

	val dyeColor: DyeColor
		get() {
			if (level != null) {
				val blockEntity = level.getBlockEntity(this)
				if (blockEntity is BlockEntityBed) {
					return blockEntity.dyeColor
				}
			}
			return DyeColor.WHITE
		}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x7)
	}
}