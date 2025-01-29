package main.java.homework_1.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Эта аннотация применяется лишь с нестатическими методами.
 * Параметр приоритет лежит в диапазоне [1..10]. Дефолтное значение = 5.
 * Приоритет означает порядок выполнения тестов, где 10 - самый высокий, и метод
 * с таким приоритетом будет выполнен первым.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {

    int priority() default 5;

}
