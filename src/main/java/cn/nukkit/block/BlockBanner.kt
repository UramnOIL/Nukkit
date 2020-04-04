package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityBanner
import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.Level
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.BlockFace
import cn.nukkit.math.NukkitMath
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.IntTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.DyeColor
import cn.nukkit.utils.Faceable

/**
 * Created by PetteriM1
 */
open class BlockBanner @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta), Faceable {
	override val id: Int
		get() = BlockID.Companion.STANDING_BANNER

	override val hardness: Double
		get() = 1

	override val resistance: Double
		get() = 5

	override val toolType: Int
		get() = ItemTool.TYPE_AXE

	override val name: String
		get() = "Banner"

	override fun recalculateBoundingBox(): AxisAlignedBB? {
		return null
	}

	override fun canPassThrough(): Boolean {
		return true
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (face != BlockFace.DOWN) {
			if (face == BlockFace.UP) {
				this.setDamage(NukkitMath.floorDouble((player!!.yaw + 180) * 16 / 360 + 0.5) and 0x0f)
				getLevel().setBlock(block, this, true)
			} else {
				this.setDamage(face.index)
				getLevel().setBlock(block, Block.Companion.get(BlockID.Companion.WALL_BANNER, this.damage), true)
			}
			val nbt = BlockEntity.getDefaultCompound(this, BlockEntity.BANNER)
					.putInt("Base", item.damage and 0xf)
			val type = item.getNamedTagEntry("Type")
			if (type is IntTag) {
				nbt.put("Type", type)
			}
			val patterns = item.getNamedTagEntry("Patterns")
			if (patterns is ListTag<*>) {
				nbt.put("Patterns", patterns)
			}
			val banner = BlockEntity.createBlockEntity(BlockEntity.BANNER, this.chunk, nbt) as BlockEntityBanner
			return banner != null
		}
		return false
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL) {
			if (this.down().id == BlockID.Companion.AIR) {
				getLevel().useBreakOn(this)
				return Level.BLOCK_UPDATE_NORMAL
			}
		}
		return 0
	}

	override fun toItem(): Item? {
		val blockEntity = getLevel().getBlockEntity(this)
		val item = Item.get(Item.BANNER)
		if (blockEntity is BlockEntityBanner) {
			val banner = blockEntity
			item.damage = banner.baseColor and 0xf
			item.namedTag = (if (item.hasCompoundTag()) item.namedTag else CompoundTag())
					.putInt("Base", banner.baseColor and 0xf)
			val type = banner.namedTag.getInt("Type")
			if (type > 0) {
				item.namedTag = (if (item.hasCompoundTag()) item.namedTag else CompoundTag())
						.putInt("Type", type)
			}
			val patterns = banner.namedTag.getList("Patterns", CompoundTag::class.java)
			if (patterns.size() > 0) {
				item.namedTag = (if (item.hasCompoundTag()) item.namedTag else CompoundTag())
						.putList(patterns)
			}
		}
		return item
	}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x7)
	}

	override val color: BlockColor
		get() = dyeColor.color

	val dyeColor: DyeColor
		get() {
			if (level != null) {
				val blockEntity = level.getBlockEntity(this)
				if (blockEntity is BlockEntityBanner) {
					return blockEntity.dyeColor
				}
			}
			return DyeColor.WHITE
		}
}