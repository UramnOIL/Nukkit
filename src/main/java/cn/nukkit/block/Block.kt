package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.entity.Entity
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.level.Level
import cn.nukkit.level.MovingObjectPosition
import cn.nukkit.level.Position
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.BlockFace
import cn.nukkit.math.Vector3
import cn.nukkit.metadata.MetadataValue
import cn.nukkit.metadata.Metadatable
import cn.nukkit.plugin.Plugin
import cn.nukkit.potion.Effect
import cn.nukkit.utils.BlockColor
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class Block protected constructor() : Position(), Metadatable, Cloneable, AxisAlignedBB, BlockID {
	open fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		return getLevel().setBlock(this, this, true, true)
	}

	//http://minecraft.gamepedia.com/Breaking
	open fun canHarvestWithHand(): Boolean {  //used for calculating breaking time
		return true
	}

	open fun isBreakable(item: Item?): Boolean {
		return true
	}

	open fun tickRate(): Int {
		return 10
	}

	open fun onBreak(item: Item): Boolean {
		return getLevel().setBlock(this, get(BlockID.Companion.AIR), true, true)
	}

	open fun onUpdate(type: Int): Int {
		return 0
	}

	open fun onActivate(item: Item): Boolean {
		return this.onActivate(item, null)
	}

	open fun onActivate(item: Item, player: Player?): Boolean {
		return false
	}

	open val hardness: Double
		get() = 10

	open val resistance: Double
		get() = 1

	open val burnChance: Int
		get() = 0

	open val burnAbility: Int
		get() = 0

	open val toolType: Int
		get() = ItemTool.TYPE_NONE

	open val frictionFactor: Double
		get() = 0.6

	open val lightLevel: Int
		get() = 0

	open fun canBePlaced(): Boolean {
		return true
	}

	open fun canBeReplaced(): Boolean {
		return false
	}

	open val isTransparent: Boolean
		get() = false

	open val isSolid: Boolean
		get() = true

	open fun canBeFlowedInto(): Boolean {
		return false
	}

	open fun canBeActivated(): Boolean {
		return false
	}

	open fun hasEntityCollision(): Boolean {
		return false
	}

	open fun canPassThrough(): Boolean {
		return false
	}

	open fun canBePushed(): Boolean {
		return true
	}

	open fun hasComparatorInputOverride(): Boolean {
		return false
	}

	open val comparatorInputOverride: Int
		get() = 0

	open fun canBeClimbed(): Boolean {
		return false
	}

	open val color: BlockColor
		get() = BlockColor.VOID_BLOCK_COLOR

	abstract val name: String
	abstract val id: Int

	/**
	 * The full id is a combination of the id and data.
	 * @return full id
	 */
	open val fullId: Int
		get() = id shl 4

	open fun addVelocityToEntity(entity: Entity, vector: Vector3) {}

	// Do nothing
	open var damage: Int
		get() = 0
		set(meta) {
			// Do nothing
		}

	fun setDamage(meta: Int?) {
		damage = if (meta == null) 0 else meta and 0x0f
	}

	fun position(v: Position) {
		x = v.x as Int.toDouble()
		y = v.y as Int.toDouble()
		z = v.z as Int.toDouble()
		level = v.level
	}

	open fun getDrops(item: Item): Array<Item?> {
		return if (id < 0 || id > list!!.size) { //Unknown blocks
			arrayOfNulls(0)
		} else {
			arrayOf(
					toItem()
			)
		}
	}

	fun getBreakTime(item: Item, player: Player): Double {
		Objects.requireNonNull(item, "getBreakTime: Item can not be null")
		Objects.requireNonNull(player, "getBreakTime: Player can not be null")
		val blockHardness = hardness
		if (blockHardness == 0.0) {
			return 0
		}
		val correctTool = correctTool0(toolType, item)
		val canHarvestWithHand = canHarvestWithHand()
		val blockId = id
		val itemToolType = toolType0(item)
		val itemTier = item.tier
		val efficiencyLoreLevel = Optional.ofNullable(item.getEnchantment(Enchantment.ID_EFFICIENCY))
				.map { obj: Enchantment -> obj.level }.orElse(0)
		val hasteEffectLevel = Optional.ofNullable(player.getEffect(Effect.HASTE))
				.map { obj: Effect -> obj.amplifier }.orElse(0)
		val insideOfWaterWithoutAquaAffinity = player.isInsideOfWater &&
				Optional.ofNullable(player.inventory.helmet.getEnchantment(Enchantment.ID_WATER_WORKER))
						.map { obj: Enchantment -> obj.level }.map { l: Int -> l >= 1 }.orElse(false)
		val outOfWaterButNotOnGround = !player.isInsideOfWater && !player.isOnGround
		return breakTime0(blockHardness, correctTool, canHarvestWithHand, blockId, itemToolType, itemTier,
				efficiencyLoreLevel, hasteEffectLevel, insideOfWaterWithoutAquaAffinity, outOfWaterButNotOnGround)
	}

	/**
	 * @param item item used
	 * @return break time
	 */
	@Deprecated("""This function is lack of Player class and is not accurate enough, use #getBreakTime(Item, Player)
      """)
	fun getBreakTime(item: Item): Double {
		var base = this.hardness * 1.5
		if (canBeBrokenWith(item)) {
			if (toolType == ItemTool.TYPE_SHEARS && item.isShears) {
				base /= 15.0
			} else if (toolType == ItemTool.TYPE_PICKAXE && item.isPickaxe ||
					toolType == ItemTool.TYPE_AXE && item.isAxe ||
					toolType == ItemTool.TYPE_SHOVEL && item.isShovel) {
				val tier = item.tier
				when (tier) {
					ItemTool.TIER_WOODEN -> base /= 2.0
					ItemTool.TIER_STONE -> base /= 4.0
					ItemTool.TIER_IRON -> base /= 6.0
					ItemTool.TIER_DIAMOND -> base /= 8.0
					ItemTool.TIER_GOLD -> base /= 12.0
				}
			}
		} else {
			base *= 3.33
		}
		if (item.isSword) {
			base *= 0.5
		}
		return base
	}

	fun canBeBrokenWith(item: Item?): Boolean {
		return this.hardness != -1.0
	}

	override fun getSide(face: BlockFace): Block {
		return if (this.isValid) {
			getLevel().getBlock(x.toInt() + face.xOffset, y.toInt() + face.yOffset, z.toInt() + face.zOffset)
		} else this.getSide(face, 1)
	}

	override fun getSide(face: BlockFace, step: Int): Block {
		if (this.isValid) {
			return if (step == 1) {
				getLevel().getBlock(x.toInt() + face.xOffset, y.toInt() + face.yOffset, z.toInt() + face.zOffset)
			} else {
				getLevel().getBlock(x.toInt() + face.xOffset * step, y.toInt() + face.yOffset * step, z.toInt() + face.zOffset * step)
			}
		}
		val block = get(Item.AIR, 0)
		block.x = x.toInt() + face.xOffset * step.toDouble()
		block.y = y.toInt() + face.yOffset * step.toDouble()
		block.z = z.toInt() + face.zOffset * step.toDouble()
		return block
	}

	override fun up(): Block {
		return up(1)
	}

	override fun up(step: Int): Block {
		return getSide(BlockFace.UP, step)
	}

	override fun down(): Block {
		return down(1)
	}

	override fun down(step: Int): Block {
		return getSide(BlockFace.DOWN, step)
	}

	override fun north(): Block {
		return north(1)
	}

	override fun north(step: Int): Block {
		return getSide(BlockFace.NORTH, step)
	}

	override fun south(): Block {
		return south(1)
	}

	override fun south(step: Int): Block {
		return getSide(BlockFace.SOUTH, step)
	}

	override fun east(): Block {
		return east(1)
	}

	override fun east(step: Int): Block {
		return getSide(BlockFace.EAST, step)
	}

	override fun west(): Block {
		return west(1)
	}

	override fun west(step: Int): Block {
		return getSide(BlockFace.WEST, step)
	}

	override fun toString(): String {
		return "Block[" + name + "] (" + id + ":" + damage + ")"
	}

	open fun collidesWithBB(bb: AxisAlignedBB): Boolean {
		return collidesWithBB(bb, false)
	}

	fun collidesWithBB(bb: AxisAlignedBB, collisionBB: Boolean): Boolean {
		val bb1 = if (collisionBB) collisionBoundingBox else boundingBox
		return bb1 != null && bb.intersectsWith(bb1)
	}

	open fun onEntityCollide(entity: Entity) {}
	open val boundingBox: AxisAlignedBB?
		get() = recalculateBoundingBox()

	val collisionBoundingBox: AxisAlignedBB?
		get() = recalculateCollisionBoundingBox()

	protected open fun recalculateBoundingBox(): AxisAlignedBB? {
		return this
	}

	override fun getMinX(): Double {
		return x
	}

	override fun getMinY(): Double {
		return y
	}

	override fun getMinZ(): Double {
		return z
	}

	override fun getMaxX(): Double {
		return x + 1
	}

	override fun getMaxY(): Double {
		return y + 1
	}

	override fun getMaxZ(): Double {
		return z + 1
	}

	protected open fun recalculateCollisionBoundingBox(): AxisAlignedBB? {
		return boundingBox
	}

	override fun calculateIntercept(pos1: Vector3, pos2: Vector3): MovingObjectPosition {
		val bb = boundingBox ?: return null
		var v1 = pos1.getIntermediateWithXValue(pos2, bb.minX)
		var v2 = pos1.getIntermediateWithXValue(pos2, bb.maxX)
		var v3 = pos1.getIntermediateWithYValue(pos2, bb.minY)
		var v4 = pos1.getIntermediateWithYValue(pos2, bb.maxY)
		var v5 = pos1.getIntermediateWithZValue(pos2, bb.minZ)
		var v6 = pos1.getIntermediateWithZValue(pos2, bb.maxZ)
		if (v1 != null && !bb.isVectorInYZ(v1)) {
			v1 = null
		}
		if (v2 != null && !bb.isVectorInYZ(v2)) {
			v2 = null
		}
		if (v3 != null && !bb.isVectorInXZ(v3)) {
			v3 = null
		}
		if (v4 != null && !bb.isVectorInXZ(v4)) {
			v4 = null
		}
		if (v5 != null && !bb.isVectorInXY(v5)) {
			v5 = null
		}
		if (v6 != null && !bb.isVectorInXY(v6)) {
			v6 = null
		}
		var vector = v1
		if (v2 != null && (vector == null || pos1.distanceSquared(v2) < pos1.distanceSquared(vector))) {
			vector = v2
		}
		if (v3 != null && (vector == null || pos1.distanceSquared(v3) < pos1.distanceSquared(vector))) {
			vector = v3
		}
		if (v4 != null && (vector == null || pos1.distanceSquared(v4) < pos1.distanceSquared(vector))) {
			vector = v4
		}
		if (v5 != null && (vector == null || pos1.distanceSquared(v5) < pos1.distanceSquared(vector))) {
			vector = v5
		}
		if (v6 != null && (vector == null || pos1.distanceSquared(v6) < pos1.distanceSquared(vector))) {
			vector = v6
		}
		if (vector == null) {
			return null
		}
		var f = -1
		if (vector === v1) {
			f = 4
		} else if (vector === v2) {
			f = 5
		} else if (vector === v3) {
			f = 0
		} else if (vector === v4) {
			f = 1
		} else if (vector === v5) {
			f = 2
		} else if (vector === v6) {
			f = 3
		}
		return MovingObjectPosition.fromBlock(x.toInt(), y.toInt(), z.toInt(), f, vector.add(x, y, z))
	}

	val saveId: String
		get() {
			val name = javaClass.name
			return name.substring(16)
		}

	@Throws(Exception::class)
	override fun setMetadata(metadataKey: String, newMetadataValue: MetadataValue) {
		if (getLevel() != null) {
			getLevel().blockMetadata.setMetadata(this, metadataKey, newMetadataValue)
		}
	}

	@Throws(Exception::class)
	override fun getMetadata(metadataKey: String): List<MetadataValue> {
		return if (getLevel() != null) {
			getLevel().blockMetadata.getMetadata(this, metadataKey)
		} else null
	}

	@Throws(Exception::class)
	override fun hasMetadata(metadataKey: String): Boolean {
		return getLevel() != null && getLevel().blockMetadata.hasMetadata(this, metadataKey)
	}

	@Throws(Exception::class)
	override fun removeMetadata(metadataKey: String, owningPlugin: Plugin) {
		if (getLevel() != null) {
			getLevel().blockMetadata.removeMetadata(this, metadataKey, owningPlugin)
		}
	}

	override fun clone(): Block {
		return super.clone() as Block
	}

	open fun getWeakPower(face: BlockFace): Int {
		return 0
	}

	open fun getStrongPower(side: BlockFace): Int {
		return 0
	}

	open val isPowerSource: Boolean
		get() = false

	val locationHash: String
		get() = this.floorX.toString() + ":" + this.floorY + ":" + this.floorZ

	open val dropExp: Int
		get() = 0

	val isNormalBlock: Boolean
		get() = !isTransparent && isSolid && !isPowerSource

	open fun toItem(): Item? {
		return ItemBlock(this, damage, 1)
	}

	open fun canSilkTouch(): Boolean {
		return false
	}

	companion object {
		@kotlin.jvm.JvmField
		var list: Array<Class<*>>? = null
		@kotlin.jvm.JvmField
		var fullList: Array<Block?>? = null
		@kotlin.jvm.JvmField
		var light: IntArray? = null
		@kotlin.jvm.JvmField
		var lightFilter: IntArray? = null
		@kotlin.jvm.JvmField
		var solid: BooleanArray? = null
		var hardness: DoubleArray? = null
		@kotlin.jvm.JvmField
		var transparent: BooleanArray? = null

		/**
		 * if a block has can have variants
		 */
		@kotlin.jvm.JvmField
		var hasMeta: BooleanArray? = null
		fun init() {
			if (list == null) {
				list = arrayOfNulls(256)
				fullList = arrayOfNulls(4096)
				light = IntArray(256)
				lightFilter = IntArray(256)
				solid = BooleanArray(256)
				hardness = DoubleArray(256)
				transparent = BooleanArray(256)
				hasMeta = BooleanArray(256)
				list.get(BlockID.Companion.AIR) = BlockAir::class.java //0
				list.get(BlockID.Companion.STONE) = BlockStone::class.java //1
				list.get(BlockID.Companion.GRASS) = BlockGrass::class.java //2
				list.get(BlockID.Companion.DIRT) = BlockDirt::class.java //3
				list.get(BlockID.Companion.COBBLESTONE) = BlockCobblestone::class.java //4
				list.get(BlockID.Companion.PLANKS) = BlockPlanks::class.java //5
				list.get(BlockID.Companion.SAPLING) = BlockSapling::class.java //6
				list.get(BlockID.Companion.BEDROCK) = BlockBedrock::class.java //7
				list.get(BlockID.Companion.WATER) = BlockWater::class.java //8
				list.get(BlockID.Companion.STILL_WATER) = BlockWaterStill::class.java //9
				list.get(BlockID.Companion.LAVA) = BlockLava::class.java //10
				list.get(BlockID.Companion.STILL_LAVA) = BlockLavaStill::class.java //11
				list.get(BlockID.Companion.SAND) = BlockSand::class.java //12
				list.get(BlockID.Companion.GRAVEL) = BlockGravel::class.java //13
				list.get(BlockID.Companion.GOLD_ORE) = BlockOreGold::class.java //14
				list.get(BlockID.Companion.IRON_ORE) = BlockOreIron::class.java //15
				list.get(BlockID.Companion.COAL_ORE) = BlockOreCoal::class.java //16
				list.get(BlockID.Companion.WOOD) = BlockWood::class.java //17
				list.get(BlockID.Companion.LEAVES) = BlockLeaves::class.java //18
				list.get(BlockID.Companion.SPONGE) = BlockSponge::class.java //19
				list.get(BlockID.Companion.GLASS) = BlockGlass::class.java //20
				list.get(BlockID.Companion.LAPIS_ORE) = BlockOreLapis::class.java //21
				list.get(BlockID.Companion.LAPIS_BLOCK) = BlockLapis::class.java //22
				list.get(BlockID.Companion.DISPENSER) = BlockDispenser::class.java //23
				list.get(BlockID.Companion.SANDSTONE) = BlockSandstone::class.java //24
				list.get(BlockID.Companion.NOTEBLOCK) = BlockNoteblock::class.java //25
				list.get(BlockID.Companion.BED_BLOCK) = BlockBed::class.java //26
				list.get(BlockID.Companion.POWERED_RAIL) = BlockRailPowered::class.java //27
				list.get(BlockID.Companion.DETECTOR_RAIL) = BlockRailDetector::class.java //28
				list.get(BlockID.Companion.STICKY_PISTON) = BlockPistonSticky::class.java //29
				list.get(BlockID.Companion.COBWEB) = BlockCobweb::class.java //30
				list.get(BlockID.Companion.TALL_GRASS) = BlockTallGrass::class.java //31
				list.get(BlockID.Companion.DEAD_BUSH) = BlockDeadBush::class.java //32
				list.get(BlockID.Companion.PISTON) = BlockPiston::class.java //33
				list.get(BlockID.Companion.PISTON_HEAD) = BlockPistonHead::class.java //34
				list.get(BlockID.Companion.WOOL) = BlockWool::class.java //35
				list.get(BlockID.Companion.DANDELION) = BlockDandelion::class.java //37
				list.get(BlockID.Companion.FLOWER) = BlockFlower::class.java //38
				list.get(BlockID.Companion.BROWN_MUSHROOM) = BlockMushroomBrown::class.java //39
				list.get(BlockID.Companion.RED_MUSHROOM) = BlockMushroomRed::class.java //40
				list.get(BlockID.Companion.GOLD_BLOCK) = BlockGold::class.java //41
				list.get(BlockID.Companion.IRON_BLOCK) = BlockIron::class.java //42
				list.get(BlockID.Companion.DOUBLE_STONE_SLAB) = BlockDoubleSlabStone::class.java //43
				list.get(BlockID.Companion.STONE_SLAB) = BlockSlabStone::class.java //44
				list.get(BlockID.Companion.BRICKS_BLOCK) = BlockBricks::class.java //45
				list.get(BlockID.Companion.TNT) = BlockTNT::class.java //46
				list.get(BlockID.Companion.BOOKSHELF) = BlockBookshelf::class.java //47
				list.get(BlockID.Companion.MOSS_STONE) = BlockMossStone::class.java //48
				list.get(BlockID.Companion.OBSIDIAN) = BlockObsidian::class.java //49
				list.get(BlockID.Companion.TORCH) = BlockTorch::class.java //50
				list.get(BlockID.Companion.FIRE) = BlockFire::class.java //51
				list.get(BlockID.Companion.MONSTER_SPAWNER) = BlockMobSpawner::class.java //52
				list.get(BlockID.Companion.WOOD_STAIRS) = BlockStairsWood::class.java //53
				list.get(BlockID.Companion.CHEST) = BlockChest::class.java //54
				list.get(BlockID.Companion.REDSTONE_WIRE) = BlockRedstoneWire::class.java //55
				list.get(BlockID.Companion.DIAMOND_ORE) = BlockOreDiamond::class.java //56
				list.get(BlockID.Companion.DIAMOND_BLOCK) = BlockDiamond::class.java //57
				list.get(BlockID.Companion.WORKBENCH) = BlockCraftingTable::class.java //58
				list.get(BlockID.Companion.WHEAT_BLOCK) = BlockWheat::class.java //59
				list.get(BlockID.Companion.FARMLAND) = BlockFarmland::class.java //60
				list.get(BlockID.Companion.FURNACE) = BlockFurnace::class.java //61
				list.get(BlockID.Companion.BURNING_FURNACE) = BlockFurnaceBurning::class.java //62
				list.get(BlockID.Companion.SIGN_POST) = BlockSignPost::class.java //63
				list.get(BlockID.Companion.WOOD_DOOR_BLOCK) = BlockDoorWood::class.java //64
				list.get(BlockID.Companion.LADDER) = BlockLadder::class.java //65
				list.get(BlockID.Companion.RAIL) = BlockRail::class.java //66
				list.get(BlockID.Companion.COBBLESTONE_STAIRS) = BlockStairsCobblestone::class.java //67
				list.get(BlockID.Companion.WALL_SIGN) = BlockWallSign::class.java //68
				list.get(BlockID.Companion.LEVER) = BlockLever::class.java //69
				list.get(BlockID.Companion.STONE_PRESSURE_PLATE) = BlockPressurePlateStone::class.java //70
				list.get(BlockID.Companion.IRON_DOOR_BLOCK) = BlockDoorIron::class.java //71
				list.get(BlockID.Companion.WOODEN_PRESSURE_PLATE) = BlockPressurePlateWood::class.java //72
				list.get(BlockID.Companion.REDSTONE_ORE) = BlockOreRedstone::class.java //73
				list.get(BlockID.Companion.GLOWING_REDSTONE_ORE) = BlockOreRedstoneGlowing::class.java //74
				list.get(BlockID.Companion.UNLIT_REDSTONE_TORCH) = BlockRedstoneTorchUnlit::class.java
				list.get(BlockID.Companion.REDSTONE_TORCH) = BlockRedstoneTorch::class.java //76
				list.get(BlockID.Companion.STONE_BUTTON) = BlockButtonStone::class.java //77
				list.get(BlockID.Companion.SNOW_LAYER) = BlockSnowLayer::class.java //78
				list.get(BlockID.Companion.ICE) = BlockIce::class.java //79
				list.get(BlockID.Companion.SNOW_BLOCK) = BlockSnow::class.java //80
				list.get(BlockID.Companion.CACTUS) = BlockCactus::class.java //81
				list.get(BlockID.Companion.CLAY_BLOCK) = BlockClay::class.java //82
				list.get(BlockID.Companion.SUGARCANE_BLOCK) = BlockSugarcane::class.java //83
				list.get(BlockID.Companion.JUKEBOX) = BlockJukebox::class.java //84
				list.get(BlockID.Companion.FENCE) = BlockFence::class.java //85
				list.get(BlockID.Companion.PUMPKIN) = BlockPumpkin::class.java //86
				list.get(BlockID.Companion.NETHERRACK) = BlockNetherrack::class.java //87
				list.get(BlockID.Companion.SOUL_SAND) = BlockSoulSand::class.java //88
				list.get(BlockID.Companion.GLOWSTONE_BLOCK) = BlockGlowstone::class.java //89
				list.get(BlockID.Companion.NETHER_PORTAL) = BlockNetherPortal::class.java //90
				list.get(BlockID.Companion.LIT_PUMPKIN) = BlockPumpkinLit::class.java //91
				list.get(BlockID.Companion.CAKE_BLOCK) = BlockCake::class.java //92
				list.get(BlockID.Companion.UNPOWERED_REPEATER) = BlockRedstoneRepeaterUnpowered::class.java //93
				list.get(BlockID.Companion.POWERED_REPEATER) = BlockRedstoneRepeaterPowered::class.java //94
				list.get(BlockID.Companion.INVISIBLE_BEDROCK) = BlockBedrockInvisible::class.java //95
				list.get(BlockID.Companion.TRAPDOOR) = BlockTrapdoor::class.java //96
				list.get(BlockID.Companion.MONSTER_EGG) = BlockMonsterEgg::class.java //97
				list.get(BlockID.Companion.STONE_BRICKS) = BlockBricksStone::class.java //98
				list.get(BlockID.Companion.BROWN_MUSHROOM_BLOCK) = BlockHugeMushroomBrown::class.java //99
				list.get(BlockID.Companion.RED_MUSHROOM_BLOCK) = BlockHugeMushroomRed::class.java //100
				list.get(BlockID.Companion.IRON_BARS) = BlockIronBars::class.java //101
				list.get(BlockID.Companion.GLASS_PANE) = BlockGlassPane::class.java //102
				list.get(BlockID.Companion.MELON_BLOCK) = BlockMelon::class.java //103
				list.get(BlockID.Companion.PUMPKIN_STEM) = BlockStemPumpkin::class.java //104
				list.get(BlockID.Companion.MELON_STEM) = BlockStemMelon::class.java //105
				list.get(BlockID.Companion.VINE) = BlockVine::class.java //106
				list.get(BlockID.Companion.FENCE_GATE) = BlockFenceGate::class.java //107
				list.get(BlockID.Companion.BRICK_STAIRS) = BlockStairsBrick::class.java //108
				list.get(BlockID.Companion.STONE_BRICK_STAIRS) = BlockStairsStoneBrick::class.java //109
				list.get(BlockID.Companion.MYCELIUM) = BlockMycelium::class.java //110
				list.get(BlockID.Companion.WATER_LILY) = BlockWaterLily::class.java //111
				list.get(BlockID.Companion.NETHER_BRICKS) = BlockBricksNether::class.java //112
				list.get(BlockID.Companion.NETHER_BRICK_FENCE) = BlockFenceNetherBrick::class.java //113
				list.get(BlockID.Companion.NETHER_BRICKS_STAIRS) = BlockStairsNetherBrick::class.java //114
				list.get(BlockID.Companion.NETHER_WART_BLOCK) = BlockNetherWart::class.java //115
				list.get(BlockID.Companion.ENCHANTING_TABLE) = BlockEnchantingTable::class.java //116
				list.get(BlockID.Companion.BREWING_STAND_BLOCK) = BlockBrewingStand::class.java //117
				list.get(BlockID.Companion.CAULDRON_BLOCK) = BlockCauldron::class.java //118
				list.get(BlockID.Companion.END_PORTAL) = BlockEndPortal::class.java //119
				list.get(BlockID.Companion.END_PORTAL_FRAME) = BlockEndPortalFrame::class.java //120
				list.get(BlockID.Companion.END_STONE) = BlockEndStone::class.java //121
				list.get(BlockID.Companion.DRAGON_EGG) = BlockDragonEgg::class.java //122
				list.get(BlockID.Companion.REDSTONE_LAMP) = BlockRedstoneLamp::class.java //123
				list.get(BlockID.Companion.LIT_REDSTONE_LAMP) = BlockRedstoneLampLit::class.java //124
				//TODO: list[DROPPER] = BlockDropper.class; //125
				list.get(BlockID.Companion.ACTIVATOR_RAIL) = BlockRailActivator::class.java //126
				list.get(BlockID.Companion.COCOA) = BlockCocoa::class.java //127
				list.get(BlockID.Companion.SANDSTONE_STAIRS) = BlockStairsSandstone::class.java //128
				list.get(BlockID.Companion.EMERALD_ORE) = BlockOreEmerald::class.java //129
				list.get(BlockID.Companion.ENDER_CHEST) = BlockEnderChest::class.java //130
				list.get(BlockID.Companion.TRIPWIRE_HOOK) = BlockTripWireHook::class.java
				list.get(BlockID.Companion.TRIPWIRE) = BlockTripWire::class.java //132
				list.get(BlockID.Companion.EMERALD_BLOCK) = BlockEmerald::class.java //133
				list.get(BlockID.Companion.SPRUCE_WOOD_STAIRS) = BlockStairsSpruce::class.java //134
				list.get(BlockID.Companion.BIRCH_WOOD_STAIRS) = BlockStairsBirch::class.java //135
				list.get(BlockID.Companion.JUNGLE_WOOD_STAIRS) = BlockStairsJungle::class.java //136
				list.get(BlockID.Companion.BEACON) = BlockBeacon::class.java //138
				list.get(BlockID.Companion.STONE_WALL) = BlockWall::class.java //139
				list.get(BlockID.Companion.FLOWER_POT_BLOCK) = BlockFlowerPot::class.java //140
				list.get(BlockID.Companion.CARROT_BLOCK) = BlockCarrot::class.java //141
				list.get(BlockID.Companion.POTATO_BLOCK) = BlockPotato::class.java //142
				list.get(BlockID.Companion.WOODEN_BUTTON) = BlockButtonWooden::class.java //143
				list.get(BlockID.Companion.SKULL_BLOCK) = BlockSkull::class.java //144
				list.get(BlockID.Companion.ANVIL) = BlockAnvil::class.java //145
				list.get(BlockID.Companion.TRAPPED_CHEST) = BlockTrappedChest::class.java //146
				list.get(BlockID.Companion.LIGHT_WEIGHTED_PRESSURE_PLATE) = BlockWeightedPressurePlateLight::class.java //147
				list.get(BlockID.Companion.HEAVY_WEIGHTED_PRESSURE_PLATE) = BlockWeightedPressurePlateHeavy::class.java //148
				list.get(BlockID.Companion.UNPOWERED_COMPARATOR) = BlockRedstoneComparatorUnpowered::class.java //149
				list.get(BlockID.Companion.POWERED_COMPARATOR) = BlockRedstoneComparatorPowered::class.java //149
				list.get(BlockID.Companion.DAYLIGHT_DETECTOR) = BlockDaylightDetector::class.java //151
				list.get(BlockID.Companion.REDSTONE_BLOCK) = BlockRedstone::class.java //152
				list.get(BlockID.Companion.QUARTZ_ORE) = BlockOreQuartz::class.java //153
				list.get(BlockID.Companion.HOPPER_BLOCK) = BlockHopper::class.java //154
				list.get(BlockID.Companion.QUARTZ_BLOCK) = BlockQuartz::class.java //155
				list.get(BlockID.Companion.QUARTZ_STAIRS) = BlockStairsQuartz::class.java //156
				list.get(BlockID.Companion.DOUBLE_WOOD_SLAB) = BlockDoubleSlabWood::class.java //157
				list.get(BlockID.Companion.WOOD_SLAB) = BlockSlabWood::class.java //158
				list.get(BlockID.Companion.STAINED_TERRACOTTA) = BlockTerracottaStained::class.java //159
				list.get(BlockID.Companion.STAINED_GLASS_PANE) = BlockGlassPaneStained::class.java //160
				list.get(BlockID.Companion.LEAVES2) = BlockLeaves2::class.java //161
				list.get(BlockID.Companion.WOOD2) = BlockWood2::class.java //162
				list.get(BlockID.Companion.ACACIA_WOOD_STAIRS) = BlockStairsAcacia::class.java //163
				list.get(BlockID.Companion.DARK_OAK_WOOD_STAIRS) = BlockStairsDarkOak::class.java //164
				list.get(BlockID.Companion.SLIME_BLOCK) = BlockSlime::class.java //165
				list.get(BlockID.Companion.IRON_TRAPDOOR) = BlockTrapdoorIron::class.java //167
				list.get(BlockID.Companion.PRISMARINE) = BlockPrismarine::class.java //168
				list.get(BlockID.Companion.SEA_LANTERN) = BlockSeaLantern::class.java //169
				list.get(BlockID.Companion.HAY_BALE) = BlockHayBale::class.java //170
				list.get(BlockID.Companion.CARPET) = BlockCarpet::class.java //171
				list.get(BlockID.Companion.TERRACOTTA) = BlockTerracotta::class.java //172
				list.get(BlockID.Companion.COAL_BLOCK) = BlockCoal::class.java //173
				list.get(BlockID.Companion.PACKED_ICE) = BlockIcePacked::class.java //174
				list.get(BlockID.Companion.DOUBLE_PLANT) = BlockDoublePlant::class.java //175
				list.get(BlockID.Companion.STANDING_BANNER) = BlockBanner::class.java //176
				list.get(BlockID.Companion.WALL_BANNER) = BlockWallBanner::class.java //177
				list.get(BlockID.Companion.DAYLIGHT_DETECTOR_INVERTED) = BlockDaylightDetectorInverted::class.java //178
				list.get(BlockID.Companion.RED_SANDSTONE) = BlockRedSandstone::class.java //179
				list.get(BlockID.Companion.RED_SANDSTONE_STAIRS) = BlockStairsRedSandstone::class.java //180
				list.get(BlockID.Companion.DOUBLE_RED_SANDSTONE_SLAB) = BlockDoubleSlabRedSandstone::class.java //181
				list.get(BlockID.Companion.RED_SANDSTONE_SLAB) = BlockSlabRedSandstone::class.java //182
				list.get(BlockID.Companion.FENCE_GATE_SPRUCE) = BlockFenceGateSpruce::class.java //183
				list.get(BlockID.Companion.FENCE_GATE_BIRCH) = BlockFenceGateBirch::class.java //184
				list.get(BlockID.Companion.FENCE_GATE_JUNGLE) = BlockFenceGateJungle::class.java //185
				list.get(BlockID.Companion.FENCE_GATE_DARK_OAK) = BlockFenceGateDarkOak::class.java //186
				list.get(BlockID.Companion.FENCE_GATE_ACACIA) = BlockFenceGateAcacia::class.java //187
				list.get(BlockID.Companion.SPRUCE_DOOR_BLOCK) = BlockDoorSpruce::class.java //193
				list.get(BlockID.Companion.BIRCH_DOOR_BLOCK) = BlockDoorBirch::class.java //194
				list.get(BlockID.Companion.JUNGLE_DOOR_BLOCK) = BlockDoorJungle::class.java //195
				list.get(BlockID.Companion.ACACIA_DOOR_BLOCK) = BlockDoorAcacia::class.java //196
				list.get(BlockID.Companion.DARK_OAK_DOOR_BLOCK) = BlockDoorDarkOak::class.java //197
				list.get(BlockID.Companion.GRASS_PATH) = BlockGrassPath::class.java //198
				list.get(BlockID.Companion.ITEM_FRAME_BLOCK) = BlockItemFrame::class.java //199
				list.get(BlockID.Companion.CHORUS_FLOWER) = BlockChorusFlower::class.java //200
				list.get(BlockID.Companion.PURPUR_BLOCK) = BlockPurpur::class.java //201
				list.get(BlockID.Companion.PURPUR_STAIRS) = BlockStairsPurpur::class.java //203
				list.get(BlockID.Companion.UNDYED_SHULKER_BOX) = BlockUndyedShulkerBox::class.java //205
				list.get(BlockID.Companion.END_BRICKS) = BlockBricksEndStone::class.java //206
				list.get(BlockID.Companion.END_ROD) = BlockEndRod::class.java //208
				list.get(BlockID.Companion.END_GATEWAY) = BlockEndGateway::class.java //209
				list.get(BlockID.Companion.MAGMA) = BlockMagma::class.java //213
				list.get(BlockID.Companion.BLOCK_NETHER_WART_BLOCK) = BlockNetherWartBlock::class.java //214
				list.get(BlockID.Companion.RED_NETHER_BRICK) = BlockBricksRedNether::class.java //215
				list.get(BlockID.Companion.BONE_BLOCK) = BlockBone::class.java //216
				list.get(BlockID.Companion.SHULKER_BOX) = BlockShulkerBox::class.java //218
				list.get(BlockID.Companion.PURPLE_GLAZED_TERRACOTTA) = BlockTerracottaGlazedPurple::class.java //219
				list.get(BlockID.Companion.WHITE_GLAZED_TERRACOTTA) = BlockTerracottaGlazedWhite::class.java //220
				list.get(BlockID.Companion.ORANGE_GLAZED_TERRACOTTA) = BlockTerracottaGlazedOrange::class.java //221
				list.get(BlockID.Companion.MAGENTA_GLAZED_TERRACOTTA) = BlockTerracottaGlazedMagenta::class.java //222
				list.get(BlockID.Companion.LIGHT_BLUE_GLAZED_TERRACOTTA) = BlockTerracottaGlazedLightBlue::class.java //223
				list.get(BlockID.Companion.YELLOW_GLAZED_TERRACOTTA) = BlockTerracottaGlazedYellow::class.java //224
				list.get(BlockID.Companion.LIME_GLAZED_TERRACOTTA) = BlockTerracottaGlazedLime::class.java //225
				list.get(BlockID.Companion.PINK_GLAZED_TERRACOTTA) = BlockTerracottaGlazedPink::class.java //226
				list.get(BlockID.Companion.GRAY_GLAZED_TERRACOTTA) = BlockTerracottaGlazedGray::class.java //227
				list.get(BlockID.Companion.SILVER_GLAZED_TERRACOTTA) = BlockTerracottaGlazedSilver::class.java //228
				list.get(BlockID.Companion.CYAN_GLAZED_TERRACOTTA) = BlockTerracottaGlazedCyan::class.java //229
				list.get(BlockID.Companion.BLUE_GLAZED_TERRACOTTA) = BlockTerracottaGlazedBlue::class.java //231
				list.get(BlockID.Companion.BROWN_GLAZED_TERRACOTTA) = BlockTerracottaGlazedBrown::class.java //232
				list.get(BlockID.Companion.GREEN_GLAZED_TERRACOTTA) = BlockTerracottaGlazedGreen::class.java //233
				list.get(BlockID.Companion.RED_GLAZED_TERRACOTTA) = BlockTerracottaGlazedRed::class.java //234
				list.get(BlockID.Companion.BLACK_GLAZED_TERRACOTTA) = BlockTerracottaGlazedBlack::class.java //235
				list.get(BlockID.Companion.CONCRETE) = BlockConcrete::class.java //236
				list.get(BlockID.Companion.CONCRETE_POWDER) = BlockConcretePowder::class.java //237
				list.get(BlockID.Companion.CHORUS_PLANT) = BlockChorusPlant::class.java //240
				list.get(BlockID.Companion.STAINED_GLASS) = BlockGlassStained::class.java //241
				list.get(BlockID.Companion.PODZOL) = BlockPodzol::class.java //243
				list.get(BlockID.Companion.BEETROOT_BLOCK) = BlockBeetroot::class.java //244
				list.get(BlockID.Companion.STONECUTTER) = BlockStonecutter::class.java //245
				list.get(BlockID.Companion.GLOWING_OBSIDIAN) = BlockObsidianGlowing::class.java //246
				//list[NETHER_REACTOR] = BlockNetherReactor.class; //247 Should not be removed

				//TODO: list[PISTON_EXTENSION] = BlockPistonExtension.class; //250
				list.get(BlockID.Companion.OBSERVER) = BlockObserver::class.java //251
				for (id in 0..255) {
					val c = list.get(id)
					if (c != null) {
						var block: Block
						try {
							block = c.newInstance() as Block
							try {
								val constructor = c.getDeclaredConstructor(Int::class.javaPrimitiveType)
								constructor.isAccessible = true
								for (data in 0..15) {
									fullList!![id shl 4 or data] = constructor.newInstance(data) as Block
								}
								hasMeta!![id] = true
							} catch (ignore: NoSuchMethodException) {
								var data = 0
								while (data < 16) {
									fullList!![id shl 4 or data] = block
									++data
								}
							}
						} catch (e: Exception) {
							Server.instance!!.logger.error("Error while registering " + c.name, e)
							var data = 0
							while (data < 16) {
								fullList!![id shl 4 or data] = BlockUnknown(id, data)
								++data
							}
							return
						}
						solid!![id] = block.isSolid
						transparent!![id] = block.isTransparent
						hardness!![id] = block.hardness
						light!![id] = block.lightLevel
						if (block.isSolid) {
							if (block.isTransparent) {
								if (block is BlockLiquid || block is BlockIce) {
									lightFilter!![id] = 2
								} else {
									lightFilter!![id] = 1
								}
							} else {
								lightFilter!![id] = 15
							}
						} else {
							lightFilter!![id] = 1
						}
					} else {
						lightFilter!![id] = 1
						for (data in 0..15) {
							fullList!![id shl 4 or data] = BlockUnknown(id, data)
						}
					}
				}
			}
		}

		operator fun get(id: Int): Block {
			return fullList!![id shl 4]!!.clone()
		}

		operator fun get(id: Int, meta: Int?): Block {
			return if (meta != null) {
				fullList!![(id shl 4) + meta]!!.clone()
			} else {
				fullList!![id shl 4]!!.clone()
			}
		}

		operator fun get(id: Int, meta: Int?, pos: Position?): Block {
			val block = fullList!![id shl 4 or (meta ?: 0)]!!.clone()
			if (pos != null) {
				block.x = pos.x
				block.y = pos.y
				block.z = pos.z
				block.level = pos.level
			}
			return block
		}

		operator fun get(id: Int, data: Int): Block {
			return fullList!![(id shl 4) + data]!!.clone()
		}

		@kotlin.jvm.JvmStatic
		operator fun get(fullId: Int, level: Level?, x: Int, y: Int, z: Int): Block {
			val block = fullList!![fullId]!!.clone()
			block.x = x.toDouble()
			block.y = y.toDouble()
			block.z = z.toDouble()
			block.level = level
			return block
		}

		private fun toolBreakTimeBonus0(
				toolType: Int, toolTier: Int, isWoolBlock: Boolean, isCobweb: Boolean): Double {
			if (toolType == ItemTool.TYPE_SWORD) return if (isCobweb) 15.0 else 1.0
			if (toolType == ItemTool.TYPE_SHEARS) return if (isWoolBlock) 5.0 else 15.0
			return if (toolType == ItemTool.TYPE_NONE) 1.0 else when (toolTier) {
				ItemTool.TIER_WOODEN -> 2.0
				ItemTool.TIER_STONE -> 4.0
				ItemTool.TIER_IRON -> 6.0
				ItemTool.TIER_DIAMOND -> 8.0
				ItemTool.TIER_GOLD -> 12.0
				else -> 1.0
			}
		}

		private fun speedBonusByEfficiencyLore0(efficiencyLoreLevel: Int): Double {
			return if (efficiencyLoreLevel == 0) 0 else (efficiencyLoreLevel * efficiencyLoreLevel + 1).toDouble()
		}

		private fun speedRateByHasteLore0(hasteLoreLevel: Int): Double {
			return 1.0 + 0.2 * hasteLoreLevel
		}

		private fun toolType0(item: Item): Int {
			if (item.isSword) return ItemTool.TYPE_SWORD
			if (item.isShovel) return ItemTool.TYPE_SHOVEL
			if (item.isPickaxe) return ItemTool.TYPE_PICKAXE
			if (item.isAxe) return ItemTool.TYPE_AXE
			return if (item.isShears) ItemTool.TYPE_SHEARS else ItemTool.TYPE_NONE
		}

		private fun correctTool0(blockToolType: Int, item: Item): Boolean {
			return blockToolType == ItemTool.TYPE_SWORD && item.isSword ||
					blockToolType == ItemTool.TYPE_SHOVEL && item.isShovel ||
					blockToolType == ItemTool.TYPE_PICKAXE && item.isPickaxe ||
					blockToolType == ItemTool.TYPE_AXE && item.isAxe ||
					blockToolType == ItemTool.TYPE_SHEARS && item.isShears || blockToolType == ItemTool.TYPE_NONE
		}

		//http://minecraft.gamepedia.com/Breaking
		private fun breakTime0(blockHardness: Double, correctTool: Boolean, canHarvestWithHand: Boolean,
							   blockId: Int, toolType: Int, toolTier: Int, efficiencyLoreLevel: Int, hasteEffectLevel: Int,
							   insideOfWaterWithoutAquaAffinity: Boolean, outOfWaterButNotOnGround: Boolean): Double {
			val baseTime = (if (correctTool || canHarvestWithHand) 1.5 else 5.0) * blockHardness
			var speed = 1.0 / baseTime
			val isWoolBlock = blockId == BlockID.Companion.WOOL
			val isCobweb = blockId == BlockID.Companion.COBWEB
			if (correctTool) speed *= toolBreakTimeBonus0(toolType, toolTier, isWoolBlock, isCobweb)
			speed += speedBonusByEfficiencyLore0(efficiencyLoreLevel)
			speed *= speedRateByHasteLore0(hasteEffectLevel)
			if (insideOfWaterWithoutAquaAffinity) speed *= 0.2
			if (outOfWaterButNotOnGround) speed *= 0.2
			return 1.0 / speed
		}

		@kotlin.jvm.JvmStatic
		@JvmOverloads
		fun equals(b1: Block?, b2: Block?, checkDamage: Boolean = true): Boolean {
			return b1 != null && b2 != null && b1.id == b2.id && (!checkDamage || b1.damage == b2.damage)
		}
	}
}