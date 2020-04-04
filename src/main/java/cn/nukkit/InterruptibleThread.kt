package cn.nukkit

/**
 * 描述一个可以被中断的线程的接口。<br></br>
 * An interface to describe a thread that can be interrupted.
 *
 *
 * 在Nukkit服务器停止时，Nukkit会找到所有实现了`InterruptibleThread`的线程，并逐一中断。<br></br>
 * When a Nukkit server is stopping, Nukkit finds all threads implements `InterruptibleThread`,
 * and interrupt them one by one.
 *
 * @author MagicDroidX(code) @ Nukkit Project
 * @author 粉鞋大妈(javadoc) @ Nukkit Project
 * @see cn.nukkit.scheduler.AsyncWorker
 *
 * @since Nukkit 1.0 | Nukkit API 1.0.0
 */
interface InterruptibleThread