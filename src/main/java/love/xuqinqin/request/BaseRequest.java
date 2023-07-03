package love.xuqinqin.request;

import love.xuqinqin.request.annotation.Header;
import love.xuqinqin.request.annotation.Headers;
import love.xuqinqin.request.annotation.Result;
import love.xuqinqin.request.annotation.param.PathParam;
import love.xuqinqin.request.annotation.param.QueryParam;
import love.xuqinqin.request.util.ParameterAndValue;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseRequest {

    protected final Map<String, Object> headers = new HashMap<>();

    protected final Map<String, Object> pathParams = new HashMap<>();

    protected final Map<String, Object> queryParams = new HashMap<>();

    protected MergedAnnotations classAnnotations;

    protected MergedAnnotations methodAnnotations;

    protected List<ParameterAndValue> parameterAndValues;

    private Class<?> returnType;

    private HttpRequestBase httpRequestBase;

    private String uri;

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public Map<String, Object> getPathParams() {
        return pathParams;
    }

    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    public MergedAnnotations getClassAnnotations() {
        return classAnnotations;
    }

    public void setClassAnnotations(MergedAnnotations classAnnotations) {
        this.classAnnotations = classAnnotations;
    }

    public MergedAnnotations getMethodAnnotations() {
        return methodAnnotations;
    }

    public void setMethodAnnotations(MergedAnnotations methodAnnotations) {
        this.methodAnnotations = methodAnnotations;
    }

    public List<ParameterAndValue> getParameterAndValues() {
        return parameterAndValues;
    }

    public void setParameterAndValues(List<ParameterAndValue> parameterAndValues) {
        this.parameterAndValues = parameterAndValues;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    public HttpRequestBase getHttpRequestBase() {
        return httpRequestBase;
    }

    public void setHttpRequestBase(HttpRequestBase httpRequestBase) {
        this.httpRequestBase = httpRequestBase;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void init() {
        initHeadersOnMethod();
        for (ParameterAndValue parameterAndValue : getParameterAndValues()) {
            Parameter parameter = parameterAndValue.getParameter();
            MergedAnnotations methodAnnotations = MergedAnnotations.from(parameter);
            resolveParameterAndValue(this.pathParams, parameterAndValue, methodAnnotations, PathParam.class);
            resolveParameterAndValue(this.queryParams, parameterAndValue, methodAnnotations, QueryParam.class);
            resolveParameterAndValue(this.headers, parameterAndValue, methodAnnotations, Header.class);
        }
        this.uri = pathParamsAndQueryParamsUri(this.uri, pathParams, queryParams);
    }

    public void initHeadersOnMethod() {
        if (!methodAnnotations.isPresent(Headers.class)) return;
        MergedAnnotation<Headers> headerMergedAnnotation = methodAnnotations.get(Headers.class);
        MergedAnnotation<Header>[] annotationArray = headerMergedAnnotation.getAnnotationArray(
                "value", Header.class
        );
        if (annotationArray.length == 0) return;
        Map<String, Object> basicHeaders = Arrays.stream(annotationArray).filter(m ->
                StringUtils.hasText(m.getString("name"))
        ).collect(Collectors.toMap(
                m -> m.getString("name"), m -> m.getString("headerValue")
        ));
        this.headers.putAll(basicHeaders);
    }

    public Object initAndSend() {
        init();
        return sendHttpRequest();
    }


    public Object sendHttpRequest() {
        if (httpRequestBase == null) throw new RuntimeException("httpUriRequest为空");
        setHeader();
        try (
                CloseableHttpClient client = HttpClientBuilder.create().build();
                CloseableHttpResponse response = client.execute(httpRequestBase)
        ) {
            return resolveResult(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends Annotation> void resolveParameterAndValue(
            Map<String, Object> params, ParameterAndValue paramAndValue, MergedAnnotations annotations, Class<T> clazz
    ) {
        if (!annotations.isPresent(clazz)) return;
        MergedAnnotation<T> queryParamMergedAnnotation = annotations.get(clazz);
        String name = queryParamMergedAnnotation.getString("name");
        if (Map.class.isAssignableFrom(paramAndValue.getParameter().getType())) {
            params.putAll((Map<String, ?>) paramAndValue.getValue());
            return;
        }
        if (!StringUtils.hasText(name)) return;
        params.put(name, paramAndValue.getValue());
    }

    protected String pathParamsUri(String uri, Map<String, Object> pathParams) {
        if (pathParams.isEmpty()) return uri;
        for (Map.Entry<String, Object> pathParam : pathParams.entrySet()) {
            uri = uri.replace("{" + pathParam.getKey() + "}", pathParam.getValue().toString());
        }
        return uri;
    }

    protected String queryParamsUri(String uri, Map<String, Object> queryParams) {
        String queryParamsStr = queryParams.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        return StringUtils.hasText(queryParamsStr) ? uri + "?" + queryParamsStr : uri;
    }

    protected String pathParamsAndQueryParamsUri(
            String uri, Map<String, Object> pathParams, Map<String, Object> queryParams
    ) {
        return queryParamsUri(pathParamsUri(uri, pathParams), queryParams);
    }

    protected void setHeader() {
        BasicHeader[] basicHeaders = this.headers.entrySet().stream().map(
                e -> new BasicHeader(e.getKey(), e.getValue().toString())
        ).toArray(BasicHeader[]::new);
        this.httpRequestBase.setHeaders(basicHeaders);
    }

    @SuppressWarnings("unchecked")
    protected Object resolveResult(CloseableHttpResponse response) throws Exception {
        if (this.returnType == String.class) return EntityUtils.toString(response.getEntity());
        if (this.returnType == org.apache.http.Header[].class) return response.getAllHeaders();
        for (ParameterAndValue parameterAndValue : parameterAndValues) {
            Class<?> type = parameterAndValue.getParameter().getType();
            if (ResolveResult.class.isAssignableFrom(type)) {
                return ((ResolveResult<?>) parameterAndValue.getValue()).resolve(response);
            }
        }
        if (methodAnnotations.isPresent(Result.class)) {
            MergedAnnotation<Result> resultMergedAnnotation = methodAnnotations.get(Result.class);
            Class<? extends ResolveResult<?>> value = (Class<? extends ResolveResult<?>>)
                    resultMergedAnnotation.getClass("value");
            ResolveResult<?> o = value.newInstance();
            return o.resolve(response);
        }
        return null;
    }
}
