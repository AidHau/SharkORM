package com.revature.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(FIELD) 
@Retention(RUNTIME)
public @interface Column {
	
	String name() default "";
	
	boolean unique() default false;
	
	
	
}
