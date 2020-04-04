package cn.nukkit.item

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.block.Block
import cn.nukkit.block.BlockID
import cn.nukkit.entity.Entity
import cn.nukkit.inventory.Fuel
import cn.nukkit.item.ItemPotionSplash
import cn.nukkit.item.ItemTotem
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.nbt.tag.StringTag
import cn.nukkit.nbt.tag.Tag
import cn.nukkit.utils.Binary
import cn.nukkit.utils.Config
import cn.nukkit.utils.MainLogger
import cn.nukkit.utils.Utils
import java.io.IOException
import java.nio.ByteOrder
import java.util.*
import java.util.regex.Pattern

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class Item @JvmOverloads constructor(id: Int, meta: Int? = 0, count: Int = 1, name: String = UNKNOWN_STR) : Cloneable, BlockID, ItemID {
	protected var block: Block? = null
	val id: Int
	protected var meta = 0
	protected var hasMeta = true
	var compoundTag: ByteArray? = ByteArray(0)
		private set
	private var cachedNBT: CompoundTag? = null
	var count: Int
	protected var durability = 0
	protected var name: String
	fun hasMeta(): Boolean {
		return hasMeta
	}

	open fun canBeActivated(): Boolean {
		return false
	}

	fun setCompoundTag(tag: CompoundTag?): Item {
		setNamedTag(tag)
		return this
	}

	fun setCompoundTag(tags: ByteArray?): Item {
		compoundTag = tags
		cachedNBT = null
		return this
	}

	fun hasCompoundTag(): Boolean {
		return compoundTag != null && compoundTag!!.size > 0
	}

	fun hasCustomBlockData(): Boolean {
		if (!hasCompoundTag()) {
			return false
		}
		val tag = namedTag
		return tag!!.contains("BlockEntityTag") && tag["BlockEntityTag"] is CompoundTag
	}

	fun clearCustomBlockData(): Item {
		if (!hasCompoundTag()) {
			return this
		}
		val tag = namedTag
		if (tag!!.contains("BlockEntityTag") && tag["BlockEntityTag"] is CompoundTag) {
			tag.remove("BlockEntityTag")
			setNamedTag(tag)
		}
		return this
	}

	fun setCustomBlockData(compoundTag: CompoundTag): Item {
		val tags = compoundTag.copy()
		tags.name = "BlockEntityTag"
		val tag: CompoundTag?
		tag = if (!hasCompoundTag()) {
			CompoundTag()
		} else {
			namedTag
		}
		tag!!.putCompound("BlockEntityTag", tags)
		setNamedTag(tag)
		return this
	}

	val customBlockData: CompoundTag?
		get() {
			if (!hasCompoundTag()) {
				return null
			}
			val tag = namedTag
			if (tag!!.contains("BlockEntityTag")) {
				val bet = tag["BlockEntityTag"]
				if (bet is CompoundTag) {
					return bet
				}
			}
			return null
		}

	fun hasEnchantments(): Boolean {
		if (!hasCompoundTag()) {
			return false
		}
		val tag = namedTag
		if (tag!!.contains("ench")) {
			val enchTag = tag["ench"]
			return enchTag is ListTag<*>
		}
		return false
	}

	fun getEnchantment(id: Int): Enchantment? {
		return getEnchantment((id and 0xffff).toShort())
	}

	fun getEnchantment(id: Short): Enchantment? {
		if (!hasEnchantments()) {
			return null
		}
		for (entry in namedTag!!.getList("ench", CompoundTag::class.java).all) {
			if (entry.getShort("id") == id.toInt()) {
				val e: Enchantment = Enchantment.Companion.getEnchantment(entry.getShort("id"))
				if (e != null) {
					e.setLevel(entry.getShort("lvl"), false)
					return e
				}
			}
		}
		return null
	}

	fun addEnchantment(vararg enchantments: Enchantment) {
		val tag: CompoundTag?
		tag = if (!hasCompoundTag()) {
			CompoundTag()
		} else {
			namedTag
		}
		val ench: ListTag<CompoundTag>
		if (!tag!!.contains("ench")) {
			ench = ListTag("ench")
			tag.putList(ench)
		} else {
			ench = tag.getList("ench", CompoundTag::class.java)
		}
		for (enchantment in enchantments) {
			var found = false
			for (k in 0 until ench.size()) {
				val entry = ench[k]
				if (entry.getShort("id") == enchantment.getId()) {
					ench.add(k, CompoundTag()
							.putShort("id", enchantment.getId())
							.putShort("lvl", enchantment.level)
					)
					found = true
					break
				}
			}
			if (!found) {
				ench.add(CompoundTag()
						.putShort("id", enchantment.getId())
						.putShort("lvl", enchantment.level)
				)
			}
		}
		setNamedTag(tag)
	}

	val enchantments: Array<Enchantment?>
		get() {
			if (!hasEnchantments()) {
				return arrayOfNulls(0)
			}
			val enchantments: MutableList<Enchantment> = ArrayList()
			val ench = namedTag!!.getList("ench", CompoundTag::class.java)
			for (entry in ench.all) {
				val e: Enchantment = Enchantment.Companion.getEnchantment(entry.getShort("id"))
				if (e != null) {
					e.setLevel(entry.getShort("lvl"), false)
					enchantments.add(e)
				}
			}
			return enchantments.toTypedArray()
		}

	fun hasCustomName(): Boolean {
		if (!hasCompoundTag()) {
			return false
		}
		val tag = namedTag
		if (tag!!.contains("display")) {
			val tag1 = tag["display"]
			return tag1 is CompoundTag && tag1.contains("Name") && tag1["Name"] is StringTag
		}
		return false
	}

	val customName: String
		get() {
			if (!hasCompoundTag()) {
				return ""
			}
			val tag = namedTag
			if (tag!!.contains("display")) {
				val tag1 = tag["display"]
				if (tag1 is CompoundTag && tag1.contains("Name") && tag1["Name"] is StringTag) {
					return tag1.getString("Name")
				}
			}
			return ""
		}

	fun setCustomName(name: String?): Item {
		if (name == null || name == "") {
			clearCustomName()
		}
		val tag: CompoundTag?
		tag = if (!hasCompoundTag()) {
			CompoundTag()
		} else {
			namedTag
		}
		if (tag!!.contains("display") && tag["display"] is CompoundTag) {
			tag.getCompound("display").putString("Name", name)
		} else {
			tag.putCompound("display", CompoundTag("display")
					.putString("Name", name)
			)
		}
		setNamedTag(tag)
		return this
	}

	fun clearCustomName(): Item {
		if (!hasCompoundTag()) {
			return this
		}
		val tag = namedTag
		if (tag!!.contains("display") && tag["display"] is CompoundTag) {
			tag.getCompound("display").remove("Name")
			if (tag.getCompound("display").isEmpty) {
				tag.remove("display")
			}
			setNamedTag(tag)
		}
		return this
	}

	val lore: Array<String>
		get() {
			val tag = getNamedTagEntry("display")
			val lines = ArrayList<String>()
			if (tag is CompoundTag) {
				val lore = tag.getList("Lore", StringTag::class.java)
				if (lore.size() > 0) {
					for (stringTag in lore.all) {
						lines.add(stringTag.data)
					}
				}
			}
			return lines.toTypedArray()
		}

	fun setLore(vararg lines: String?): Item {
		val tag: CompoundTag?
		tag = if (!hasCompoundTag()) {
			CompoundTag()
		} else {
			namedTag
		}
		val lore = ListTag<StringTag>("Lore")
		for (line in lines) {
			lore.add(StringTag("", line))
		}
		if (!tag!!.contains("display")) {
			tag.putCompound("display", CompoundTag("display").putList(lore))
		} else {
			tag.getCompound("display").putList(lore)
		}
		setNamedTag(tag)
		return this
	}

	fun getNamedTagEntry(name: String?): Tag? {
		val tag = namedTag
		return if (tag != null) {
			if (tag.contains(name)) tag[name] else null
		} else null
	}

	val namedTag: CompoundTag?
		get() {
			if (!hasCompoundTag()) {
				return null
			}
			if (cachedNBT == null) {
				cachedNBT = parseCompoundTag(compoundTag)
			}
			if (cachedNBT != null) {
				cachedNBT!!.name = ""
			}
			return cachedNBT
		}

	fun setNamedTag(tag: CompoundTag?): Item {
		if (tag!!.isEmpty) {
			return clearNamedTag()
		}
		tag.name = null
		cachedNBT = tag
		compoundTag = writeCompoundTag(tag)
		return this
	}

	fun clearNamedTag(): Item {
		return this.setCompoundTag(ByteArray(0))
	}

	fun writeCompoundTag(tag: CompoundTag?): ByteArray {
		return try {
			tag!!.name = ""
			NBTIO.write(tag, ByteOrder.LITTLE_ENDIAN)
		} catch (e: IOException) {
			throw RuntimeException(e)
		}
	}

	val isNull: Boolean
		get() = count <= 0 || id == BlockID.AIR

	fun getName(): String {
		return if (hasCustomName()) customName else name
	}

	fun canBePlaced(): Boolean {
		return block != null && block!!.canBePlaced()
	}

	open fun getBlock(): Block? {
		return if (block != null) {
			block!!.clone()
		} else {
			Block[BlockID.AIR]
		}
	}

	open var damage: Int?
		get() = meta
		set(meta) {
			if (meta != null) {
				this.meta = meta and 0xffff
			} else {
				hasMeta = false
			}
		}

	open val maxStackSize: Int
		get() = 64

	val fuelTime: Short?
		get() {
			if (!Fuel.duration.containsKey(id)) {
				return null
			}
			return if (id != ItemID.Companion.BUCKET || meta == 10) {
				Fuel.duration[id]
			} else null
		}

	open fun useOn(entity: Entity?): Boolean {
		return false
	}

	open fun useOn(block: Block): Boolean {
		return false
	}

	open val isTool: Boolean
		get() = false

	open val maxDurability: Int
		get() = -1

	open val tier: Int
		get() = 0

	open val isPickaxe: Boolean
		get() = false

	open val isAxe: Boolean
		get() = false

	open val isSword: Boolean
		get() = false

	open val isShovel: Boolean
		get() = false

	open val isHoe: Boolean
		get() = false

	open val isShears: Boolean
		get() = false

	open val isArmor: Boolean
		get() = false

	open val isHelmet: Boolean
		get() = false

	open val isChestplate: Boolean
		get() = false

	open val isLeggings: Boolean
		get() = false

	open val isBoots: Boolean
		get() = false

	open val enchantAbility: Int
		get() = 0

	open val attackDamage: Int
		get() = 1

	open val armorPoints: Int
		get() = 0

	open val toughness: Int
		get() = 0

	open val isUnbreakable: Boolean
		get() = false

	open fun onUse(player: Player, ticksUsed: Int): Boolean {
		return false
	}

	open fun onRelease(player: Player, ticksUsed: Int): Boolean {
		return false
	}

	override fun toString(): String {
		return "Item " + name + " (" + id + ":" + (if (!hasMeta) "?" else meta) + ")x" + count + if (hasCompoundTag()) " tags:0x" + Binary.bytesToHexString(compoundTag) else ""
	}

	fun getDestroySpeed(block: Block?, player: Player?): Int {
		return 1
	}

	open fun onActivate(level: Level, player: Player, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double): Boolean {
		return false
	}

	/**
	 * Called when a player uses the item on air, for example throwing a projectile.
	 * Returns whether the item was changed, for example count decrease or durability change.
	 *
	 * @param player player
	 * @param directionVector direction
	 * @return item changed
	 */
	open fun onClickAir(player: Player, directionVector: Vector3): Boolean {
		return false
	}

	override fun equals(item: Any?): Boolean {
		return item is Item && this.equals(item, true)
	}

	@JvmOverloads
	fun equals(item: Item, checkDamage: Boolean, checkCompound: Boolean = true): Boolean {
		if (id == item.id && (!checkDamage || damage == item.damage)) {
			if (checkCompound) {
				if (Arrays.equals(compoundTag, item.compoundTag)) {
					return true
				} else if (hasCompoundTag() && item.hasCompoundTag()) {
					return namedTag == item.namedTag
				}
			} else {
				return true
			}
		}
		return false
	}

	/**
	 * Returns whether the specified item stack has the same ID, damage, NBT and count as this item stack.
	 *
	 * @param other item
	 * @return equal
	 */
	fun equalsExact(other: Item): Boolean {
		return this.equals(other, true, true) && count == other.count
	}

	@Deprecated("")
	fun deepEquals(item: Item): Boolean {
		return equals(item, true)
	}

	@Deprecated("")
	fun deepEquals(item: Item, checkDamage: Boolean): Boolean {
		return equals(item, checkDamage, true)
	}

	@Deprecated("")
	fun deepEquals(item: Item, checkDamage: Boolean, checkCompound: Boolean): Boolean {
		return equals(item, checkDamage, checkCompound)
	}

	public override fun clone(): Item {
		return try {
			val item = super.clone() as Item
			item.compoundTag = compoundTag!!.clone()
			item
		} catch (e: CloneNotSupportedException) {
			null
		}
	}

	companion object {
		//Normal Item IDs
		protected var UNKNOWN_STR = "Unknown"
		var list: Array<Class<*>>? = null
		fun init() {
			if (list == null) {
				list = arrayOfNulls(65535)
				list.get(ItemID.Companion.IRON_SHOVEL) = ItemShovelIron::class.java //256
				list.get(ItemID.Companion.IRON_PICKAXE) = ItemPickaxeIron::class.java //257
				list.get(ItemID.Companion.IRON_AXE) = ItemAxeIron::class.java //258
				list.get(ItemID.Companion.FLINT_AND_STEEL) = ItemFlintSteel::class.java //259
				list.get(ItemID.Companion.APPLE) = ItemApple::class.java //260
				list.get(ItemID.Companion.BOW) = ItemBow::class.java //261
				list.get(ItemID.Companion.ARROW) = ItemArrow::class.java //262
				list.get(ItemID.Companion.COAL) = ItemCoal::class.java //263
				list.get(ItemID.Companion.DIAMOND) = ItemDiamond::class.java //264
				list.get(ItemID.Companion.IRON_INGOT) = ItemIngotIron::class.java //265
				list.get(ItemID.Companion.GOLD_INGOT) = ItemIngotGold::class.java //266
				list.get(ItemID.Companion.IRON_SWORD) = ItemSwordIron::class.java //267
				list.get(ItemID.Companion.WOODEN_SWORD) = ItemSwordWood::class.java //268
				list.get(ItemID.Companion.WOODEN_SHOVEL) = ItemShovelWood::class.java //269
				list.get(ItemID.Companion.WOODEN_PICKAXE) = ItemPickaxeWood::class.java //270
				list.get(ItemID.Companion.WOODEN_AXE) = ItemAxeWood::class.java //271
				list.get(ItemID.Companion.STONE_SWORD) = ItemSwordStone::class.java //272
				list.get(ItemID.Companion.STONE_SHOVEL) = ItemShovelStone::class.java //273
				list.get(ItemID.Companion.STONE_PICKAXE) = ItemPickaxeStone::class.java //274
				list.get(ItemID.Companion.STONE_AXE) = ItemAxeStone::class.java //275
				list.get(ItemID.Companion.DIAMOND_SWORD) = ItemSwordDiamond::class.java //276
				list.get(ItemID.Companion.DIAMOND_SHOVEL) = ItemShovelDiamond::class.java //277
				list.get(ItemID.Companion.DIAMOND_PICKAXE) = ItemPickaxeDiamond::class.java //278
				list.get(ItemID.Companion.DIAMOND_AXE) = ItemAxeDiamond::class.java //279
				list.get(ItemID.Companion.STICK) = ItemStick::class.java //280
				list.get(ItemID.Companion.BOWL) = ItemBowl::class.java //281
				list.get(ItemID.Companion.MUSHROOM_STEW) = ItemMushroomStew::class.java //282
				list.get(ItemID.Companion.GOLD_SWORD) = ItemSwordGold::class.java //283
				list.get(ItemID.Companion.GOLD_SHOVEL) = ItemShovelGold::class.java //284
				list.get(ItemID.Companion.GOLD_PICKAXE) = ItemPickaxeGold::class.java //285
				list.get(ItemID.Companion.GOLD_AXE) = ItemAxeGold::class.java //286
				list.get(ItemID.Companion.STRING) = ItemString::class.java //287
				list.get(ItemID.Companion.FEATHER) = ItemFeather::class.java //288
				list.get(ItemID.Companion.GUNPOWDER) = ItemGunpowder::class.java //289
				list.get(ItemID.Companion.WOODEN_HOE) = ItemHoeWood::class.java //290
				list.get(ItemID.Companion.STONE_HOE) = ItemHoeStone::class.java //291
				list.get(ItemID.Companion.IRON_HOE) = ItemHoeIron::class.java //292
				list.get(ItemID.Companion.DIAMOND_HOE) = ItemHoeDiamond::class.java //293
				list.get(ItemID.Companion.GOLD_HOE) = ItemHoeGold::class.java //294
				list.get(ItemID.Companion.WHEAT_SEEDS) = ItemSeedsWheat::class.java //295
				list.get(ItemID.Companion.WHEAT) = ItemWheat::class.java //296
				list.get(ItemID.Companion.BREAD) = ItemBread::class.java //297
				list.get(ItemID.Companion.LEATHER_CAP) = ItemHelmetLeather::class.java //298
				list.get(ItemID.Companion.LEATHER_TUNIC) = ItemChestplateLeather::class.java //299
				list.get(ItemID.Companion.LEATHER_PANTS) = ItemLeggingsLeather::class.java //300
				list.get(ItemID.Companion.LEATHER_BOOTS) = ItemBootsLeather::class.java //301
				list.get(ItemID.Companion.CHAIN_HELMET) = ItemHelmetChain::class.java //302
				list.get(ItemID.Companion.CHAIN_CHESTPLATE) = ItemChestplateChain::class.java //303
				list.get(ItemID.Companion.CHAIN_LEGGINGS) = ItemLeggingsChain::class.java //304
				list.get(ItemID.Companion.CHAIN_BOOTS) = ItemBootsChain::class.java //305
				list.get(ItemID.Companion.IRON_HELMET) = ItemHelmetIron::class.java //306
				list.get(ItemID.Companion.IRON_CHESTPLATE) = ItemChestplateIron::class.java //307
				list.get(ItemID.Companion.IRON_LEGGINGS) = ItemLeggingsIron::class.java //308
				list.get(ItemID.Companion.IRON_BOOTS) = ItemBootsIron::class.java //309
				list.get(ItemID.Companion.DIAMOND_HELMET) = ItemHelmetDiamond::class.java //310
				list.get(ItemID.Companion.DIAMOND_CHESTPLATE) = ItemChestplateDiamond::class.java //311
				list.get(ItemID.Companion.DIAMOND_LEGGINGS) = ItemLeggingsDiamond::class.java //312
				list.get(ItemID.Companion.DIAMOND_BOOTS) = ItemBootsDiamond::class.java //313
				list.get(ItemID.Companion.GOLD_HELMET) = ItemHelmetGold::class.java //314
				list.get(ItemID.Companion.GOLD_CHESTPLATE) = ItemChestplateGold::class.java //315
				list.get(ItemID.Companion.GOLD_LEGGINGS) = ItemLeggingsGold::class.java //316
				list.get(ItemID.Companion.GOLD_BOOTS) = ItemBootsGold::class.java //317
				list.get(ItemID.Companion.FLINT) = ItemFlint::class.java //318
				list.get(ItemID.Companion.RAW_PORKCHOP) = ItemPorkchopRaw::class.java //319
				list.get(ItemID.Companion.COOKED_PORKCHOP) = ItemPorkchopCooked::class.java //320
				list.get(ItemID.Companion.PAINTING) = ItemPainting::class.java //321
				list.get(ItemID.Companion.GOLDEN_APPLE) = ItemAppleGold::class.java //322
				list.get(ItemID.Companion.SIGN) = ItemSign::class.java //323
				list.get(ItemID.Companion.WOODEN_DOOR) = ItemDoorWood::class.java //324
				list.get(ItemID.Companion.BUCKET) = ItemBucket::class.java //325
				list.get(ItemID.Companion.MINECART) = ItemMinecart::class.java //328
				list.get(ItemID.Companion.SADDLE) = ItemSaddle::class.java //329
				list.get(ItemID.Companion.IRON_DOOR) = ItemDoorIron::class.java //330
				list.get(ItemID.Companion.REDSTONE) = ItemRedstone::class.java //331
				list.get(ItemID.Companion.SNOWBALL) = ItemSnowball::class.java //332
				list.get(ItemID.Companion.BOAT) = ItemBoat::class.java //333
				list.get(ItemID.Companion.LEATHER) = ItemLeather::class.java //334
				list.get(ItemID.Companion.BRICK) = ItemBrick::class.java //336
				list.get(ItemID.Companion.CLAY) = ItemClay::class.java //337
				list.get(ItemID.Companion.SUGARCANE) = ItemSugarcane::class.java //338
				list.get(ItemID.Companion.PAPER) = ItemPaper::class.java //339
				list.get(ItemID.Companion.BOOK) = ItemBook::class.java //340
				list.get(ItemID.Companion.SLIMEBALL) = ItemSlimeball::class.java //341
				list.get(ItemID.Companion.MINECART_WITH_CHEST) = ItemMinecartChest::class.java //342
				list.get(ItemID.Companion.EGG) = ItemEgg::class.java //344
				list.get(ItemID.Companion.COMPASS) = ItemCompass::class.java //345
				list.get(ItemID.Companion.FISHING_ROD) = ItemFishingRod::class.java //346
				list.get(ItemID.Companion.CLOCK) = ItemClock::class.java //347
				list.get(ItemID.Companion.GLOWSTONE_DUST) = ItemGlowstoneDust::class.java //348
				list.get(ItemID.Companion.RAW_FISH) = ItemFish::class.java //349
				list.get(ItemID.Companion.COOKED_FISH) = ItemFishCooked::class.java //350
				list.get(ItemID.Companion.DYE) = ItemDye::class.java //351
				list.get(ItemID.Companion.BONE) = ItemBone::class.java //352
				list.get(ItemID.Companion.SUGAR) = ItemSugar::class.java //353
				list.get(ItemID.Companion.CAKE) = ItemCake::class.java //354
				list.get(ItemID.Companion.BED) = ItemBed::class.java //355
				list.get(ItemID.Companion.REPEATER) = ItemRedstoneRepeater::class.java //356
				list.get(ItemID.Companion.COOKIE) = ItemCookie::class.java //357
				list.get(ItemID.Companion.MAP) = ItemMap::class.java //358
				list.get(ItemID.Companion.SHEARS) = ItemShears::class.java //359
				list.get(ItemID.Companion.MELON) = ItemMelon::class.java //360
				list.get(ItemID.Companion.PUMPKIN_SEEDS) = ItemSeedsPumpkin::class.java //361
				list.get(ItemID.Companion.MELON_SEEDS) = ItemSeedsMelon::class.java //362
				list.get(ItemID.Companion.RAW_BEEF) = ItemBeefRaw::class.java //363
				list.get(ItemID.Companion.STEAK) = ItemSteak::class.java //364
				list.get(ItemID.Companion.RAW_CHICKEN) = ItemChickenRaw::class.java //365
				list.get(ItemID.Companion.COOKED_CHICKEN) = ItemChickenCooked::class.java //366
				list.get(ItemID.Companion.ROTTEN_FLESH) = ItemRottenFlesh::class.java //367
				list.get(ItemID.Companion.ENDER_PEARL) = ItemEnderPearl::class.java //368
				list.get(ItemID.Companion.BLAZE_ROD) = ItemBlazeRod::class.java //369
				list.get(ItemID.Companion.GHAST_TEAR) = ItemGhastTear::class.java //370
				list.get(ItemID.Companion.GOLD_NUGGET) = ItemNuggetGold::class.java //371
				list.get(ItemID.Companion.NETHER_WART) = ItemNetherWart::class.java //372
				list.get(ItemID.Companion.POTION) = ItemPotion::class.java //373
				list.get(ItemID.Companion.GLASS_BOTTLE) = ItemGlassBottle::class.java //374
				list.get(ItemID.Companion.SPIDER_EYE) = ItemSpiderEye::class.java //375
				list.get(ItemID.Companion.FERMENTED_SPIDER_EYE) = ItemSpiderEyeFermented::class.java //376
				list.get(ItemID.Companion.BLAZE_POWDER) = ItemBlazePowder::class.java //377
				list.get(ItemID.Companion.MAGMA_CREAM) = ItemMagmaCream::class.java //378
				list.get(ItemID.Companion.BREWING_STAND) = ItemBrewingStand::class.java //379
				list.get(ItemID.Companion.CAULDRON) = ItemCauldron::class.java //380
				list.get(ItemID.Companion.ENDER_EYE) = ItemEnderEye::class.java //381
				list.get(ItemID.Companion.GLISTERING_MELON) = ItemMelonGlistering::class.java //382
				list.get(ItemID.Companion.SPAWN_EGG) = ItemSpawnEgg::class.java //383
				list.get(ItemID.Companion.EXPERIENCE_BOTTLE) = ItemExpBottle::class.java //384
				list.get(ItemID.Companion.FIRE_CHARGE) = ItemFireCharge::class.java //385
				list.get(ItemID.Companion.BOOK_AND_QUILL) = ItemBookAndQuill::class.java //386
				list.get(ItemID.Companion.WRITTEN_BOOK) = ItemBookWritten::class.java //387
				list.get(ItemID.Companion.EMERALD) = ItemEmerald::class.java //388
				list.get(ItemID.Companion.ITEM_FRAME) = ItemItemFrame::class.java //389
				list.get(ItemID.Companion.FLOWER_POT) = ItemFlowerPot::class.java //390
				list.get(ItemID.Companion.CARROT) = ItemCarrot::class.java //391
				list.get(ItemID.Companion.POTATO) = ItemPotato::class.java //392
				list.get(ItemID.Companion.BAKED_POTATO) = ItemPotatoBaked::class.java //393
				list.get(ItemID.Companion.POISONOUS_POTATO) = ItemPotatoPoisonous::class.java //394
				//TODO: list[EMPTY_MAP] = ItemEmptyMap.class; //395
				list.get(ItemID.Companion.GOLDEN_CARROT) = ItemCarrotGolden::class.java //396
				list.get(ItemID.Companion.SKULL) = ItemSkull::class.java //397
				list.get(ItemID.Companion.CARROT_ON_A_STICK) = ItemCarrotOnAStick::class.java //398
				list.get(ItemID.Companion.NETHER_STAR) = ItemNetherStar::class.java //399
				list.get(ItemID.Companion.PUMPKIN_PIE) = ItemPumpkinPie::class.java //400
				list.get(ItemID.Companion.FIREWORKS) = ItemFirework::class.java //401
				list.get(ItemID.Companion.ENCHANTED_BOOK) = ItemBookEnchanted::class.java //403
				list.get(ItemID.Companion.COMPARATOR) = ItemRedstoneComparator::class.java //404
				list.get(ItemID.Companion.NETHER_BRICK) = ItemNetherBrick::class.java //405
				list.get(ItemID.Companion.QUARTZ) = ItemQuartz::class.java //406
				list.get(ItemID.Companion.MINECART_WITH_TNT) = ItemMinecartTNT::class.java //407
				list.get(ItemID.Companion.MINECART_WITH_HOPPER) = ItemMinecartHopper::class.java //408
				list.get(ItemID.Companion.PRISMARINE_SHARD) = ItemPrismarineShard::class.java //409
				list.get(ItemID.Companion.HOPPER) = ItemHopper::class.java
				list.get(ItemID.Companion.RAW_RABBIT) = ItemRabbitRaw::class.java //411
				list.get(ItemID.Companion.COOKED_RABBIT) = ItemRabbitCooked::class.java //412
				list.get(ItemID.Companion.RABBIT_STEW) = ItemRabbitStew::class.java //413
				list.get(ItemID.Companion.RABBIT_FOOT) = ItemRabbitFoot::class.java //414
				//TODO: list[RABBIT_HIDE] = ItemRabbitHide.class; //415
				list.get(ItemID.Companion.LEATHER_HORSE_ARMOR) = ItemHorseArmorLeather::class.java //416
				list.get(ItemID.Companion.IRON_HORSE_ARMOR) = ItemHorseArmorIron::class.java //417
				list.get(ItemID.Companion.GOLD_HORSE_ARMOR) = ItemHorseArmorGold::class.java //418
				list.get(ItemID.Companion.DIAMOND_HORSE_ARMOR) = ItemHorseArmorDiamond::class.java //419
				//TODO: list[LEAD] = ItemLead.class; //420
				//TODO: list[NAME_TAG] = ItemNameTag.class; //421
				list.get(ItemID.Companion.PRISMARINE_CRYSTALS) = ItemPrismarineCrystals::class.java //422
				list.get(ItemID.Companion.RAW_MUTTON) = ItemMuttonRaw::class.java //423
				list.get(ItemID.Companion.COOKED_MUTTON) = ItemMuttonCooked::class.java //424
				list.get(ItemID.Companion.END_CRYSTAL) = ItemEndCrystal::class.java //426
				list.get(ItemID.Companion.SPRUCE_DOOR) = ItemDoorSpruce::class.java //427
				list.get(ItemID.Companion.BIRCH_DOOR) = ItemDoorBirch::class.java //428
				list.get(ItemID.Companion.JUNGLE_DOOR) = ItemDoorJungle::class.java //429
				list.get(ItemID.Companion.ACACIA_DOOR) = ItemDoorAcacia::class.java //430
				list.get(ItemID.Companion.DARK_OAK_DOOR) = ItemDoorDarkOak::class.java //431
				list.get(ItemID.Companion.CHORUS_FRUIT) = ItemChorusFruit::class.java //432
				//TODO: list[POPPED_CHORUS_FRUIT] = ItemChorusFruitPopped.class; //433

				//TODO: list[DRAGON_BREATH] = ItemDragonBreath.class; //437
				list.get(ItemID.Companion.SPLASH_POTION) = ItemPotionSplash::class.java //438
				list.get(ItemID.Companion.LINGERING_POTION) = ItemPotionLingering::class.java //441
				list.get(ItemID.Companion.ELYTRA) = ItemElytra::class.java //444

				//TODO: list[SHULKER_SHELL] = ItemShulkerShell.class; //445
				list.get(ItemID.Companion.BANNER) = ItemBanner::class.java //446
				list.get(ItemID.Companion.TOTEM) = ItemTotem::class.java //450
				list.get(ItemID.Companion.TRIDENT) = ItemTrident::class.java //455
				list.get(ItemID.Companion.BEETROOT) = ItemBeetroot::class.java //457
				list.get(ItemID.Companion.BEETROOT_SEEDS) = ItemSeedsBeetroot::class.java //458
				list.get(ItemID.Companion.BEETROOT_SOUP) = ItemBeetrootSoup::class.java //459
				list.get(ItemID.Companion.RAW_SALMON) = ItemSalmon::class.java //460
				list.get(ItemID.Companion.CLOWNFISH) = ItemClownfish::class.java //461
				list.get(ItemID.Companion.PUFFERFISH) = ItemPufferfish::class.java //462
				list.get(ItemID.Companion.COOKED_SALMON) = ItemSalmonCooked::class.java //463
				list.get(ItemID.Companion.DRIED_KELP) = ItemDriedKelp::class.java //464
				list.get(ItemID.Companion.GOLDEN_APPLE_ENCHANTED) = ItemAppleGoldEnchanted::class.java //466
				list.get(ItemID.Companion.TURTLE_SHELL) = ItemTurtleShell::class.java //469
				list.get(ItemID.Companion.SWEET_BERRIES) = ItemSweetBerries::class.java //477
				list.get(ItemID.Companion.RECORD_11) = ItemRecord11::class.java
				list.get(ItemID.Companion.RECORD_CAT) = ItemRecordCat::class.java
				list.get(ItemID.Companion.RECORD_13) = ItemRecord13::class.java
				list.get(ItemID.Companion.RECORD_BLOCKS) = ItemRecordBlocks::class.java
				list.get(ItemID.Companion.RECORD_CHIRP) = ItemRecordChirp::class.java
				list.get(ItemID.Companion.RECORD_FAR) = ItemRecordFar::class.java
				list.get(ItemID.Companion.RECORD_WARD) = ItemRecordWard::class.java
				list.get(ItemID.Companion.RECORD_MALL) = ItemRecordMall::class.java
				list.get(ItemID.Companion.RECORD_MELLOHI) = ItemRecordMellohi::class.java
				list.get(ItemID.Companion.RECORD_STAL) = ItemRecordStal::class.java
				list.get(ItemID.Companion.RECORD_STRAD) = ItemRecordStrad::class.java
				list.get(ItemID.Companion.RECORD_WAIT) = ItemRecordWait::class.java
				list.get(ItemID.Companion.SHIELD) = ItemShield::class.java //513
				for (i in 0..255) {
					if (Block.list!![i] != null) {
						list.get(i) = Block.list!![i]
					}
				}
			}
			initCreativeItems()
		}

		private val creative = ArrayList<Item>()
		private fun initCreativeItems() {
			clearCreativeItems()
			val config = Config(Config.YAML)
			config.load(Server::class.java.classLoader.getResourceAsStream("creativeitems.json"))
			val list = config.getMapList("items")
			for (map in list) {
				try {
					addCreativeItem(fromJson(map))
				} catch (e: Exception) {
					MainLogger.getLogger().logException(e)
				}
			}
		}

		fun clearCreativeItems() {
			creative.clear()
		}

		val creativeItems: ArrayList<Item>
			get() = ArrayList(creative)

		fun addCreativeItem(item: Item) {
			creative.add(item.clone())
		}

		fun removeCreativeItem(item: Item) {
			val index = getCreativeItemIndex(item)
			if (index != -1) {
				creative.removeAt(index)
			}
		}

		fun isCreativeItem(item: Item): Boolean {
			for (aCreative in creative) {
				if (item.equals(aCreative, !item.isTool)) {
					return true
				}
			}
			return false
		}

		fun getCreativeItem(index: Int): Item? {
			return if (index >= 0 && index < creative.size) creative[index] else null
		}

		fun getCreativeItemIndex(item: Item): Int {
			for (i in creative.indices) {
				if (item.equals(creative[i], !item.isTool)) {
					return i
				}
			}
			return -1
		}

		@kotlin.jvm.JvmStatic
		@JvmOverloads
		operator fun get(id: Int, meta: Int = 0, count: Int = 1, tags: ByteArray = ByteArray(0)): Item {
			return try {
				val c = list!![id]
				val item: Item
				item = if (c == null) {
					Item(id, meta, count)
				} else if (id < 256) {
					if (meta >= 0) {
						ItemBlock(Block[id, meta], meta, count)
					} else {
						ItemBlock(Block[id], meta, count)
					}
				} else {
					c.getConstructor(Int::class.java, Int::class.javaPrimitiveType).newInstance(meta, count) as Item
				}
				if (tags.size != 0) {
					item.setCompoundTag(tags)
				}
				item
			} catch (e: Exception) {
				Item(id, meta, count).setCompoundTag(tags)
			}
		}

		@kotlin.jvm.JvmStatic
		fun fromString(str: String): Item {
			val b = str.trim { it <= ' ' }.replace(' ', '_').replace("minecraft:", "").split(":").toTypedArray()
			var id = 0
			var meta = 0
			val integerPattern = Pattern.compile("^[1-9]\\d*$")
			if (integerPattern.matcher(b[0]).matches()) {
				id = Integer.valueOf(b[0])
			} else {
				try {
					id = Item::class.java.getField(b[0].toUpperCase()).getInt(null)
				} catch (ignore: Exception) {
				}
			}
			id = id and 0xFFFF
			if (b.size != 1) meta = Integer.valueOf(b[1]) and 0xFFFF
			return get(id, meta)
		}

		fun fromJson(data: Map<String?, Any?>): Item {
			var nbt = data["nbt_b64"] as String?
			val nbtBytes: ByteArray
			if (nbt != null) {
				nbtBytes = Base64.getDecoder().decode(nbt)
			} else { // Support old format for backwards compat
				nbt = data.getOrDefault("nbt_hex", null) as String?
				nbtBytes = if (nbt == null) {
					ByteArray(0)
				} else {
					Utils.parseHexBinary(nbt)
				}
			}
			return get(Utils.toInt(data["id"]), Utils.toInt(data.getOrDefault("damage", 0)), Utils.toInt(data.getOrDefault("count", 1)), nbtBytes)
		}

		fun fromStringMultiple(str: String): Array<Item?> {
			val b = str.split(",").toTypedArray()
			val items = arrayOfNulls<Item>(b.size - 1)
			for (i in b.indices) {
				items[i] = fromString(b[i])
			}
			return items
		}

		fun parseCompoundTag(tag: ByteArray?): CompoundTag {
			return try {
				NBTIO.read(tag, ByteOrder.LITTLE_ENDIAN)
			} catch (e: IOException) {
				throw RuntimeException(e)
			}
		}
	}

	init {
		this.id = id and 0xffff
		if (meta != null && meta >= 0) {
			this.meta = meta and 0xffff
		} else {
			hasMeta = false
		}
		this.count = count
		this.name = name
		/*f (this.block != null && this.id <= 0xff && Block.list[id] != null) { //probably useless
            this.block = Block.get(this.id, this.meta);
            this.name = this.block.getName();
        }*/
	}
}