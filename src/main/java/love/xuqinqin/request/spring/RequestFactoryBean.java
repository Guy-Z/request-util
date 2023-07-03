package love.xuqinqin.request.spring;

import love.xuqinqin.request.proxy.RequestInvocationHandler;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

public class RequestFactoryBean<T> implements FactoryBean<T> {

    private final Class<T> clazz;

    public RequestFactoryBean(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() {
        return (T) Proxy.newProxyInstance(
                this.getClass().getClassLoader(), new Class[]{this.clazz}, new RequestInvocationHandler()
        );
    }

    @Override
    public Class<?> getObjectType() {
        return clazz;
    }
}
