package love.xuqinqin.request.spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

public class RequestImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(RequestPackageScan.class.getName())
        );
        if (annotationAttributes == null || annotationAttributes.isEmpty()) return;
        RequestClassPathScanner requestClassPathScanner = new RequestClassPathScanner(registry);
        requestClassPathScanner.scan(annotationAttributes.getStringArray("packageNames"));
    }
}
