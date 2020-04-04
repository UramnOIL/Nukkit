package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityMusic
import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.Level
import cn.nukkit.level.Sound
import cn.nukkit.math.BlockFace
import cn.nukkit.network.protocol.BlockEventPacket
import cn.nukkit.network.protocol.LevelSoundEventPacket
import cn.nukkit.utils.BlockColor

/**
 * Created by Snake1999 on 2016/1/17.
 * Package cn.nukkit.block in project nukkit.
 */
class BlockNoteblock : BlockSolid() {
	override val name: String
		get() = "Note Block"

	override val id: Int
		get() = BlockID.Companion.NOTEBLOCK

	override val toolType: Int
		get() = ItemTool.TYPE_AXE

	override val hardness: Double
		get() = 0.8

	override val resistance: Double
		get() = 4.0

	override fun canBeActivated(): Boolean {
		return true
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		getLevel().setBlock(block, this, true)
		return createBlockEntity() != null
	}

	val strength: Int
		get() {
			val blockEntity = blockEntity
			return blockEntity?.pitch ?: 0
		}

	fun increaseStrength() {
		val blockEntity = blockEntity
		blockEntity?.changePitch()
	}

	val instrument: Instrument
		get() = when (this.down().id) {
			BlockID.Companion.GOLD_BLOCK -> Instrument.GLOCKENSPIEL
			BlockID.Companion.CLAY_BLOCK -> Instrument.FLUTE
			BlockID.Companion.PACKED_ICE -> Instrument.CHIME
			BlockID.Companion.WOOL -> Instrument.GUITAR
			BlockID.Companion.BONE_BLOCK -> Instrument.XYLOPHONE
			BlockID.Companion.IRON_BLOCK -> Instrument.VIBRAPHONE
			BlockID.Companion.SOUL_SAND -> Instrument.COW_BELL
			BlockID.Companion.PUMPKIN -> Instrument.DIDGERIDOO
			BlockID.Companion.EMERALD_BLOCK -> Instrument.SQUARE_WAVE
			BlockID.Companion.HAY_BALE -> Instrument.BANJO
			BlockID.Companion.GLOWSTONE_BLOCK -> Instrument.ELECTRIC_PIANO
			BlockID.Companion.LOG, BlockID.Companion.LOG2, BlockID.Companion.PLANKS, BlockID.Companion.DOUBLE_WOODEN_SLAB, BlockID.Companion.WOODEN_SLAB, BlockID.Companion.WOOD_STAIRS, BlockID.Companion.SPRUCE_WOOD_STAIRS, BlockID.Companion.BIRCH_WOOD_STAIRS, BlockID.Companion.JUNGLE_WOOD_STAIRS, BlockID.Companion.ACACIA_WOOD_STAIRS, BlockID.Companion.DARK_OAK_WOOD_STAIRS, BlockID.Companion.FENCE, BlockID.Companion.FENCE_GATE, BlockID.Companion.FENCE_GATE_SPRUCE, BlockID.Companion.FENCE_GATE_BIRCH, BlockID.Companion.FENCE_GATE_JUNGLE, BlockID.Companion.FENCE_GATE_DARK_OAK, BlockID.Companion.FENCE_GATE_ACACIA, BlockID.Companion.DOOR_BLOCK, BlockID.Companion.SPRUCE_DOOR_BLOCK, BlockID.Companion.BIRCH_DOOR_BLOCK, BlockID.Companion.JUNGLE_DOOR_BLOCK, BlockID.Companion.ACACIA_DOOR_BLOCK, BlockID.Companion.DARK_OAK_DOOR_BLOCK, BlockID.Companion.WOODEN_PRESSURE_PLATE, BlockID.Companion.TRAPDOOR, BlockID.Companion.SIGN_POST, BlockID.Companion.WALL_SIGN, BlockID.Companion.NOTEBLOCK, BlockID.Companion.BOOKSHELF, BlockID.Companion.CHEST, BlockID.Companion.TRAPPED_CHEST, BlockID.Companion.CRAFTING_TABLE, BlockID.Companion.JUKEBOX, BlockID.Companion.BROWN_MUSHROOM_BLOCK, BlockID.Companion.RED_MUSHROOM_BLOCK, BlockID.Companion.DAYLIGHT_DETECTOR, BlockID.Companion.DAYLIGHT_DETECTOR_INVERTED, BlockID.Companion.STANDING_BANNER, BlockID.Companion.WALL_BANNER -> Instrument.BASS
			BlockID.Companion.SAND, BlockID.Companion.GRAVEL, BlockID.Companion.CONCRETE_POWDER -> Instrument.DRUM
			BlockID.Companion.GLASS, BlockID.Companion.GLASS_PANEL, BlockID.Companion.STAINED_GLASS_PANE, BlockID.Companion.STAINED_GLASS, BlockID.Companion.BEACON, BlockID.Companion.SEA_LANTERN -> Instrument.STICKS
			BlockID.Companion.STONE, BlockID.Companion.SANDSTONE, BlockID.Companion.RED_SANDSTONE, BlockID.Companion.COBBLESTONE, BlockID.Companion.MOSSY_STONE, BlockID.Companion.BRICKS, BlockID.Companion.STONE_BRICKS, BlockID.Companion.NETHER_BRICK_BLOCK, BlockID.Companion.RED_NETHER_BRICK, BlockID.Companion.QUARTZ_BLOCK, BlockID.Companion.DOUBLE_SLAB, BlockID.Companion.SLAB, BlockID.Companion.DOUBLE_RED_SANDSTONE_SLAB, BlockID.Companion.RED_SANDSTONE_SLAB, BlockID.Companion.COBBLE_STAIRS, BlockID.Companion.BRICK_STAIRS, BlockID.Companion.STONE_BRICK_STAIRS, BlockID.Companion.NETHER_BRICKS_STAIRS, BlockID.Companion.SANDSTONE_STAIRS, BlockID.Companion.QUARTZ_STAIRS, BlockID.Companion.RED_SANDSTONE_STAIRS, BlockID.Companion.PURPUR_STAIRS, BlockID.Companion.COBBLE_WALL, BlockID.Companion.NETHER_BRICK_FENCE, BlockID.Companion.BEDROCK, BlockID.Companion.GOLD_ORE, BlockID.Companion.IRON_ORE, BlockID.Companion.COAL_ORE, BlockID.Companion.LAPIS_ORE, BlockID.Companion.DIAMOND_ORE, BlockID.Companion.REDSTONE_ORE, BlockID.Companion.GLOWING_REDSTONE_ORE, BlockID.Companion.EMERALD_ORE, BlockID.Companion.DROPPER, BlockID.Companion.DISPENSER, BlockID.Companion.FURNACE, BlockID.Companion.BURNING_FURNACE, BlockID.Companion.OBSIDIAN, BlockID.Companion.GLOWING_OBSIDIAN, BlockID.Companion.MONSTER_SPAWNER, BlockID.Companion.STONE_PRESSURE_PLATE, BlockID.Companion.NETHERRACK, BlockID.Companion.QUARTZ_ORE, BlockID.Companion.ENCHANTING_TABLE, BlockID.Companion.END_PORTAL_FRAME, BlockID.Companion.END_STONE, BlockID.Companion.END_BRICKS, BlockID.Companion.ENDER_CHEST, BlockID.Companion.STAINED_TERRACOTTA, BlockID.Companion.TERRACOTTA, BlockID.Companion.PRISMARINE, BlockID.Companion.COAL_BLOCK, BlockID.Companion.PURPUR_BLOCK, BlockID.Companion.MAGMA, BlockID.Companion.CONCRETE, BlockID.Companion.STONECUTTER, BlockID.Companion.OBSERVER -> Instrument.BASS_DRUM
			else -> Instrument.PIANO
		}

