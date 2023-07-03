package love.xuqinqin.request.annotation;

import love.xuqinqin.request.ResolveResult;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Result {

    Class<? extends ResolveResult<?>> value();

}
