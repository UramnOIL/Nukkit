package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityComparator
import cn.nukkit.item.Item
import cn.nukkit.item.ItemRedstoneComparator
import cn.nukkit.level.Level
import cn.nukkit.level.Sound
import cn.nukkit.math.BlockFace
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.utils.BlockColor

/**
 * @author CreeperFace
 */
abstract class BlockRedstoneComparator @JvmOverloads constructor(meta: Int = 0) : BlockRedstoneDiode(meta) {
	protected override val delay: Int
		protected get() = 2

	override val facing: BlockFace
		get() = BlockFace.fromHorizontalIndex(this.damage)

	val mode: Mode
		get() = if (damage and 4 > 0) Mode.SUBTRACT else Mode.COMPARE

	protected override val unpowered: Block
		protected get() = Block.Companion.get(BlockID.Companion.UNPOWERED_COMPARATOR, this.damage) as BlockRedstoneComparator

	override fun getPowered(): BlockRedstoneComparator {
		return Block.Companion.get(BlockID.Companion.POWERED_COMPARATOR, this.damage) as BlockRedstoneComparator
	}

	protected override val redstoneSignal: Int
		protected get() {
			val blockEntity = level.getBlockEntity(this)
			return if (blockEntity is BlockEntityComparator) blockEntity.outputSignal else 0
		}

	override fun updateState() {
		if (!level.isBlockTickPending(this, this)) {
			val output = calculateOutput()
			val blockEntity = level.getBlockEntity(this)
			val power = if (blockEntity is BlockEntityComparator) blockEntity.outputSignal else 0
			if (output != power || isPowered() != shouldBePowered()) {
				/*if(isFacingTowardsRepeater()) {
                    this.level.scheduleUpdate(this, this, 2, -1);
                } else {
                    this.level.scheduleUpdate(this, this, 2, 0);
                }*/

				//System.out.println("schedule update 0");
				level.scheduleUpdate(this, this, 2)
			}
		}
	}

	override fun calculateInputStrength(): Int {
		var power = super.calculateInputStrength()
		val face = facing
		var block = this.getSide(face)
		if (block!!.hasComparatorInputOverride()) {
			power = block.comparatorInputOverride
		} else if (power < 15 && block.isNormalBlock) {
			block = block.getSide(face)
			if (block.hasComparatorInputOverride()) {
				power = block.comparatorInputOverride
			}
		}
		return power
	}

	override fun shouldBePowered(): Boolean {
		val input = calculateInputStrength()
		return if (input >= 15) {
			true
		} else if (input == 0) {
			false
		} else {
			val sidePower = this.powerOnSides
			sidePower == 0 || input >= sidePower
		}
	}

	private fun calculateOutput(): Int {
		return if (mode == Mode.SUBTRACT) Math.max(calculateInputStrength() - this.powerOnSides, 0) else calculateInputStrength()
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (mode == Mode.SUBTRACT) {
			this.setDamage(this.damage - 4)
		} else {
			this.setDamage(this.damage + 4)
		}
		level.addSound(this, Sound.RANDOM_CLICK, 1f, if (mode == Mode.SUBTRACT) 0.55f else 0.5f)
		level.setBlock(this, this, true, false)
		//bug?
		onChange()
		return true
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_SCHEDULED) {
			onChange()
			return type
		}
		return super.onUpdate(type)
	}

	private fun onChange() {
		val output = calculateOutput()
		val blockEntity = level.getBlockEntity(this)
		var currentOutput = 0
		if (blockEntity is BlockEntityComparator) {
			val blockEntityComparator = blockEntity
			currentOutput = blockEntityComparator.outputSignal
			blockEntityComparator.outputSignal = output
		}
		if (currentOutput != output || mode == Mode.COMPARE) {
			val shouldBePowered = shouldBePowered()
			val isPowered = isPowered()
			if (isPowered && !shouldBePowered) {
				level.setBlock(this, unpowered, true, false)
			} else if (!isPowered && shouldBePowered) {
				level.setBlock(this, powered, true, false)
			}
			level.updateAroundRedstone(this, null)
		}
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (super.place(item, block, target, face, fx, fy, fz, player)) {
			val nbt = CompoundTag()
					.putList(ListTag("Items"))
					.putString("id", BlockEntity.COMPARATOR)
					.putInt("x", x.toInt())
					.putInt("y", y.toInt())
					.putInt("z", z.toInt())
			val comparator = BlockEntity.createBlockEntity(BlockEntity.COMPARATOR, level.getChunk(x.toInt() shr 4, z.toInt() shr 4), nbt) as BlockEntityComparator
					?: return false
			onUpdate(Level.BLOCK_UPDATE_REDSTONE)
			return true
		}
		return false
	}

	override fun isPowered(): Boolean {
		return isPowered || this.damage and 8 > 0
	}

	override fun toItem(): Item? {
		return ItemRedstoneComparator()
	}

	enum class Mode {
		COMPARE, SUBTRACT
	}

	override val color: BlockColor
		get() = BlockColor.AIR_BLOCK_COLOR
}