package com.hmw.account.aop;

import java.lang.annotation.*;

/**
 * Documented Test
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface AccountLock {
    long tryLockTime() default 5000L;
}