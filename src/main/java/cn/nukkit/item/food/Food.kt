package cn.nukkit.item.food

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.event.player.PlayerEatFoodEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemID
import cn.nukkit.plugin.Plugin
import cn.nukkit.potion.Effect
import java.util.*
import java.util.function.Consumer
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.set

/**
 * Created by Snake1999 on 2016/1/13.
 * Package cn.nukkit.item.food in project nukkit.
 */
abstract class Food {
	var restoreFood = 0
		protected set
	var restoreSaturation = 0f
		protected set
	protected val relativeIDs: MutableList<NodeIDMeta> = ArrayList()
	fun eatenBy(player: Player): Boolean {
		val event = PlayerEatFoodEvent(player, this)
		player.getServer().pluginManager.callEvent(event)
		return if (event.isCancelled) false else event.food.onEatenBy(player)
	}

	protected open fun onEatenBy(player: Player): Boolean {
		player.foodData!!.addFoodLevel(this)
		return true
	}

	@JvmOverloads
	fun addRelative(relativeID: Int, meta: Int = 0): Food {
		val node = NodeIDMeta(relativeID, meta)
		return addRelative(node)
	}

	private fun addRelative(node: NodeIDMeta): Food {
		if (!relativeIDs.contains(node)) relativeIDs.add(node)
		return this
	}

	fun setRestoreFood(restoreFood: Int): Food {
		this.restoreFood = restoreFood
		return this
	}

	fun setRestoreSaturation(restoreSaturation: Float): Food {
		this.restoreSaturation = restoreSaturation
		return this
	}

	open class NodeIDMeta(val id: Int, val meta: Int)

	internal class NodeIDMetaPlugin(id: Int, meta: Int, val plugin: Plugin) : NodeIDMeta(id, meta)

