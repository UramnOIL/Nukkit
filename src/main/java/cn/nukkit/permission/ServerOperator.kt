package cn.nukkit.permission

/**
 * 能成为服务器管理员(OP)的对象。<br></br>
 * Who can be an operator(OP).
 *
 * @author MagicDroidX(code) @ Nukkit Project
 * @author 粉鞋大妈(javadoc) @ Nukkit Project
 * @see cn.nukkit.permission.Permissible
 *
 * @since Nukkit 1.0 | Nukkit API 1.0.0
 */
interface ServerOperator {
	var isOp: Boolean
}