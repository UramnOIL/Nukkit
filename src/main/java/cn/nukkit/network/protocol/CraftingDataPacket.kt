package cn.nukkit.network.protocol

import cn.nukkit.inventory.*
import cn.nukkit.item.Item
import lombok.ToString
import java.util.ArrayList
import java.util.Collections
import java.util.List
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * @author Nukkit Project Team
 */
@ToString
class CraftingDataPacket : DataPacket() {
	private var entries: List<Recipe?>? = ArrayList()
	private val brewingEntries: List<BrewingRecipe?>? = ArrayList()
	private val containerEntries: List<ContainerRecipe?>? = ArrayList()
	var cleanRecipes = false
	fun addShapelessRecipe(vararg recipe: ShapelessRecipe?) {
		Collections.addAll(entries, recipe)
	}

	fun addShapedRecipe(vararg recipe: ShapedRecipe?) {
		Collections.addAll(entries, recipe)
	}

	fun addFurnaceRecipe(vararg recipe: FurnaceRecipe?) {
		Collections.addAll(entries, recipe)
	}

	fun addBrewingRecipe(vararg recipe: BrewingRecipe?) {
		Collections.addAll(brewingEntries, recipe)
	}

	fun addContainerRecipe(vararg recipe: ContainerRecipe?) {
		Collections.addAll(containerEntries, recipe)
	}

	@Override
	override fun clean(): DataPacket? {
		entries = ArrayList()
		return super.clean()
	}

	@Override
	override fun decode() {
	}

	@Override
	override fun encode() {
		this.reset()
		this.putUnsignedVarInt(entries!!.size())
		for (recipe in entries!!) {
			this.putVarInt(recipe.getType().ordinal())
			when (recipe.getType()) {
				SHAPELESS -> {
					val shapeless: ShapelessRecipe? = recipe as ShapelessRecipe?
					this.putString(shapeless.getRecipeId())
					val ingredients: List<Item?> = shapeless.getIngredientList()
					this.putUnsignedVarInt(ingredients.size())
					for (ingredient in ingredients) {
						this.putRecipeIngredient(ingredient)
					}
					this.putUnsignedVarInt(1)
					this.putSlot(shapeless.getResult())
					this.putUUID(shapeless.getId())
					this.putString(CRAFTING_TAG_CRAFTING_TABLE)
					this.putVarInt(shapeless.getPriority())
				}
				SHAPED -> {
					val shaped: ShapedRecipe? = recipe as ShapedRecipe?
					this.putString(shaped.getRecipeId())
					this.putVarInt(shaped.getWidth())
					this.putVarInt(shaped.getHeight())
					var z = 0
					while (z < shaped.getHeight()) {
						var x = 0
						while (x < shaped.getWidth()) {
							this.putRecipeIngredient(shaped.getIngredient(x, z))
							++x
						}
						++z
					}
					val outputs: List<Item?> = ArrayList()
					outputs.add(shaped.getResult())
					outputs.addAll(shaped.getExtraResults())
					this.putUnsignedVarInt(outputs.size())
					for (output in outputs) {
						this.putSlot(output)
					}
					this.putUUID(shaped.getId())
					this.putString(CRAFTING_TAG_CRAFTING_TABLE)
					this.putVarInt(shaped.getPriority())
				}
				FURNACE, FURNACE_DATA -> {
					val furnace: FurnaceRecipe? = recipe as FurnaceRecipe?
					val input: Item = furnace.getInput()
					this.putVarInt(input.getId())
					if (recipe.getType() === RecipeType.FURNACE_DATA) {
						this.putVarInt(input.getDamage())
					}
					this.putSlot(furnace.getResult())
					this.putString(CRAFTING_TAG_FURNACE)
				}
			}
		}
		this.putUnsignedVarInt(brewingEntries!!.size())
		for (recipe in brewingEntries!!) {
			this.putVarInt(recipe.getInput().getDamage())
			this.putVarInt(recipe.getIngredient().getId())
			this.putVarInt(recipe.getResult().getDamage())
		}
		this.putUnsignedVarInt(containerEntries!!.size())
		for (recipe in containerEntries!!) {
			this.putVarInt(recipe.getInput().getId())
			this.putVarInt(recipe.getIngredient().getId())
			this.putVarInt(recipe.getResult().getId())
		}
		this.putBoolean(cleanRecipes)
	}

	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.CRAFTING_DATA_PACKET
		val CRAFTING_TAG_CRAFTING_TABLE: String? = "crafting_table"
		val CRAFTING_TAG_CARTOGRAPHY_TABLE: String? = "cartography_table"
		val CRAFTING_TAG_STONECUTTER: String? = "stonecutter"
		val CRAFTING_TAG_FURNACE: String? = "furnace"
		val CRAFTING_TAG_CAMPFIRE: String? = "campfire"
		val CRAFTING_TAG_BLAST_FURNACE: String? = "blast_furnace"
		val CRAFTING_TAG_SMOKER: String? = "smoker"
	}
}