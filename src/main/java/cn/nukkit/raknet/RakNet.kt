package cn.nukkit.raknet

/**
 * author: MagicDroidX
 * Nukkit Project
 * UDP network library that follows the RakNet protocol for Nukkit Project
 * This is not affiliated with Jenkins Software LLC nor RakNet.
 */
object RakNet {
	const val VERSION = "1.1.0"
	const val PROTOCOL: Byte = 9
	val MAGIC = byteArrayOf(
			0x00.toByte(), 0xff.toByte(), 0xff.toByte(), 0x00.toByte(),
			0xfe.toByte(), 0xfe.toByte(), 0xfe.toByte(), 0xfe.toByte(),
			0xfd.toByte(), 0xfd.toByte(), 0xfd.toByte(), 0xfd.toByte(),
			0x12.toByte(), 0x34.toByte(), 0x56.toByte(), 0x78.toByte()
	)
	const val PRIORITY_NORMAL: Byte = 0
	const val PRIORITY_IMMEDIATE: Byte = 1
	const val FLAG_NEED_ACK: Byte = 8

	/*
     * ENCAPSULATED payload:
     * byte (identifier length)
     * byte[] (identifier)
     * byte (flags, last 3 bits, priority)
     * payload (binary internal EncapsulatedPacket)
     */
	const val PACKET_ENCAPSULATED: Byte = 0x01

	/*
     * OPEN_SESSION payload:
     * byte (identifier length)
     * byte[] (identifier)
     * byte (address length)
     * byte[] (address)
     * short (port)
     * long (clientID)
     */
	const val PACKET_OPEN_SESSION: Byte = 0x02

	/*
     * CLOSE_SESSION payload:
     * byte (identifier length)
     * byte[] (identifier)
     * string (reason)
     */
	const val PACKET_CLOSE_SESSION: Byte = 0x03

	/*
     * INVALID_SESSION payload:
     * byte (identifier length)
     * byte[] (identifier)
     */
	const val PACKET_INVALID_SESSION: Byte = 0x04

	/* SEND_QUEUE payload:
     * byte (identifier length)
     * byte[] (identifier)
     */
	const val PACKET_SEND_QUEUE: Byte = 0x05

	/*
     * ACK_NOTIFICATION payload:
     * byte (identifier length)
     * byte[] (identifier)
     * int (identifierACK)
     */
	const val PACKET_ACK_NOTIFICATION: Byte = 0x06

	/*
     * SET_OPTION payload:
     * byte (option name length)
     * byte[] (option name)
     * byte[] (option value)
     */
	const val PACKET_SET_OPTION: Byte = 0x07

	/*
     * RAW payload:
     * byte (address length)
     * byte[] (address from/to)
     * short (port)
     * byte[] (payload)
     */
	const val PACKET_RAW: Byte = 0x08

	/*
     * BLOCK_ADDRESS payload:
     * byte (address length)
     * byte[] (address)
     * int (timeout)
     */
	const val PACKET_BLOCK_ADDRESS: Byte = 0x09

	/*
     * UNBLOCK_ADDRESS payload:
     * byte (adress length)
     * byte[] (address)
     */
	const val PACKET_UNBLOCK_ADDRESS: Byte = 0x10

	/*
     * No payload
     *
     * Sends the disconnect message, removes sessions correctly, closes sockets.
     */
	const val PACKET_SHUTDOWN: Byte = 0x7e

	/*
     * No payload
     *
     * Leaves everything as-is and halts, other Threads can be in a post-crash condition.
     */
	const val PACKET_EMERGENCY_SHUTDOWN: Byte = 0x7f
}