package love.xuqinqin.request.proxy;

import love.xuqinqin.request.BaseRequest;
import love.xuqinqin.request.RequestFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RequestInvocationHandler implements InvocationHandler {

    private final Map<Method, BaseRequest> requestCache = new HashMap<>();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) return method.invoke(this, args);
        return execRequestMethod(method, args);
    }

    public Object execRequestMethod(Method method, Object[] args) {
        if (requestCache.containsKey(method)) return requestCache.get(method).initAndSend();
        BaseRequest request = RequestFactory.createRequest(method, args);
        requestCache.put(method, request);
        return request.initAndSend();
    }
}
