package love.xuqinqin.request.spring;

import love.xuqinqin.request.annotation.Request;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.StringUtils;

import java.util.Set;

public class RequestClassPathScanner extends ClassPathBeanDefinitionScanner {

    public RequestClassPathScanner(BeanDefinitionRegistry registry) {
        super(registry);
        this.addIncludeFilter(this::requestIncludeFilter);
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
        if (!beanDefinitionHolders.isEmpty()) process(beanDefinitionHolders);
        return beanDefinitionHolders;
    }

    private void process(Set<BeanDefinitionHolder> beanDefinitionHolders) {
        for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitionHolders) {
            AbstractBeanDefinition beanDefinition = (AbstractBeanDefinition) beanDefinitionHolder.getBeanDefinition();
            String beanClassName = beanDefinition.getBeanClassName();
            if (!StringUtils.hasText(beanClassName)) continue;
            try {
                beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(
                        0, Class.forName(beanClassName)
                );
            } catch (ClassNotFoundException e) {
                continue;
            }
            beanDefinition.setBeanClass(RequestFactoryBean.class);
        }
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        return metadata.isInterface() && metadata.isIndependent();
    }

    private boolean requestIncludeFilter(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) {
        AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
        return annotationMetadata.hasAnnotation(Request.class.getName());
    }
}
