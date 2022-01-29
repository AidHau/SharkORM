package com.revature.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

//Needs changes

@Retention(RUNTIME)
@Target(TYPE)
public @interface Entity {

	String name() default "";
	
}