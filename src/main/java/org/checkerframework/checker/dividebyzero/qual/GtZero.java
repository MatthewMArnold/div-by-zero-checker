package org.checkerframework.checker.dividebyzero.qual;

import org.checkerframework.framework.qual.SubtypeOf;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@SubtypeOf({NeZero.class, GeZero.class})
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface GtZero { }
