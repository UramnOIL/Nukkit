package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.item.Item
import cn.nukkit.level.Level
import cn.nukkit.level.Sound
import cn.nukkit.math.NukkitRandom
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.DoubleTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/12/8 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockTNT : BlockSolid() {
	override val name: String
		get() = "TNT"

	override val id: Int
		get() = BlockID.Companion.TNT

	override val hardness: Double
		get() = 0

	override val resistance: Double
		get() = 0

	override fun canBeActivated(): Boolean {
		return true
	}

	override val burnChance: Int
		get() = 15

	override val burnAbility: Int
		get() = 100

	@JvmOverloads
	fun prime(fuse: Int = 80, source: Entity? = null) {
		getLevel().setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true)
		val mot = NukkitRandom().nextSignedFloat() * Math.PI * 2
		val nbt = CompoundTag()
				.putList(ListTag<DoubleTag>("Pos")
						.add(DoubleTag("", x + 0.5))
						.add(DoubleTag("", y))
						.add(DoubleTag("", z + 0.5)))
				.putList(ListTag<DoubleTag>("Motion")
						.add(DoubleTag("", -Math.sin(mot) * 0.02))
						.add(DoubleTag("", 0.2))
						.add(DoubleTag("", -Math.cos(mot) * 0.02)))
				.putList(ListTag<FloatTag>("Rotation")
						.add(FloatTag("", 0))
						.add(FloatTag("", 0)))
				.putShort("Fuse", fuse)
		val tnt = Entity.createEntity("PrimedTnt",
				getLevel().getChunk(this.floorX shr 4, this.floorZ shr 4),
				nbt, source
		)
				?: return
		tnt.spawnToAll()
		level.addSound(this, Sound.RANDOM_FUSE)
	}

	override fun onUpdate(type: Int): Int {
		if ((type == Level.BLOCK_UPDATE_NORMAL || type == Level.BLOCK_UPDATE_REDSTONE) && level.isBlockPowered(this.location)) {
			prime()
		}
		return 0
	}

	override fun onActivate(item: Item, player: Player?): Boolean {
		if (item.id == Item.FLINT_STEEL) {
			item.useOn(this)
			prime(80, player)
			return true
		}
		if (item.id == Item.FIRE_CHARGE) {
			if (!player!!.isCreative) player.inventory.removeItem(Item.get(Item.FIRE_CHARGE, 0, 1))
			level.addSound(player, Sound.MOB_GHAST_FIREBALL)
			prime(80, player)
			return true
		}
		return false
	}

	override val color: BlockColor
		get() = BlockColor.TNT_BLOCK_COLOR
}