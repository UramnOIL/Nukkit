package cn.nukkit.raknet.server

import cn.nukkit.utils.ThreadedLogger
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelOption
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollDatagramChannel
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.DatagramPacket
import io.netty.channel.socket.nio.NioDatagramChannel
import java.io.IOException
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class UDPServerSocket @JvmOverloads constructor(protected val logger: ThreadedLogger, port: Int = 19132, interfaz: String = "0.0.0.0") : ChannelInboundHandlerAdapter() {
	protected var bootstrap: Bootstrap? = null
	protected var channel: Channel? = null
	protected var packets = ConcurrentLinkedQueue<DatagramPacket>()
	fun close() {
		bootstrap!!.config().group().shutdownGracefully()
		if (channel != null) {
			channel!!.close().syncUninterruptibly()
		}
	}

	fun clearPacketQueue() {
		packets.clear()
	}

	@Throws(IOException::class)
	fun readPacket(): DatagramPacket {
		return packets.poll()
	}

	@Throws(IOException::class)
	fun writePacket(data: ByteArray?, dest: String?, port: Int): Int {
		return this.writePacket(data, InetSocketAddress(dest, port))
	}

	@Throws(IOException::class)
	fun writePacket(data: ByteArray?, dest: InetSocketAddress?): Int {
		channel!!.writeAndFlush(DatagramPacket(Unpooled.wrappedBuffer(data), dest))
		return data!!.size
	}

	@Throws(Exception::class)
	override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
		packets.add(msg as DatagramPacket)
	}

	override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
		logger.warning(cause.message, cause)
	}

	companion object {
		val EPOLL = Epoll.isAvailable()
	}

	init {
		try {
			bootstrap = Bootstrap()
					.channel(if (EPOLL) EpollDatagramChannel::class.java else NioDatagramChannel::class.java)
					.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
					.handler(this)
					.group(if (EPOLL) EpollEventLoopGroup() else NioEventLoopGroup())
			logger.info("Epoll Status is $EPOLL")
			channel = bootstrap.bind(interfaz, port).sync().channel()
		} catch (e: Exception) {
			logger.critical("**** FAILED TO BIND TO $interfaz:$port!")
			logger.critical("Perhaps a server is already running on that port?")
			System.exit(1)
		}
	}
}