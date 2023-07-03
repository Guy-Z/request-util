package love.xuqinqin.request.spring;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(RequestImportBeanDefinitionRegistrar.class)
public @interface RequestPackageScan {

    @AliasFor("packageNames")
    String[] value() default {};

    @AliasFor("value")
    String[] packageNames() default {};

}
