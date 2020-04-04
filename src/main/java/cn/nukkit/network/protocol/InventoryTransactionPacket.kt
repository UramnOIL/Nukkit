package cn.nukkit.network.protocol

import cn.nukkit.inventory.transaction.data.ReleaseItemData
import cn.nukkit.inventory.transaction.data.TransactionData
import cn.nukkit.inventory.transaction.data.UseItemData
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData
import cn.nukkit.network.protocol.types.NetworkInventoryAction
import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

@ToString
class InventoryTransactionPacket : DataPacket() {
	var transactionType = 0
	var actions: Array<NetworkInventoryAction?>?
	var transactionData: TransactionData? = null

	/**
	 * NOTE: THIS FIELD DOES NOT EXIST IN THE PROTOCOL, it's merely used for convenience for PocketMine-MP to easily
	 * determine whether we're doing a crafting transaction.
	 */
	var isCraftingPart = false

	@Override
	override fun pid(): Byte {
		return ProtocolInfo.INVENTORY_TRANSACTION_PACKET
	}

	@Override
	override fun encode() {
		this.reset()
		this.putUnsignedVarInt(transactionType)
		this.putUnsignedVarInt(actions!!.size)
		for (action in actions!!) {
			action!!.write(this)
		}
		when (transactionType) {
			TYPE_NORMAL, TYPE_MISMATCH -> {
			}
			TYPE_USE_ITEM -> {
				val useItemData: UseItemData? = transactionData as UseItemData?
				this.putUnsignedVarInt(useItemData.actionType)
				this.putBlockVector3(useItemData.blockPos)
				this.putBlockFace(useItemData.face)
				this.putVarInt(useItemData.hotbarSlot)
				this.putSlot(useItemData.itemInHand)
				this.putVector3f(useItemData.playerPos.asVector3f())
				this.putVector3f(useItemData.clickPos)
				this.putUnsignedVarInt(useItemData.blockRuntimeId)
			}
			TYPE_USE_ITEM_ON_ENTITY -> {
				val useItemOnEntityData: UseItemOnEntityData? = transactionData as UseItemOnEntityData?
				this.putEntityRuntimeId(useItemOnEntityData.entityRuntimeId)
				this.putUnsignedVarInt(useItemOnEntityData.actionType)
				this.putVarInt(useItemOnEntityData.hotbarSlot)
				this.putSlot(useItemOnEntityData.itemInHand)
				this.putVector3f(useItemOnEntityData.playerPos.asVector3f())
				this.putVector3f(useItemOnEntityData.clickPos.asVector3f())
			}
			TYPE_RELEASE_ITEM -> {
				val releaseItemData: ReleaseItemData? = transactionData as ReleaseItemData?
				this.putUnsignedVarInt(releaseItemData.actionType)
				this.putVarInt(releaseItemData.hotbarSlot)
				this.putSlot(releaseItemData.itemInHand)
				this.putVector3f(releaseItemData.headRot.asVector3f())
			}
			else -> throw RuntimeException("Unknown transaction type " + transactionType)
		}
	}

	@Override
	override fun decode() {
		transactionType = this.getUnsignedVarInt() as Int
		actions = arrayOfNulls<NetworkInventoryAction?>(this.getUnsignedVarInt() as Int)
		for (i in actions.indices) {
			actions!![i] = NetworkInventoryAction().read(this)
		}
		when (transactionType) {
			TYPE_NORMAL, TYPE_MISMATCH -> {
			}
			TYPE_USE_ITEM -> {
				val itemData = UseItemData()
				itemData.actionType = this.getUnsignedVarInt() as Int
				itemData.blockPos = this.getBlockVector3()
				itemData.face = this.getBlockFace()
				itemData.hotbarSlot = this.getVarInt()
				itemData.itemInHand = this.getSlot()
				itemData.playerPos = this.getVector3f().asVector3()
				itemData.clickPos = this.getVector3f()
				itemData.blockRuntimeId = this.getUnsignedVarInt() as Int
				transactionData = itemData
			}
			TYPE_USE_ITEM_ON_ENTITY -> {
				val useItemOnEntityData = UseItemOnEntityData()
				useItemOnEntityData.entityRuntimeId = this.getEntityRuntimeId()
				useItemOnEntityData.actionType = this.getUnsignedVarInt() as Int
				useItemOnEntityData.hotbarSlot = this.getVarInt()
				useItemOnEntityData.itemInHand = this.getSlot()
				useItemOnEntityData.playerPos = this.getVector3f().asVector3()
				useItemOnEntityData.clickPos = this.getVector3f().asVector3()
				transactionData = useItemOnEntityData
			}
			TYPE_RELEASE_ITEM -> {
				val releaseItemData = ReleaseItemData()
				releaseItemData.actionType = getUnsignedVarInt() as Int
				releaseItemData.hotbarSlot = getVarInt()
				releaseItemData.itemInHand = getSlot()
				releaseItemData.headRot = this.getVector3f().asVector3()
				transactionData = releaseItemData
			}
			else -> throw RuntimeException("Unknown transaction type " + transactionType)
		}
	}

	companion object {
		const val TYPE_NORMAL = 0
		const val TYPE_MISMATCH = 1
		const val TYPE_USE_ITEM = 2
		const val TYPE_USE_ITEM_ON_ENTITY = 3
		const val TYPE_RELEASE_ITEM = 4
		const val USE_ITEM_ACTION_CLICK_BLOCK = 0
		const val USE_ITEM_ACTION_CLICK_AIR = 1
		const val USE_ITEM_ACTION_BREAK_BLOCK = 2
		const val RELEASE_ITEM_ACTION_RELEASE = 0 //bow shoot
		const val RELEASE_ITEM_ACTION_CONSUME = 1 //eat food, drink potion
		const val USE_ITEM_ON_ENTITY_ACTION_INTERACT = 0
		const val USE_ITEM_ON_ENTITY_ACTION_ATTACK = 1
		const val ACTION_MAGIC_SLOT_DROP_ITEM = 0
		const val ACTION_MAGIC_SLOT_PICKUP_ITEM = 1
		const val ACTION_MAGIC_SLOT_CREATIVE_DELETE_ITEM = 0
		const val ACTION_MAGIC_SLOT_CREATIVE_CREATE_ITEM = 1
	}
}