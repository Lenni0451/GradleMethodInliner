package net.lenni0451.methodinliner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a method to be inlined after compilation.<br>
 * The method <b>has</b> to be private.<br>
 * Both static and non-static methods are supported.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface InlineMethod {
}
