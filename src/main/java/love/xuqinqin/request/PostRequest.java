package love.xuqinqin.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import love.xuqinqin.request.annotation.param.FormParam;
import love.xuqinqin.request.annotation.param.JSONParam;
import love.xuqinqin.request.util.ParameterAndValue;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.springframework.core.annotation.MergedAnnotations;

import java.io.File;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PostRequest extends BaseRequest {

    protected final Map<String, Object> formParams = new HashMap<>();

    protected final Map<String, Object> jsonParams = new HashMap<>();

    public Map<String, Object> getFormParams() {
        return formParams;
    }

    public Map<String, Object> getJsonParams() {
        return jsonParams;
    }

    @Override
    public void init() {
        super.init();
        for (ParameterAndValue parameterAndValue : getParameterAndValues()) {
            Parameter parameter = parameterAndValue.getParameter();
            MergedAnnotations methodAnnotations = MergedAnnotations.from(parameter);
            resolveParameterAndValue(this.formParams, parameterAndValue, methodAnnotations, FormParam.class);
            resolveParameterAndValue(this.jsonParams, parameterAndValue, methodAnnotations, JSONParam.class);
        }
        setHttpRequestBase(new HttpPost(getUri()));
        ((HttpEntityEnclosingRequestBase)getHttpRequestBase()).setEntity(getFormOrJSONEntity(formParams, jsonParams));
    }


    private HttpEntity getFormOrJSONEntity(Map<String, Object> formParams, Map<String, Object> jsonParams) {
        if (formParams.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonStr;
            try {
                jsonStr = objectMapper.writeValueAsString(jsonParams);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return new StringEntity(jsonStr, ContentType.create("application/json", StandardCharsets.UTF_8));
        }
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        multipartEntityBuilder.setCharset(StandardCharsets.UTF_8);
        for (Map.Entry<String, Object> formParam : formParams.entrySet()) {
            if (File.class.isAssignableFrom(formParam.getValue().getClass())) {
                multipartEntityBuilder.addBinaryBody(formParam.getKey(), (File) formParam.getValue());
                continue;
            }
            multipartEntityBuilder.addTextBody(formParam.getKey(), formParam.getValue().toString(), ContentType.create(
                    "text/plain", StandardCharsets.UTF_8
            ));
        }
        return multipartEntityBuilder.build();
    }
}