	companion object {
		private val registryCustom: MutableMap<NodeIDMetaPlugin, Food> = LinkedHashMap()
		private val registryDefault: MutableMap<NodeIDMeta, Food?> = LinkedHashMap()
		val apple = registerDefaultFood(FoodNormal(4, 2.4f).addRelative(ItemID.Companion.APPLE))
		val apple_golden = registerDefaultFood(FoodEffective(4, 9.6f)
				.addEffect(Effect.getEffect(Effect.REGENERATION).setAmplifier(1).setDuration(5 * 20))
				.addEffect(Effect.getEffect(Effect.ABSORPTION).setDuration(2 * 60 * 20))
				.addRelative(ItemID.Companion.GOLDEN_APPLE))
		val apple_golden_enchanted = registerDefaultFood(FoodEffective(4, 9.6f)
				.addEffect(Effect.getEffect(Effect.REGENERATION).setAmplifier(4).setDuration(30 * 20))
				.addEffect(Effect.getEffect(Effect.ABSORPTION).setDuration(2 * 60 * 20).setAmplifier(3))
				.addEffect(Effect.getEffect(Effect.DAMAGE_RESISTANCE).setDuration(5 * 60 * 20))
				.addEffect(Effect.getEffect(Effect.FIRE_RESISTANCE).setDuration(5 * 60 * 20))
				.addRelative(ItemID.Companion.GOLDEN_APPLE_ENCHANTED))
		val beef_raw = registerDefaultFood(FoodNormal(3, 1.8f).addRelative(ItemID.Companion.RAW_BEEF))
		val beetroot = registerDefaultFood(FoodNormal(1, 1.2f).addRelative(ItemID.Companion.BEETROOT))
		val beetroot_soup = registerDefaultFood(FoodInBowl(6, 7.2f).addRelative(ItemID.Companion.BEETROOT_SOUP))
		val bread = registerDefaultFood(FoodNormal(5, 6f).addRelative(ItemID.Companion.BREAD))
		val cake_slice = registerDefaultFood(FoodNormal(2, 0.4f)
				.addRelative(Block.CAKE_BLOCK, 0).addRelative(Block.CAKE_BLOCK, 1).addRelative(Block.CAKE_BLOCK, 2)
				.addRelative(Block.CAKE_BLOCK, 3).addRelative(Block.CAKE_BLOCK, 4).addRelative(Block.CAKE_BLOCK, 5)
				.addRelative(Block.CAKE_BLOCK, 6))
		val carrot = registerDefaultFood(FoodNormal(3, 4.8f).addRelative(ItemID.Companion.CARROT))
		val carrot_golden = registerDefaultFood(FoodNormal(6, 14.4f).addRelative(ItemID.Companion.GOLDEN_CARROT))
		val chicken_raw = registerDefaultFood(FoodEffective(2, 1.2f)
				.addChanceEffect(0.3f, Effect.getEffect(Effect.HUNGER).setDuration(30 * 20))
				.addRelative(ItemID.Companion.RAW_CHICKEN))
		val chicken_cooked = registerDefaultFood(FoodNormal(6, 7.2f).addRelative(ItemID.Companion.COOKED_CHICKEN))
		val chorus_fruit = registerDefaultFood(FoodChorusFruit())
		val cookie = registerDefaultFood(FoodNormal(2, 0.4f).addRelative(ItemID.Companion.COOKIE))
		val melon_slice = registerDefaultFood(FoodNormal(2, 1.2f).addRelative(ItemID.Companion.MELON_SLICE))
		val milk = registerDefaultFood(FoodMilk().addRelative(ItemID.Companion.BUCKET, 1))
		val mushroom_stew = registerDefaultFood(FoodInBowl(6, 7.2f).addRelative(ItemID.Companion.MUSHROOM_STEW))
		val mutton_cooked = registerDefaultFood(FoodNormal(6, 9.6f).addRelative(ItemID.Companion.COOKED_MUTTON))
		val mutton_raw = registerDefaultFood(FoodNormal(2, 1.2f).addRelative(ItemID.Companion.RAW_MUTTON))
		val porkchop_cooked = registerDefaultFood(FoodNormal(8, 12.8f).addRelative(ItemID.Companion.COOKED_PORKCHOP))
		val porkchop_raw = registerDefaultFood(FoodNormal(3, 1.8f).addRelative(ItemID.Companion.RAW_PORKCHOP))
		val potato_raw = registerDefaultFood(FoodNormal(1, 0.6f).addRelative(ItemID.Companion.POTATO))
		val potato_baked = registerDefaultFood(FoodNormal(5, 7.2f).addRelative(ItemID.Companion.BAKED_POTATO))
		val potato_poisonous = registerDefaultFood(FoodEffective(2, 1.2f)
				.addChanceEffect(0.6f, Effect.getEffect(Effect.POISON).setDuration(4 * 20))
				.addRelative(ItemID.Companion.POISONOUS_POTATO))
		val pumpkin_pie = registerDefaultFood(FoodNormal(8, 4.8f).addRelative(ItemID.Companion.PUMPKIN_PIE))
		val rabbit_cooked = registerDefaultFood(FoodNormal(5, 6f).addRelative(ItemID.Companion.COOKED_RABBIT))
		val rabbit_raw = registerDefaultFood(FoodNormal(3, 1.8f).addRelative(ItemID.Companion.RAW_RABBIT))
		val rabbit_stew = registerDefaultFood(FoodInBowl(10, 12f).addRelative(ItemID.Companion.RABBIT_STEW))
		val rotten_flesh = registerDefaultFood(FoodEffective(4, 0.8f)
				.addChanceEffect(0.8f, Effect.getEffect(Effect.HUNGER).setDuration(30 * 20))
				.addRelative(ItemID.Companion.ROTTEN_FLESH))
		val spider_eye = registerDefaultFood(FoodEffective(2, 3.2f)
				.addEffect(Effect.getEffect(Effect.POISON).setDuration(4 * 20))
				.addRelative(ItemID.Companion.SPIDER_EYE))
		val steak = registerDefaultFood(FoodNormal(8, 12.8f).addRelative(ItemID.Companion.COOKED_BEEF))

		//different kinds of fishes
		val clownfish = registerDefaultFood(FoodNormal(1, 0.2f).addRelative(ItemID.Companion.CLOWNFISH))
		val fish_cooked = registerDefaultFood(FoodNormal(5, 6f).addRelative(ItemID.Companion.COOKED_FISH))
		val fish_raw = registerDefaultFood(FoodNormal(2, 0.4f).addRelative(ItemID.Companion.RAW_FISH))
		val salmon_cooked = registerDefaultFood(FoodNormal(6, 9.6f).addRelative(ItemID.Companion.COOKED_SALMON))
		val salmon_raw = registerDefaultFood(FoodNormal(2, 0.4f).addRelative(ItemID.Companion.RAW_SALMON))
		val pufferfish = registerDefaultFood(FoodEffective(1, 0.2f)
				.addEffect(Effect.getEffect(Effect.HUNGER).setAmplifier(2).setDuration(15 * 20))
				.addEffect(Effect.getEffect(Effect.NAUSEA).setAmplifier(1).setDuration(15 * 20))
				.addEffect(Effect.getEffect(Effect.POISON).setAmplifier(4).setDuration(60 * 20))
				.addRelative(ItemID.Companion.PUFFERFISH))
		val dried_kelp = registerDefaultFood(FoodNormal(1, 0.6f).addRelative(ItemID.Companion.DRIED_KELP))
		val sweet_berries = registerDefaultFood(FoodNormal(2, 0.4f).addRelative(ItemID.Companion.SWEET_BERRIES))

		//Opened API for plugins
		fun registerFood(food: Food, plugin: Plugin): Food {
			Objects.requireNonNull(food)
			Objects.requireNonNull(plugin)
			food.relativeIDs.forEach(Consumer { n: NodeIDMeta -> registryCustom[NodeIDMetaPlugin(n.id, n.meta, plugin)] = food })
			return food
		}

		private fun registerDefaultFood(food: Food?): Food? {
			food!!.relativeIDs.forEach(Consumer { n: NodeIDMeta -> registryDefault[n] = food })
			return food
		}

		fun getByRelative(item: Item): Food? {
			Objects.requireNonNull(item)
			return getByRelative(item.id, item.damage)
		}

		fun getByRelative(block: Block): Food? {
			Objects.requireNonNull(block)
			return getByRelative(block.id, block.damage)
		}

		fun getByRelative(relativeID: Int, meta: Int): Food? {
			val result = arrayOf<Food?>(null)
			registryCustom.forEach { (n: NodeIDMetaPlugin, f: Food?) -> if (n.id == relativeID && n.meta == meta && n.plugin.isEnabled) result[0] = f }
			if (result[0] == null) {
				registryDefault.forEach { (n: NodeIDMeta, f: Food?) -> if (n.id == relativeID && n.meta == meta) result[0] = f }
			}
			return result[0]
		}
	}
}