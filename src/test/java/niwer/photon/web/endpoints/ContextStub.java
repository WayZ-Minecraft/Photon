package niwer.photon.web.endpoints;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import io.javalin.http.Context;

final class ContextStub {

    private final Map<String, String> formParams = new HashMap<>();
    private final Map<String, String> queryParams = new HashMap<>();
    private final Map<String, String> requestHeaders = new HashMap<>();
    private final Map<String, String> responseHeaders = new HashMap<>();

    private String body;
    private String ip = "127.0.0.1";
    private Integer statusCode;
    private Object resultBody;
    private Object jsonBody;
    private String contentType;

    private final Context proxy;

    ContextStub() {
        this.proxy = (Context) Proxy.newProxyInstance(
            Context.class.getClassLoader(),
            new Class<?>[] { Context.class },
            new Handler()
        );
    }

    Context context() {
        return this.proxy;
    }

    ContextStub formParam(String key, String value) {
        this.formParams.put(key, value);
        return this;
    }

    ContextStub queryParam(String key, String value) {
        this.queryParams.put(key, value);
        return this;
    }

    ContextStub requestHeader(String key, String value) {
        this.requestHeaders.put(key, value);
        return this;
    }

    ContextStub body(String value) {
        this.body = value;
        return this;
    }

    ContextStub ip(String value) {
        this.ip = value;
        return this;
    }

    Integer statusCode() {
        return this.statusCode;
    }

    Object resultBody() {
        return this.resultBody;
    }

    Object jsonBody() {
        return this.jsonBody;
    }

    String contentType() {
        return this.contentType;
    }

    Map<String, String> responseHeaders() {
        return this.responseHeaders;
    }

    private final class Handler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            final String name = method.getName();
            final int argCount = args == null ? 0 : args.length;

            if (name.equals("formParam") && argCount == 1) return formParams.get(String.valueOf(args[0]));
            if (name.equals("queryParam") && argCount == 1) return queryParams.get(String.valueOf(args[0]));
            if (name.equals("header") && argCount == 1) return requestHeaders.get(String.valueOf(args[0]));
            if (name.equals("body") && argCount == 0) return body;
            if (name.equals("ip") && argCount == 0) return ip;

            if (name.equals("status") && argCount == 1 && args[0] instanceof Number number) {
                statusCode = number.intValue();
                return proxy;
            }

            if (name.equals("result") && argCount == 1) {
                resultBody = args[0];
                return proxy;
            }

            if (name.equals("json") && argCount == 1) {
                jsonBody = args[0];
                return proxy;
            }

            if (name.equals("contentType") && argCount == 1) {
                contentType = String.valueOf(args[0]);
                return proxy;
            }

            if (name.equals("header") && argCount == 2) {
                responseHeaders.put(String.valueOf(args[0]), String.valueOf(args[1]));
                return proxy;
            }

            if (method.getReturnType().isAssignableFrom(Context.class)) return proxy;
            return defaultValue(method.getReturnType());
        }
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        if (type == char.class) return '\0';
        return null;
    }
}