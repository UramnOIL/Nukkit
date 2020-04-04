package cn.nukkit.network.protocol

import cn.nukkit.resourcepacks.ResourcePack
import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

@ToString
class ResourcePackStackPacket : DataPacket() {
	var mustAccept = false
	var behaviourPackStack: Array<ResourcePack?>? = arrayOfNulls<ResourcePack?>(0)
	var resourcePackStack: Array<ResourcePack?>? = arrayOfNulls<ResourcePack?>(0)
	var isExperimental = false
	var gameVersion: String? = ProtocolInfo.MINECRAFT_VERSION_NETWORK

	@Override
	override fun decode() {
	}

	@Override
	override fun encode() {
		this.reset()
		this.putBoolean(mustAccept)
		this.putUnsignedVarInt(behaviourPackStack!!.size)
		for (entry in behaviourPackStack!!) {
			this.putString(entry.packId.toString())
			this.putString(entry.packVersion)
			this.putString("") //TODO: subpack name
		}
		this.putUnsignedVarInt(resourcePackStack!!.size)
		for (entry in resourcePackStack!!) {
			this.putString(entry.packId.toString())
			this.putString(entry.packVersion)
			this.putString("") //TODO: subpack name
		}
		this.putBoolean(isExperimental)
		this.putString(gameVersion)
	}

	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.RESOURCE_PACK_STACK_PACKET
	}
}