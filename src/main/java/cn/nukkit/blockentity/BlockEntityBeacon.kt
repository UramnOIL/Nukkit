package cn.nukkit.blockentity

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.block.BlockID
import cn.nukkit.inventory.BeaconInventory
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.LevelSoundEventPacket
import cn.nukkit.potion.Effect

/**
 * author: Rover656
 */
class BlockEntityBeacon(chunk: FullChunk?, nbt: CompoundTag) : BlockEntitySpawnable(chunk, nbt) {
	override fun initBlockEntity() {
		if (!namedTag.contains("Lock")) {
			namedTag.putString("Lock", "")
		}
		if (!namedTag.contains("Levels")) {
			namedTag.putInt("Levels", 0)
		}
		if (!namedTag.contains("Primary")) {
			namedTag.putInt("Primary", 0)
		}
		if (!namedTag.contains("Secondary")) {
			namedTag.putInt("Secondary", 0)
		}
		scheduleUpdate()
		super.initBlockEntity()
	}

	override val isBlockEntityValid: Boolean
		get() {
			val blockID = block.id
			return blockID == Block.BEACON
		}

	override val spawnCompound: CompoundTag?
		get() = CompoundTag()
				.putString("id", BlockEntity.Companion.BEACON)
				.putInt("x", x.toInt())
				.putInt("y", y.toInt())
				.putInt("z", z.toInt())
				.putString("Lock", namedTag.getString("Lock"))
				.putInt("Levels", namedTag.getInt("Levels"))
				.putInt("Primary", namedTag.getInt("Primary"))
				.putInt("Secondary", namedTag.getInt("Secondary"))

	private var currentTick: Long = 0
	override fun onUpdate(): Boolean {
		//Only apply effects every 4 secs
		if (currentTick++ % 80 != 0L) {
			return true
		}
		val oldPowerLevel = powerLevel
		//Get the power level based on the pyramid
		powerLevel = calculatePowerLevel()
		val newPowerLevel = powerLevel

		//Skip beacons that do not have a pyramid or sky access
		if (newPowerLevel < 1 || !hasSkyAccess()) {
			if (oldPowerLevel > 0) {
				getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_BEACON_DEACTIVATE)
			}
			return true
		} else if (oldPowerLevel < 1) {
			getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_BEACON_ACTIVATE)
		} else {
			getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_BEACON_AMBIENT)
		}

		//Get all players in game
		val players = level.players

		//Calculate vars for beacon power
		val range = 10 + powerLevel * 10
		val duration = 9 + powerLevel * 2
		for ((_, p) in players) {

			//If the player is in range
			if (p.distance(this) < range) {
				var e: Effect
				if (primaryPower != 0) {
					//Apply the primary power
					e = Effect.getEffect(primaryPower)

					//Set duration
					e.duration = duration * 20

					//If secondary is selected as the primary too, apply 2 amplification
					if (secondaryPower == primaryPower) {
						e.amplifier = 2
					} else {
						e.amplifier = 1
					}

					//Hide particles
					e.isVisible = false

					//Add the effect
					p.addEffect(e)
				}

				//If we have a secondary power as regen, apply it
				if (secondaryPower == Effect.REGENERATION) {
					//Get the regen effect
					e = Effect.getEffect(Effect.REGENERATION)

					//Set duration
					e.duration = duration * 20

					//Regen I
					e.amplifier = 1

					//Hide particles
					e.isVisible = false

					//Add effect
					p.addEffect(e)
				}
			}
		}
		return true
	}

	private fun hasSkyAccess(): Boolean {
		val tileX = floorX
		val tileY = floorY
		val tileZ = floorZ

		//Check every block from our y coord to the top of the world
		for (y in tileY + 1..255) {
			val testBlockId = level.getBlockIdAt(tileX, y, tileZ)
			if (!Block.transparent!![testBlockId]) {
				//There is no sky access
				return false
			}
		}
		return true
	}

	private fun calculatePowerLevel(): Int {
		val tileX = floorX
		val tileY = floorY
		val tileZ = floorZ

		//The power level that we're testing for
		for (powerLevel in 1..POWER_LEVEL_MAX) {
			val queryY = tileY - powerLevel //Layer below the beacon block
			for (queryX in tileX - powerLevel..tileX + powerLevel) {
				for (queryZ in tileZ - powerLevel..tileZ + powerLevel) {
					val testBlockId = level.getBlockIdAt(queryX, queryY, queryZ)
					if (testBlockId != Block.IRON_BLOCK && testBlockId != Block.GOLD_BLOCK && testBlockId != Block.EMERALD_BLOCK && testBlockId != Block.DIAMOND_BLOCK) {
						return powerLevel - 1
					}
				}
			}
		}
		return POWER_LEVEL_MAX
	}

	var powerLevel: Int
		get() = namedTag.getInt("Level")
		set(level) {
			val currentLevel = powerLevel
			if (level != currentLevel) {
				namedTag.putInt("Level", level)
				setDirty()
				spawnToAll()
			}
		}

	var primaryPower: Int
		get() = namedTag.getInt("Primary")
		set(power) {
			val currentPower = primaryPower
			if (power != currentPower) {
				namedTag.putInt("Primary", power)
				setDirty()
				spawnToAll()
			}
		}

	var secondaryPower: Int
		get() = namedTag.getInt("Secondary")
		set(power) {
			val currentPower = secondaryPower
			if (power != currentPower) {
				namedTag.putInt("Secondary", power)
				setDirty()
				spawnToAll()
			}
		}

	override fun updateCompoundTag(nbt: CompoundTag, player: Player): Boolean {
		if (nbt.getString("id") != BlockEntity.Companion.BEACON) {
			return false
		}
		primaryPower = nbt.getInt("primary")
		secondaryPower = nbt.getInt("secondary")
		getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_BEACON_POWER)
		val inv = player.getWindowById(Player.BEACON_WINDOW_ID) as BeaconInventory?
		inv!!.setItem(0, ItemBlock(get(BlockID.AIR), 0, 0))
		return true
	}

	companion object {
		private const val POWER_LEVEL_MAX = 4
	}
}