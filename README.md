# request-util
## Example:
```java
@Request("http://127.0.0.1:8080")
public interface TestRequest {

    @Post("/test")
    String test(@JSONParam("name") String name);

    @Post("/testWithFile")
    @Result(BodyToString.class)
    @Headers({@Header(name = "token", headerValue = "10011001"), @Header(name = "type", headerValue = "3")})
    <T> Integer testWithFile(@FormParam Map<String, Object> params, ResolveResult<T> resolveResult);

    class BodyToString implements ResolveResult<String>{
        @Override
        public String resolve(CloseableHttpResponse response) throws IOException {
            return EntityUtils.toString(response.getEntity());
        }
    }

}
```