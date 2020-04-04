package cn.nukkit.command.simple

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * @author Tee7even
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(RetentionPolicy.RUNTIME)
annotation class ForbidConsole