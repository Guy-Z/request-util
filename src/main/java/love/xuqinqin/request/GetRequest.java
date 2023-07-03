package love.xuqinqin.request;

import org.apache.http.client.methods.HttpGet;

public class GetRequest extends BaseRequest{

    @Override
    public void init() {
        super.init();
        setHttpRequestBase(new HttpGet(getUri()));
    }

}
