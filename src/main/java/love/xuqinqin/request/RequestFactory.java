package love.xuqinqin.request;

import love.xuqinqin.request.annotation.Get;
import love.xuqinqin.request.annotation.Post;
import love.xuqinqin.request.annotation.Request;
import love.xuqinqin.request.util.ParameterAndValue;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestFactory {

    public static final Map<Class<? extends Annotation>, BaseRequest> requestMapping = new HashMap<>();

    static {
        requestMapping.put(Get.class, new PostRequest());
        requestMapping.put(Post.class, new PostRequest());
    }

    public static BaseRequest createRequest(Method method, Object[] args) {
        Class<?> requestClass = method.getDeclaringClass();
        MergedAnnotations classAnnotation = MergedAnnotations.from(requestClass);
        if (!classAnnotation.isPresent(Request.class)) {
            throw new RuntimeException("目标类" + requestClass.getName() + "未标记@Request注解.");
        }
        MergedAnnotation<Request> requestMergedAnnotation = classAnnotation.get(Request.class);
        String url = requestMergedAnnotation.getString("url");
        MergedAnnotations methodAnnotation = MergedAnnotations.from(method);
        Parameter[] parameters = method.getParameters();
        BaseRequest request = resolveRequestMethod(method, methodAnnotation);
        request.setClassAnnotations(classAnnotation);
        request.setMethodAnnotations(methodAnnotation);
        request.setParameterAndValues(toParameterAndValues(parameters, args));
        request.setReturnType(method.getReturnType());
        request.setUri(url + request.getUri());
        return request;
    }

    public static BaseRequest resolveRequestMethod(Method method, MergedAnnotations methodAnnotation) {
        for (MergedAnnotation<Annotation> annotationMergedAnnotation : methodAnnotation) {
            Class<Annotation> type = annotationMergedAnnotation.getType();
            if (!requestMapping.containsKey(type)) continue;
            String urn = annotationMergedAnnotation.getString("urn");
            BaseRequest request = requestMapping.get(type);
            request.setUri(urn);
            return request;
        }
        throw new RuntimeException("目标方法" + method.getName() + "未标记任何请求方式注解.");
    }

    private static List<ParameterAndValue> toParameterAndValues(Parameter[] parameters, Object[] args) {
        List<ParameterAndValue> parameterAndValues = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            parameterAndValues.add(new ParameterAndValue(parameters[i], args[i]));
        }
        return parameterAndValues;
    }

}
