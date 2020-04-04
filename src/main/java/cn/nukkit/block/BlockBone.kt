package cn.nukkit.block

import cn.nukkit.Player.isSurvival
import cn.nukkit.Player.addWindow
import cn.nukkit.Player.uIInventory
import cn.nukkit.Player.viewingEnderChest
import cn.nukkit.Server.getDifficulty
import cn.nukkit.Player.isCreative
import cn.nukkit.Player.isSpectator
import cn.nukkit.Player.setCraftingGrid
import cn.nukkit.Server.logger
import cn.nukkit.Player.foodData
import cn.nukkit.PlayerFood.level
import cn.nukkit.PlayerFood.maxLevel
import cn.nukkit.Player.sendMessage
import cn.nukkit.Player.sleepOn
import cn.nukkit.Player.isAdventure
import cn.nukkit.block.BlockSolid
import cn.nukkit.block.BlockID
import cn.nukkit.utils.BlockColor
import cn.nukkit.block.BlockRedstoneLamp
import cn.nukkit.event.redstone.RedstoneUpdateEvent
import cn.nukkit.math.BlockFace
import cn.nukkit.Player
import cn.nukkit.block.BlockMushroom
import kotlin.jvm.JvmOverloads
import cn.nukkit.block.BlockSolidMeta
import cn.nukkit.utils.Faceable
import cn.nukkit.block.BlockDoor
import cn.nukkit.block.BlockLeaves
import cn.nukkit.block.BlockTransparent
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.math.MathHelper
import cn.nukkit.block.BlockTransparentMeta
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.blockentity.BlockEntityMusic
import cn.nukkit.network.protocol.LevelSoundEventPacket
import cn.nukkit.network.protocol.BlockEventPacket
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.level.Sound
import cn.nukkit.blockentity.BlockEntityItemFrame
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.block.BlockFenceGate
import cn.nukkit.event.block.DoorToggleEvent
import cn.nukkit.block.BlockDirt
import cn.nukkit.level.particle.BoneMealParticle
import cn.nukkit.level.generator.``object`
import cn.nukkit.math.NukkitRandom
import cn.nukkit.math.Vector3
import cn.nukkit.block.BlockAir
import cn.nukkit.event.block.BlockSpreadEvent
import cn.nukkit.blockentity.BlockEntityJukebox
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.blockentity.BlockEntityBeacon
import cn.nukkit.inventory.BeaconInventory
import cn.nukkit.block.BlockRail
import cn.nukkit.utils.Rail
import cn.nukkit.block.BlockRailPowered
import cn.nukkit.block.BlockCrops
import cn.nukkit.block.BlockDoorWood
import cn.nukkit.block.BlockFallable
import cn.nukkit.network.protocol.LevelEventPacket
import cn.nukkit.math.SimpleAxisAlignedBB
import cn.nukkit.block.BlockFence
import cn.nukkit.block.BlockPressurePlateBase
import cn.nukkit.entity.EntityLiving
import cn.nukkit.block.BlockFlowable
import java.util.stream.Collectors
import java.util.HashMap
import cn.nukkit.event.block.BlockRedstoneEvent
import cn.nukkit.block.BlockLever.LeverOrientation
import java.lang.IllegalArgumentException
import cn.nukkit.entity.item.EntityMinecartAbstract
import cn.nukkit.utils.DyeColor
import cn.nukkit.level.generator.``object`
import cn.nukkit.block.BlockButton
import cn.nukkit.event.block.BlockGrowEvent
import cn.nukkit.event.block.BlockFadeEvent
import cn.nukkit.block.BlockPistonBase
import it.unimi.dsi.fastutil.longs.Long2ByteMap
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap
import cn.nukkit.block.BlockWater
import cn.nukkit.event.block.BlockFromToEvent
import cn.nukkit.block.BlockLiquid
import cn.nukkit.event.block.LiquidFlowEvent
import cn.nukkit.level.particle.SmokeParticle
import cn.nukkit.block.BlockPlanks
import cn.nukkit.block.BlockGlass
import cn.nukkit.block.BlockSandstone
import cn.nukkit.block.BlockRedstoneDiode
import cn.nukkit.block.BlockRedstoneComparator
import cn.nukkit.blockentity.BlockEntityComparator
import cn.nukkit.block.BlockThin
import cn.nukkit.block.BlockMonsterEgg
import cn.nukkit.block.BlockOreRedstone
import cn.nukkit.block.BlockNetherPortal
import cn.nukkit.blockentity.BlockEntityHopper
import cn.nukkit.inventory.ContainerInventory
import java.util.HashSet
import cn.nukkit.blockentity.BlockEntityEnderChest
import cn.nukkit.nbt.tag.StringTag
import cn.nukkit.blockentity.BlockEntityFurnace
import cn.nukkit.event.block.LeavesDecayEvent
import it.unimi.dsi.fastutil.longs.LongArraySet
import it.unimi.dsi.fastutil.longs.LongSet
import cn.nukkit.block.BlockDoubleSlabStone
import cn.nukkit.block.BlockBone
import cn.nukkit.block.BlockNetherWart
import cn.nukkit.block.BlockGrass
import cn.nukkit.block.BlockNetherBrick
import cn.nukkit.block.BlockCocoa
import cn.nukkit.block.BlockWood
import cn.nukkit.math.NukkitMath
import cn.nukkit.nbt.tag.IntTag
import cn.nukkit.blockentity.BlockEntityBanner
import cn.nukkit.event.entity.EntityCombustByBlockEvent
import cn.nukkit.event.entity.EntityDamageByBlockEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.level.GameRule
import cn.nukkit.event.block.BlockIgniteEvent
import cn.nukkit.entity.item.EntityPrimedTNT
import cn.nukkit.entity.projectile.EntityArrow
import cn.nukkit.event.block.BlockBurnEvent
import cn.nukkit.block.BlockTNT
import cn.nukkit.block.BlockStairs
import cn.nukkit.block.BlockSlab
import cn.nukkit.block.BlockSnowLayer
import cn.nukkit.nbt.tag.DoubleTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.entity.item.EntityFallingBlock
import cn.nukkit.block.BlockLava
import cn.nukkit.blockentity.BlockEntityPistonArm
import cn.nukkit.block.BlockPistonHead
import cn.nukkit.event.block.BlockPistonChangeEvent
import cn.nukkit.block.BlockPistonBase.BlocksCalculator
import cn.nukkit.block.BlockGlassPane
import cn.nukkit.block.BlockDoubleSlab
import cn.nukkit.blockentity.BlockEntityFlowerPot
import cn.nukkit.block.BlockFlowerPot
import cn.nukkit.block.BlockDaylightDetector
import cn.nukkit.block.BlockRailActivator
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.block.BlockDoublePlant
import cn.nukkit.block.BlockFurnaceBurning
import cn.nukkit.block.BlockIce
import cn.nukkit.metadata.Metadatable
import cn.nukkit.level.MovingObjectPosition
import kotlin.jvm.Throws
import cn.nukkit.metadata.MetadataValue
import cn.nukkit.block.BlockStone
import cn.nukkit.block.BlockCobblestone
import cn.nukkit.block.BlockSapling
import cn.nukkit.block.BlockBedrock
import cn.nukkit.block.BlockWaterStill
import cn.nukkit.block.BlockLavaStill
import cn.nukkit.block.BlockSand
import cn.nukkit.block.BlockGravel
import cn.nukkit.block.BlockOreGold
import cn.nukkit.block.BlockOreIron
import cn.nukkit.block.BlockOreCoal
import cn.nukkit.block.BlockSponge
import cn.nukkit.block.BlockOreLapis
import cn.nukkit.block.BlockLapis
import cn.nukkit.block.BlockDispenser
import cn.nukkit.block.BlockNoteblock
import cn.nukkit.block.BlockBed
import cn.nukkit.block.BlockRailDetector
import cn.nukkit.block.BlockPistonSticky
import cn.nukkit.block.BlockCobweb
import cn.nukkit.block.BlockTallGrass
import cn.nukkit.block.BlockDeadBush
import cn.nukkit.block.BlockPiston
import cn.nukkit.block.BlockWool
import cn.nukkit.block.BlockDandelion
import cn.nukkit.block.BlockFlower
import cn.nukkit.block.BlockMushroomBrown
import cn.nukkit.block.BlockMushroomRed
import cn.nukkit.block.BlockGold
import cn.nukkit.block.BlockIron
import cn.nukkit.block.BlockSlabStone
import cn.nukkit.block.BlockBricks
import cn.nukkit.block.BlockBookshelf
import cn.nukkit.block.BlockMossStone
import cn.nukkit.block.BlockObsidian
import cn.nukkit.block.BlockTorch
import cn.nukkit.block.BlockFire
import cn.nukkit.block.BlockMobSpawner
import cn.nukkit.block.BlockStairsWood
import cn.nukkit.block.BlockChest
import cn.nukkit.block.BlockRedstoneWire
import cn.nukkit.block.BlockOreDiamond
import cn.nukkit.block.BlockDiamond
import cn.nukkit.block.BlockCraftingTable
import cn.nukkit.block.BlockWheat
import cn.nukkit.block.BlockFarmland
import cn.nukkit.block.BlockFurnace
import cn.nukkit.block.BlockSignPost
import cn.nukkit.block.BlockLadder
import cn.nukkit.block.BlockStairsCobblestone
import cn.nukkit.block.BlockWallSign
import cn.nukkit.block.BlockLever
import cn.nukkit.block.BlockPressurePlateStone
import cn.nukkit.block.BlockDoorIron
import cn.nukkit.block.BlockPressurePlateWood
import cn.nukkit.block.BlockOreRedstoneGlowing
import cn.nukkit.block.BlockRedstoneTorchUnlit
import cn.nukkit.block.BlockRedstoneTorch
import cn.nukkit.block.BlockButtonStone
import cn.nukkit.block.BlockSnow
import cn.nukkit.block.BlockCactus
import cn.nukkit.block.BlockClay
import cn.nukkit.block.BlockSugarcane
import cn.nukkit.block.BlockJukebox
import cn.nukkit.block.BlockPumpkin
import cn.nukkit.block.BlockNetherrack
import cn.nukkit.block.BlockSoulSand
import cn.nukkit.block.BlockGlowstone
import cn.nukkit.block.BlockPumpkinLit
import cn.nukkit.block.BlockCake
import cn.nukkit.block.BlockRedstoneRepeaterUnpowered
import cn.nukkit.block.BlockRedstoneRepeaterPowered
import cn.nukkit.block.BlockBedrockInvisible
import cn.nukkit.block.BlockTrapdoor
import cn.nukkit.block.BlockBricksStone
import cn.nukkit.block.BlockHugeMushroomBrown
import cn.nukkit.block.BlockHugeMushroomRed
import cn.nukkit.block.BlockIronBars
import cn.nukkit.block.BlockMelon
import cn.nukkit.block.BlockStemPumpkin
import cn.nukkit.block.BlockStemMelon
import cn.nukkit.block.BlockVine
import cn.nukkit.block.BlockStairsBrick
import cn.nukkit.block.BlockStairsStoneBrick
import cn.nukkit.block.BlockMycelium
import cn.nukkit.block.BlockWaterLily
import cn.nukkit.block.BlockBricksNether
import cn.nukkit.block.BlockFenceNetherBrick
import cn.nukkit.block.BlockStairsNetherBrick
import cn.nukkit.block.BlockEnchantingTable
import cn.nukkit.block.BlockBrewingStand
import cn.nukkit.block.BlockCauldron
import cn.nukkit.block.BlockEndPortal
import cn.nukkit.block.BlockEndPortalFrame
import cn.nukkit.block.BlockEndStone
import cn.nukkit.block.BlockDragonEgg
import cn.nukkit.block.BlockRedstoneLampLit
import cn.nukkit.block.BlockStairsSandstone
import cn.nukkit.block.BlockOreEmerald
import cn.nukkit.block.BlockEnderChest
import cn.nukkit.block.BlockTripWireHook
import cn.nukkit.block.BlockTripWire
import cn.nukkit.block.BlockEmerald
import cn.nukkit.block.BlockStairsSpruce
import cn.nukkit.block.BlockStairsBirch
import cn.nukkit.block.BlockStairsJungle
import cn.nukkit.block.BlockBeacon
import cn.nukkit.block.BlockWall
import cn.nukkit.block.BlockCarrot
import cn.nukkit.block.BlockPotato
import cn.nukkit.block.BlockButtonWooden
import cn.nukkit.block.BlockSkull
import cn.nukkit.block.BlockAnvil
import cn.nukkit.block.BlockTrappedChest
import cn.nukkit.block.BlockWeightedPressurePlateLight
import cn.nukkit.block.BlockWeightedPressurePlateHeavy
import cn.nukkit.block.BlockRedstoneComparatorUnpowered
import cn.nukkit.block.BlockRedstoneComparatorPowered
import cn.nukkit.block.BlockRedstone
import cn.nukkit.block.BlockOreQuartz
import cn.nukkit.block.BlockHopper
import cn.nukkit.block.BlockQuartz
import cn.nukkit.block.BlockStairsQuartz
import cn.nukkit.block.BlockDoubleSlabWood
import cn.nukkit.block.BlockSlabWood
import cn.nukkit.block.BlockTerracottaStained
import cn.nukkit.block.BlockGlassPaneStained
import cn.nukkit.block.BlockLeaves2
import cn.nukkit.block.BlockWood2
import cn.nukkit.block.BlockStairsAcacia
import cn.nukkit.block.BlockStairsDarkOak
import cn.nukkit.block.BlockSlime
import cn.nukkit.block.BlockTrapdoorIron
import cn.nukkit.block.BlockPrismarine
import cn.nukkit.block.BlockSeaLantern
import cn.nukkit.block.BlockHayBale
import cn.nukkit.block.BlockCarpet
import cn.nukkit.block.BlockTerracotta
import cn.nukkit.block.BlockCoal
import cn.nukkit.block.BlockIcePacked
import cn.nukkit.block.BlockBanner
import cn.nukkit.block.BlockWallBanner
import cn.nukkit.block.BlockDaylightDetectorInverted
import cn.nukkit.block.BlockRedSandstone
import cn.nukkit.block.BlockStairsRedSandstone
import cn.nukkit.block.BlockDoubleSlabRedSandstone
import cn.nukkit.block.BlockSlabRedSandstone
import cn.nukkit.block.BlockFenceGateSpruce
import cn.nukkit.block.BlockFenceGateBirch
import cn.nukkit.block.BlockFenceGateJungle
import cn.nukkit.block.BlockFenceGateDarkOak
import cn.nukkit.block.BlockFenceGateAcacia
import cn.nukkit.block.BlockDoorSpruce
import cn.nukkit.block.BlockDoorBirch
import cn.nukkit.block.BlockDoorJungle
import cn.nukkit.block.BlockDoorAcacia
import cn.nukkit.block.BlockDoorDarkOak
import cn.nukkit.block.BlockGrassPath
import cn.nukkit.block.BlockItemFrame
import cn.nukkit.block.BlockChorusFlower
import cn.nukkit.block.BlockPurpur
import cn.nukkit.block.BlockStairsPurpur
import cn.nukkit.block.BlockUndyedShulkerBox
import cn.nukkit.block.BlockBricksEndStone
import cn.nukkit.block.BlockEndRod
import cn.nukkit.block.BlockEndGateway
import cn.nukkit.block.BlockMagma
import cn.nukkit.block.BlockNetherWartBlock
import cn.nukkit.block.BlockBricksRedNether
import cn.nukkit.block.BlockShulkerBox
import cn.nukkit.block.BlockTerracottaGlazedPurple
import cn.nukkit.block.BlockTerracottaGlazedWhite
import cn.nukkit.block.BlockTerracottaGlazedOrange
import cn.nukkit.block.BlockTerracottaGlazedMagenta
import cn.nukkit.block.BlockTerracottaGlazedLightBlue
import cn.nukkit.block.BlockTerracottaGlazedYellow
import cn.nukkit.block.BlockTerracottaGlazedLime
import cn.nukkit.block.BlockTerracottaGlazedPink
import cn.nukkit.block.BlockTerracottaGlazedGray
import cn.nukkit.block.BlockTerracottaGlazedSilver
import cn.nukkit.block.BlockTerracottaGlazedCyan
import cn.nukkit.block.BlockTerracottaGlazedBlue
import cn.nukkit.block.BlockTerracottaGlazedBrown
import cn.nukkit.block.BlockTerracottaGlazedGreen
import cn.nukkit.block.BlockTerracottaGlazedRed
import cn.nukkit.block.BlockTerracottaGlazedBlack
import cn.nukkit.block.BlockConcrete
import cn.nukkit.block.BlockConcretePowder
import cn.nukkit.block.BlockChorusPlant
import cn.nukkit.block.BlockGlassStained
import cn.nukkit.block.BlockPodzol
import cn.nukkit.block.BlockBeetroot
import cn.nukkit.block.BlockStonecutter
import cn.nukkit.block.BlockObsidianGlowing
import cn.nukkit.block.BlockObserver
import java.lang.NoSuchMethodException
import cn.nukkit.block.BlockUnknown
import cn.nukkit.item.food.Food
import cn.nukkit.api.API.Definition
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.blockentity.BlockEntityBed
import cn.nukkit.blockentity.BlockEntityCauldron
import cn.nukkit.event.player.PlayerBucketFillEvent
import cn.nukkit.event.player.PlayerBucketEmptyEvent
import cn.nukkit.blockentity.BlockEntityChest
import cn.nukkit.blockentity.BlockEntityEnchantTable
import cn.nukkit.inventory.EnchantInventory
import cn.nukkit.blockentity.BlockEntityBrewingStand
import cn.nukkit.inventory.AnvilInventory
import cn.nukkit.event.player.PlayerInteractEvent
import cn.nukkit.event.entity.EntityInteractEvent
import cn.nukkit.math.BlockFace.Plane
import java.util.EnumSet
import cn.nukkit.level.generator.``object`
import cn.nukkit.level.generator.``object`
import cn.nukkit.level.generator.``object`
import cn.nukkit.level.generator.``object`
import cn.nukkit.level.generator.``object`
import cn.nukkit.level.generator.``object`
import cn.nukkit.blockentity.BlockEntitySign
import cn.nukkit.blockentity.BlockEntitySkull
import cn.nukkit.block.BlockMeta
import cn.nukkit.level.GlobalBlockPalette
import cn.nukkit.utils.TerracottaColor
import cn.nukkit.block.BlockTerracottaGlazed
import cn.nukkit.utils.LevelException
import cn.nukkit.blockentity.BlockEntityShulkerBox
import cn.nukkit.inventory.ShulkerBoxInventory
import cn.nukkit.item.*
import cn.nukkit.nbt.NBTIO

/**
 * @author CreeperFace
 */
class BlockBone @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta), Faceable {
	override val id: Int
		get() = BlockID.Companion.BONE_BLOCK

	override val name: String
		get() = "Bone Block"

	override val hardness: Double
		get() = 2

	override val resistance: Double
		get() = 10

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(ItemBlock(this))
		} else arrayOfNulls(0)
	}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x7)
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		this.setDamage(this.damage and 0x3 or FACES[face.index])
		getLevel().setBlock(block, this, true)
		return true
	}

	override val color: BlockColor
		get() = BlockColor.SAND_BLOCK_COLOR

	companion object {
		private val FACES = intArrayOf(
				0,
				0,
				8,
				8,
				4,
				4
		)
	}
}