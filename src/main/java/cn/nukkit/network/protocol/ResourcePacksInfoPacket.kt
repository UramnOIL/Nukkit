package cn.nukkit.network.protocol

import cn.nukkit.resourcepacks.ResourcePack
import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

@ToString
class ResourcePacksInfoPacket : DataPacket() {
	var mustAccept = false
	var unknownBool = false
	var behaviourPackEntries: Array<ResourcePack?>? = arrayOfNulls<ResourcePack?>(0)
	var resourcePackEntries: Array<ResourcePack?>? = arrayOfNulls<ResourcePack?>(0)

	@Override
	override fun decode() {
	}

	@Override
	override fun encode() {
		this.reset()
		this.putBoolean(mustAccept)
		this.putBoolean(unknownBool)
		encodePacks(resourcePackEntries)
		encodePacks(behaviourPackEntries)
	}

	private fun encodePacks(packs: Array<ResourcePack?>?) {
		this.putLShort(packs!!.size)
		for (entry in packs!!) {
			this.putString(entry.packId.toString())
			this.putString(entry.packVersion)
			this.putLLong(entry.packSize)
			this.putString("") // encryption key
			this.putString("") // sub-pack name
			this.putString("") // content identity
			this.putBoolean(false) // ???
		}
	}

	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.RESOURCE_PACKS_INFO_PACKET
	}
}