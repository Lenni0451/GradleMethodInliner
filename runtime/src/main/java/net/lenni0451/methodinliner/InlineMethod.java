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

    /**
     * Keep the method after inlining.<br>
     * This will allow you to inline public methods as well.<br>
     * <b>If a non-static method is inlined and this is set to true, the method can only be inlined into the same class!</b><br>
     * <b>Also make sure no private fields/methods are accessed in the inlined method!</b>
     */
    boolean keep() default false;

}
