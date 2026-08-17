package com.qualcomm.robotcore.eventloop.opmode;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Compile-only stub matching FTC {@code TeleOp}. */
@Retention(RetentionPolicy.RUNTIME)
public @interface TeleOp {
    String name() default "";

    String group() default "";
}
