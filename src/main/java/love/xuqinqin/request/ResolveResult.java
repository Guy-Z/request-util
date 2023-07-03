package love.xuqinqin.request;

import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;

public interface ResolveResult<T> {

    T resolve(CloseableHttpResponse response) throws IOException;

}
