package cn.nukkit.itemimport

import cn.nukkit.item.Item

cn.nukkit.block.Block.idimport cn.nukkit.block.Block.damageimport cn.nukkit.Player.getServerimport cn.nukkit.event.Event.isCancelledimport cn.nukkit.event.player.PlayerEatFoodEvent.foodimport cn.nukkit.Player.foodDataimport cn.nukkit.PlayerFood.addFoodLevelimport cn.nukkit.entity.EntityHumanType.getInventoryimport cn.nukkit.inventory.BaseInventory.addItemimport cn.nukkit.entity.Entity.removeAllEffectsimport cn.nukkit.block.Block.isSolidimport cn.nukkit.entity.Entity.teleportimport cn.nukkit.block.Block.Companion.getimport cn.nukkit.block.Block.canBePlacedimport cn.nukkit.block.Block.cloneimport cn.nukkit.entity.Entity.addEffectimport cn.nukkit.event.entity.EntityDamageEvent.causeimport cn.nukkit.inventory.PlayerInventory.armorContentsimport cn.nukkit.entity.Entity.attackimport cn.nukkit.entity.Entity.setOnFireimport cn.nukkit.event.entity.EntityCombustEvent.durationimport cn.nukkit.inventory.BaseInventory.containsimport cn.nukkit.Player.isCreativeimport cn.nukkit.entity.EntityHumanType.offhandInventoryimport cn.nukkit.inventory.Inventory.containsimport cn.nukkit.Player.isSurvivalimport cn.nukkit.inventory.BaseInventory.sendContentsimport cn.nukkit.inventory.Inventory.sendContentsimport cn.nukkit.entity.EntityHuman.eyeHeightimport cn.nukkit.event.Event.setCancelledimport cn.nukkit.event.entity.EntityShootBowEvent.getProjectileimport cn.nukkit.entity.Entity.killimport cn.nukkit.inventory.PlayerInventory.sendContentsimport cn.nukkit.entity.Entity.setMotionimport cn.nukkit.entity.Entity.motionimport cn.nukkit.event.entity.EntityShootBowEvent.forceimport cn.nukkit.entity.projectile.EntityArrow.pickupModeimport cn.nukkit.inventory.Inventory.removeItemimport cn.nukkit.inventory.PlayerInventory.setItemInHandimport cn.nukkit.entity.Entity.spawnToAllimport cn.nukkit.Player.dataPacketimport cn.nukkit.inventory.PlayerInventory.itemInHandimport cn.nukkit.block.Block.toolTypeimport cn.nukkit.block.Block.getBreakTimeimport cn.nukkit.inventory.PlayerInventory.helmetimport cn.nukkit.inventory.PlayerInventory.setHelmetimport cn.nukkit.inventory.PlayerInventory.chestplateimport cn.nukkit.inventory.PlayerInventory.setChestplateimport cn.nukkit.inventory.PlayerInventory.leggingsimport cn.nukkit.inventory.PlayerInventory.setLeggingsimport cn.nukkit.inventory.PlayerInventory.bootsimport cn.nukkit.inventory.PlayerInventory.setBootsimport cn.nukkit.inventory.Inventory.clearimport cn.nukkit.inventory.PlayerInventory.heldItemIndeximport cn.nukkit.block.Block.nameimport cn.nukkit.block.Block.setDamageimport cn.nukkit.block.Block.getSideimport cn.nukkit.event.player.PlayerBucketEvent.itemimport cn.nukkit.block.Block.canBeFlowedIntoimport cn.nukkit.PlayerFood.levelimport cn.nukkit.PlayerFood.maxLevelimport cn.nukkit.PlayerFood.sendFoodLevelimport cn.nukkit.entity.projectile.EntityThrownTrident.itemimport cn.nukkit.block.Block.canPassThroughimport cn.nukkit.inventory.BaseInventory.decreaseCountimport cn.nukkit.entity.Entity.isGlidingimport cn.nukkit.Player.setMotionimport cn.nukkit.block.BlockRail.orientationimport cn.nukkit.block.Block.isTransparentimport cn.nukkit.block.Block.upimport cn.nukkit.block.Block.boundingBoximport cn.nukkit.block.BlockFire.isBlockTopFacingSurfaceSolidimport cn.nukkit.block.Block.downimport cn.nukkit.block.BlockFire.canNeighborBurnimport cn.nukkit.block.BlockFire.tickRateimport cn.nukkit.Player.stopFishingimport cn.nukkit.Player.startFishingimport cn.nukkit.Server.tickimport cn.nukkit.Player.lastEnderPearlThrowingTickimport cn.nukkit.Player.onThrowEnderPearlimport cn.nukkit.Player.lastChorusFruitTeleportimport cn.nukkit.Player.onChorusFruitTeleportimport cn.nukkit.inventory.BaseInventory.canAddItemimport cn.nukkit.entity.Entity.getDirectionVectorimport cn.nukkit.item.food.Food.NodeIDMetaimport cn.nukkit.Playerimport cn.nukkit.event.player.PlayerEatFoodEventimport kotlin.jvm.JvmOverloadsimport cn.nukkit.item.food.Foodimport cn.nukkit.item.food.Food.NodeIDMetaPluginimport java.util.LinkedHashMapimport cn.nukkit.item.food.FoodNormalimport cn.nukkit.item.ItemIDimport cn.nukkit.item.food.FoodEffectiveimport cn.nukkit.item.food.FoodInBowlimport cn.nukkit.item.food.FoodChorusFruitimport cn.nukkit.item.food.FoodMilkimport cn.nukkit.item.ItemBucketimport cn.nukkit.item.ItemBowlimport java.util.LinkedListimport cn.nukkit.math.NukkitRandomimport cn.nukkit.math.Vector3import cn.nukkit.block.BlockLiquidimport cn.nukkit.level.Soundimport cn.nukkit.event.player.PlayerTeleportEventimport cn.nukkit.block.BlockIDimport cn.nukkit.nbt.tag.CompoundTagimport cn.nukkit.nbt.tag.ListTagimport cn.nukkit.item.enchantment.Enchantmentimport cn.nukkit.nbt.tag.StringTagimport cn.nukkit.nbt.NBTIOimport java.nio.ByteOrderimport java.io.IOExceptionimport java.lang.RuntimeExceptionimport cn.nukkit.inventory.Fuelimport cn.nukkit.math.BlockFaceimport java.lang.CloneNotSupportedExceptionimport cn.nukkit.item.ItemShovelIronimport cn.nukkit.item.ItemPickaxeIronimport cn.nukkit.item.ItemAxeIronimport cn.nukkit.item.ItemFlintSteelimport cn.nukkit.item.ItemAppleimport cn.nukkit.item.ItemBowimport cn.nukkit.item.ItemArrowimport cn.nukkit.item.ItemCoalimport cn.nukkit.item.ItemDiamondimport cn.nukkit.item.ItemIngotIronimport cn.nukkit.item.ItemIngotGoldimport cn.nukkit.item.ItemSwordIronimport cn.nukkit.item.ItemSwordWoodimport cn.nukkit.item.ItemShovelWoodimport cn.nukkit.item.ItemPickaxeWoodimport cn.nukkit.item.ItemAxeWoodimport cn.nukkit.item.ItemSwordStoneimport cn.nukkit.item.ItemShovelStoneimport cn.nukkit.item.ItemPickaxeStoneimport cn.nukkit.item.ItemAxeStoneimport cn.nukkit.item.ItemSwordDiamondimport cn.nukkit.item.ItemShovelDiamondimport cn.nukkit.item.ItemPickaxeDiamondimport cn.nukkit.item.ItemAxeDiamondimport cn.nukkit.item.ItemStickimport cn.nukkit.item.ItemMushroomStewimport cn.nukkit.item.ItemSwordGoldimport cn.nukkit.item.ItemShovelGoldimport cn.nukkit.item.ItemPickaxeGoldimport cn.nukkit.item.ItemAxeGoldimport cn.nukkit.item.ItemStringimport cn.nukkit.item.ItemFeatherimport cn.nukkit.item.ItemGunpowderimport cn.nukkit.item.ItemHoeWoodimport cn.nukkit.item.ItemHoeStoneimport cn.nukkit.item.ItemHoeIronimport cn.nukkit.item.ItemHoeDiamondimport cn.nukkit.item.ItemHoeGoldimport cn.nukkit.item.ItemSeedsWheatimport cn.nukkit.item.ItemWheatimport cn.nukkit.item.ItemBreadimport cn.nukkit.item.ItemHelmetLeatherimport cn.nukkit.item.ItemChestplateLeatherimport cn.nukkit.item.ItemLeggingsLeatherimport cn.nukkit.item.ItemBootsLeatherimport cn.nukkit.item.ItemHelmetChainimport cn.nukkit.item.ItemChestplateChainimport cn.nukkit.item.ItemLeggingsChainimport cn.nukkit.item.ItemBootsChainimport cn.nukkit.item.ItemHelmetIronimport cn.nukkit.item.ItemChestplateIronimport cn.nukkit.item.ItemLeggingsIronimport cn.nukkit.item.ItemBootsIronimport cn.nukkit.item.ItemHelmetDiamondimport cn.nukkit.item.ItemChestplateDiamondimport cn.nukkit.item.ItemLeggingsDiamondimport cn.nukkit.item.ItemBootsDiamondimport cn.nukkit.item.ItemHelmetGoldimport cn.nukkit.item.ItemChestplateGoldimport cn.nukkit.item.ItemLeggingsGoldimport cn.nukkit.item.ItemBootsGoldimport cn.nukkit.item.ItemFlintimport cn.nukkit.item.ItemPorkchopRawimport cn.nukkit.item.ItemPorkchopCookedimport cn.nukkit.item.ItemPaintingimport cn.nukkit.item.ItemAppleGoldimport cn.nukkit.item.ItemSignimport cn.nukkit.item.ItemDoorWoodimport cn.nukkit.item.ItemMinecartimport cn.nukkit.item.ItemSaddleimport cn.nukkit.item.ItemDoorIronimport cn.nukkit.item.ItemRedstoneimport cn.nukkit.item.ItemSnowballimport cn.nukkit.item.ItemBoatimport cn.nukkit.item.ItemLeatherimport cn.nukkit.item.ItemBrickimport cn.nukkit.item.ItemClayimport cn.nukkit.item.ItemSugarcaneimport cn.nukkit.item.ItemPaperimport cn.nukkit.item.ItemBookimport cn.nukkit.item.ItemSlimeballimport cn.nukkit.item.ItemMinecartChestimport cn.nukkit.item.ItemEggimport cn.nukkit.item.ItemCompassimport cn.nukkit.item.ItemFishingRodimport cn.nukkit.item.ItemClockimport cn.nukkit.item.ItemGlowstoneDustimport cn.nukkit.item.ItemFishimport cn.nukkit.item.ItemFishCookedimport cn.nukkit.item.ItemDyeimport cn.nukkit.item.ItemBoneimport cn.nukkit.item.ItemSugarimport cn.nukkit.item.ItemCakeimport cn.nukkit.item.ItemBedimport cn.nukkit.item.ItemRedstoneRepeaterimport cn.nukkit.item.ItemCookieimport cn.nukkit.item.ItemMapimport cn.nukkit.item.ItemShearsimport cn.nukkit.item.ItemMelonimport cn.nukkit.item.ItemSeedsPumpkinimport cn.nukkit.item.ItemSeedsMelonimport cn.nukkit.item.ItemBeefRawimport cn.nukkit.item.ItemSteakimport cn.nukkit.item.ItemChickenRawimport cn.nukkit.item.ItemChickenCookedimport cn.nukkit.item.ItemRottenFleshimport cn.nukkit.item.ItemEnderPearlimport cn.nukkit.item.ItemBlazeRodimport cn.nukkit.item.ItemGhastTearimport cn.nukkit.item.ItemNuggetGoldimport cn.nukkit.item.ItemNetherWartimport cn.nukkit.item.ItemPotionimport cn.nukkit.item.ItemGlassBottleimport cn.nukkit.item.ItemSpiderEyeimport cn.nukkit.item.ItemSpiderEyeFermentedimport cn.nukkit.item.ItemBlazePowderimport cn.nukkit.item.ItemMagmaCreamimport cn.nukkit.item.ItemBrewingStandimport cn.nukkit.item.ItemCauldronimport cn.nukkit.item.ItemEnderEyeimport cn.nukkit.item.ItemMelonGlisteringimport cn.nukkit.item.ItemSpawnEggimport cn.nukkit.item.ItemExpBottleimport cn.nukkit.item.ItemFireChargeimport cn.nukkit.item.ItemBookAndQuillimport cn.nukkit.item.ItemBookWrittenimport cn.nukkit.item.ItemEmeraldimport cn.nukkit.item.ItemItemFrameimport cn.nukkit.item.ItemFlowerPotimport cn.nukkit.item.ItemCarrotimport cn.nukkit.item.ItemPotatoimport cn.nukkit.item.ItemPotatoBakedimport cn.nukkit.item.ItemPotatoPoisonousimport cn.nukkit.item.ItemCarrotGoldenimport cn.nukkit.item.ItemSkullimport cn.nukkit.item.ItemCarrotOnAStickimport cn.nukkit.item.ItemNetherStarimport cn.nukkit.item.ItemPumpkinPieimport cn.nukkit.item.ItemFireworkimport cn.nukkit.item.ItemBookEnchantedimport cn.nukkit.item.ItemRedstoneComparatorimport cn.nukkit.item.ItemNetherBrickimport cn.nukkit.item.ItemQuartzimport cn.nukkit.item.ItemMinecartTNTimport cn.nukkit.item.ItemMinecartHopperimport cn.nukkit.item.ItemPrismarineShardimport cn.nukkit.item.ItemHopperimport cn.nukkit.item.ItemRabbitRawimport cn.nukkit.item.ItemRabbitCookedimport cn.nukkit.item.ItemRabbitStewimport cn.nukkit.item.ItemRabbitFootimport cn.nukkit.item.ItemHorseArmorLeatherimport cn.nukkit.item.ItemHorseArmorIronimport cn.nukkit.item.ItemHorseArmorGoldimport cn.nukkit.item.ItemHorseArmorDiamondimport cn.nukkit.item.ItemPrismarineCrystalsimport cn.nukkit.item.ItemMuttonRawimport cn.nukkit.item.ItemMuttonCookedimport cn.nukkit.item.ItemEndCrystalimport cn.nukkit.item.ItemDoorSpruceimport cn.nukkit.item.ItemDoorBirchimport cn.nukkit.item.ItemDoorJungleimport cn.nukkit.item.ItemDoorAcaciaimport cn.nukkit.item.ItemDoorDarkOakimport cn.nukkit.item.ItemChorusFruitimport cn.nukkit.item.ItemPotionSplashimport cn.nukkit.item.ItemPotionLingeringimport cn.nukkit.item.ItemElytraimport cn.nukkit.item.ItemBannerimport cn.nukkit.item.ItemTotemimport cn.nukkit.item.ItemTridentimport cn.nukkit.item.ItemBeetrootimport cn.nukkit.item.ItemSeedsBeetrootimport cn.nukkit.item.ItemBeetrootSoupimport cn.nukkit.item.ItemSalmonimport cn.nukkit.item.ItemClownfishimport cn.nukkit.item.ItemPufferfishimport cn.nukkit.item.ItemSalmonCookedimport cn.nukkit.item.ItemDriedKelpimport cn.nukkit.item.ItemAppleGoldEnchantedimport cn.nukkit.item.ItemTurtleShellimport cn.nukkit.item.ItemSweetBerriesimport cn.nukkit.item.ItemRecord11import cn.nukkit.item.ItemRecordCatimport cn.nukkit.item.ItemRecord13import cn.nukkit.item.ItemRecordBlocksimport cn.nukkit.item.ItemRecordChirpimport cn.nukkit.item.ItemRecordFarimport cn.nukkit.item.ItemRecordWardimport cn.nukkit.item.ItemRecordMallimport cn.nukkit.item.ItemRecordMellohiimport cn.nukkit.item.ItemRecordStalimport cn.nukkit.item.ItemRecordStradimport cn.nukkit.item.ItemRecordWaitimport cn.nukkit.item.ItemShieldimport cn.nukkit.utils.MainLoggerimport cn.nukkit.item.ItemBlockimport cn.nukkit.item.randomitem.RandomItemimport cn.nukkit.item.randomitem.Fishingimport cn.nukkit.item.randomitem.ConstantItemSelectorimport cn.nukkit.potion.Potionimport cn.nukkit.utils.DyeColorimport java.util.HashMapimport cn.nukkit.item.enchantment.EnchantmentTypeimport cn.nukkit.item.enchantment.bow.EnchantmentBowimport cn.nukkit.item.enchantment.loot.EnchantmentLootimport cn.nukkit.item.enchantment.damage.EnchantmentDamageimport cn.nukkit.entity.EntitySmiteimport cn.nukkit.entity.EntityArthropodimport cn.nukkit.item.enchantment.trident.EnchantmentTridentimport cn.nukkit.item.enchantment.protection.EnchantmentProtectionimport cn.nukkit.event.entity.EntityDamageEventimport cn.nukkit.event.entity.EntityDamageEvent.DamageCauseimport cn.nukkit.item.enchantment.protection.EnchantmentProtectionAllimport cn.nukkit.item.enchantment.protection.EnchantmentProtectionFireimport cn.nukkit.item.enchantment.protection.EnchantmentProtectionFallimport cn.nukkit.item.enchantment.protection.EnchantmentProtectionExplosionimport cn.nukkit.item.enchantment.protection.EnchantmentProtectionProjectileimport cn.nukkit.item.enchantment.EnchantmentThornsimport cn.nukkit.item.enchantment.EnchantmentWaterBreathimport cn.nukkit.item.enchantment.EnchantmentWaterWorkerimport cn.nukkit.item.enchantment.EnchantmentWaterWalkerimport cn.nukkit.item.enchantment.damage.EnchantmentDamageAllimport cn.nukkit.item.enchantment.damage.EnchantmentDamageSmiteimport cn.nukkit.item.enchantment.damage.EnchantmentDamageArthropodsimport cn.nukkit.item.enchantment.EnchantmentKnockbackimport cn.nukkit.item.enchantment.EnchantmentFireAspectimport cn.nukkit.item.enchantment.loot.EnchantmentLootWeaponimport cn.nukkit.item.enchantment.EnchantmentEfficiencyimport cn.nukkit.item.enchantment.EnchantmentSilkTouchimport cn.nukkit.item.enchantment.EnchantmentDurabilityimport cn.nukkit.item.enchantment.loot.EnchantmentLootDiggingimport cn.nukkit.item.enchantment.bow.EnchantmentBowPowerimport cn.nukkit.item.enchantment.bow.EnchantmentBowKnockbackimport cn.nukkit.item.enchantment.bow.EnchantmentBowFlameimport cn.nukkit.item.enchantment.bow.EnchantmentBowInfinityimport cn.nukkit.item.enchantment.loot.EnchantmentLootFishingimport cn.nukkit.item.enchantment.EnchantmentLureimport cn.nukkit.item.enchantment.EnchantmentFrostWalkerimport cn.nukkit.item.enchantment.EnchantmentMendingimport cn.nukkit.item.enchantment.EnchantmentBindingCurseimport cn.nukkit.item.enchantment.EnchantmentVanishingCurseimport cn.nukkit.item.enchantment.trident.EnchantmentTridentImpalingimport cn.nukkit.item.enchantment.trident.EnchantmentTridentRiptideimport cn.nukkit.item.enchantment.trident.EnchantmentTridentLoyaltyimport cn.nukkit.item.enchantment.trident.EnchantmentTridentChannelingimport cn.nukkit.item.enchantment.Enchantment.UnknownEnchantmentimport java.util.HashSetimport cn.nukkit.item.enchantment.EnchantmentEntryimport cn.nukkit.item.enchantment.EnchantmentListimport cn.nukkit.item.ItemArmorimport cn.nukkit.block.BlockPumpkinimport cn.nukkit.entity.EntityHumanTypeimport cn.nukkit.event.entity.EntityDamageByEntityEventimport cn.nukkit.event.entity.EntityCombustByEntityEventimport cn.nukkit.item.ItemToolimport cn.nukkit.inventory.Inventoryimport cn.nukkit.nbt.tag.DoubleTagimport cn.nukkit.nbt.tag.FloatTagimport cn.nukkit.entity.projectile.EntityArrowimport cn.nukkit.event.entity.EntityShootBowEventimport cn.nukkit.entity.projectile.EntityProjectileimport cn.nukkit.event.entity.ProjectileLaunchEventimport cn.nukkit.network.protocol.LevelSoundEventPacketimport cn.nukkit.utils.BlockColorimport cn.nukkit.item.ProjectileItemimport java.awt.image.BufferedImageimport kotlin.jvm.Throwsimport javax.imageio.ImageIOimport java.awt.Graphics2Dimport java.io.ByteArrayOutputStreamimport java.io.ByteArrayInputStreamimport cn.nukkit.network.protocol.ClientboundMapItemDataPacketimport cn.nukkit.entity.item.EntityBoatimport cn.nukkit.block.BlockWaterimport cn.nukkit.item.ItemEdibleimport cn.nukkit.item.ItemDurableimport cn.nukkit.nbt.tag.ByteTagimport cn.nukkit.utils.BannerPatternimport cn.nukkit.nbt.tag.IntTagimport cn.nukkit.block.BlockAirimport cn.nukkit.event.player.PlayerBucketFillEventimport cn.nukkit.math.BlockFace.Planeimport cn.nukkit.block.BlockLavaimport cn.nukkit.event.player.PlayerBucketEmptyEventimport cn.nukkit.network.protocol.UpdateBlockPacketimport cn.nukkit.event.player.PlayerItemConsumeEventimport cn.nukkit.entity.projectile.EntityThrownTridentimport cn.nukkit.item.ItemFirework.FireworkExplosionimport cn.nukkit.entity.item.EntityFireworkimport cn.nukkit.item.ItemFirework.FireworkExplosion.ExplosionTypeimport cn.nukkit.utils.Railimport cn.nukkit.block.BlockRailimport cn.nukkit.entity.item.EntityMinecartEmptyimport cn.nukkit.level.format.FullChunkimport cn.nukkit.entity.item.EntityPainting.Motiveimport cn.nukkit.entity.item.EntityPaintingimport cn.nukkit.item.ItemRecordimport cn.nukkit.event.entity.CreatureSpawnEventimport cn.nukkit.event.entity.CreatureSpawnEvent.SpawnReasonimport cn.nukkit.item.ItemColorArmorimport cn.nukkit.block.BlockBedrockimport cn.nukkit.block.BlockObsidianimport cn.nukkit.block.BlockSolidimport cn.nukkit.block.BlockSolidMetaimport cn.nukkit.block.BlockFireimport cn.nukkit.event.block.BlockIgniteEventimport cn.nukkit.entity.projectile.EntityEnderPearlimport cn.nukkit.item.ItemBookWritableimport cn.nukkit.entity.item.EntityMinecartTNTimport cn.nukkit.entity.item.EntityMinecartChestimport cn.nukkit.entity.item.EntityMinecartHopper
/**
 * Created by Snake1999 on 2016/1/14.
 * Package cn.nukkit.item in project nukkit.
 */
class ItemSpiderEye @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.SPIDER_EYE, meta, count, "Spider Eye")