	fun emitSound() {
		if (this.up().id != BlockID.Companion.AIR) return
		val instrument = instrument
		level.addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_NOTE, instrument.ordinal shl 8 or strength)
		val pk = BlockEventPacket()
		pk.x = this.floorX
		pk.y = this.floorY
		pk.z = this.floorZ
		pk.case1 = instrument.ordinal
		pk.case2 = strength
		getLevel().addChunkPacket(this.floorX shr 4, this.floorZ shr 4, pk)
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		increaseStrength()
		emitSound()
		return true
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_REDSTONE) {
			val blockEntity = blockEntity
			if (blockEntity != null) {
				if (getLevel().isBlockPowered(this)) {
					if (!blockEntity.isPowered) {
						emitSound()
					}
					blockEntity.isPowered = true
				} else {
					blockEntity.isPowered = false
				}
			}
		}
		return super.onUpdate(type)
	}

	private val blockEntity: BlockEntityMusic?
		private get() {
			val blockEntity = getLevel().getBlockEntity(this)
			return if (blockEntity is BlockEntityMusic) {
				blockEntity
			} else null
		}

	private fun createBlockEntity(): BlockEntityMusic? {
		return BlockEntity.createBlockEntity(BlockEntity.MUSIC, getLevel().getChunk(this.floorX shr 4, this.floorZ shr 4),
				BlockEntity.getDefaultCompound(this, BlockEntity.MUSIC)) as BlockEntityMusic
	}

	enum class Instrument(val sound: Sound) {
		PIANO(Sound.NOTE_HARP), BASS_DRUM(Sound.NOTE_BD), DRUM(Sound.NOTE_SNARE), STICKS(Sound.NOTE_HAT), BASS(Sound.NOTE_BASS), GLOCKENSPIEL(Sound.NOTE_BELL), FLUTE(Sound.NOTE_FLUTE), CHIME(Sound.NOTE_CHIME), GUITAR(Sound.NOTE_GUITAR), XYLOPHONE(Sound.NOTE_XYLOPHONE), VIBRAPHONE(Sound.NOTE_IRON_XYLOPHONE), COW_BELL(Sound.NOTE_COW_BELL), DIDGERIDOO(Sound.NOTE_DIDGERIDOO), SQUARE_WAVE(Sound.NOTE_BIT), BANJO(Sound.NOTE_BANJO), ELECTRIC_PIANO(Sound.NOTE_PLING);

	}

	override val color: BlockColor
		get() = BlockColor.WOOD_BLOCK_COLOR
}