package io.github.newhoo.restkit.ext.redis;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypes;
import io.github.newhoo.restkit.open.RestClient;
import io.github.newhoo.restkit.open.ep.RestClientProvider;
import io.github.newhoo.restkit.open.model.KV;
import io.github.newhoo.restkit.open.model.RestClientData;
import io.github.newhoo.restkit.open.request.Request;
import io.github.newhoo.restkit.open.request.RequestInfo;
import io.github.newhoo.restkit.open.request.Response;
import io.github.newhoo.restkit.open.request.Status;
import nl.melp.redis.Redis;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huzunrong
 * @since 1.0.0
 */
public class RedisRestClient implements RestClient {

    @NotNull
    @Override
    public String getProtocol() {
        return "redis";
    }

    @Override
    public @NotNull List<KV> getConfig(@NotNull String projectName) {
        return Arrays.asList(
                new KV("address", "{{redisAddress}}")
        );
    }

    @Override
    public @NotNull Map<String, String> getConfigLabel() {
        Map<String, String> map = new HashMap<>();
        map.put("address", "Address: ");
        return map;
    }

    @Override
    public @NotNull Request createRequest(RestClientData restClientData) {
        Request request = new Request();

        request.setUrl(restClientData.getUrl());
        request.setMethod(restClientData.getMethod());
        request.setConfig(restClientData.getConfig());
        request.setHeaders(restClientData.getHeaders());
        request.setParams(restClientData.getParams());
        request.setBody(restClientData.getBody());

        return request;
    }

    @Override
    public @NotNull RequestInfo sendRequest(Request request) {
        long startTs = System.currentTimeMillis();
        Object[] args;
        try {
            args = fromJsonArr(request.getBody(), Object.class).toArray(new Object[0]);
        } catch (Exception e) {
            return new RequestInfo(request, "wrong redis command, should be String array such as \n[\"GET\", \"key\"] ");
        }
        try {
            String address = request.getConfig().get("address");
            Redis redis = getRedis(address);
            Object result = redis.call(args);
            Response response = new Response();
            if (result instanceof byte[]) {
                response.setBody(new String((byte[]) result));
            } else {
                response.setBody(toJson(result));
            }
            return new RequestInfo(request, response, System.currentTimeMillis() - startTs, address, "127");
        } catch (Exception e) {
            RequestInfo requestInfo = new RequestInfo(request, e.toString());
            requestInfo.setTime(System.currentTimeMillis() - startTs);
            return requestInfo;
        }
    }

    @Override
    public @NotNull FileType parseResponseFileType(Response response) {
        return FileTypes.PLAIN_TEXT;
    }

    @Override
    public void cancelRequest(@NotNull Request request) {
    }

    @Override
    public @NotNull Status getResponseStatus(RequestInfo requestInfo) {
        if (requestInfo.getResponse() == null || requestInfo.getResponse().getBody() == null) {
            return new Status("ERROR", null, requestInfo.getTime(), null, requestInfo.getRemoteAddress(), requestInfo.getLocalAddress());
        }
        String status = "success";
        return new Status(status, "success", requestInfo.getTime(), (long) requestInfo.getResponse().getBody().length(), requestInfo.getRemoteAddress(), requestInfo.getLocalAddress());
    }

    @NotNull
    @Override
    public String formatResponseInfo(RequestInfo requestInfo) {
        return toJson(requestInfo);
    }

    @NotNull
    @Override
    public String formatLogInfo(RequestInfo requestInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("############################# ").append(LocalDateTime.now()).append(" #############################").append("\n");
        sb.append(toJson(requestInfo)).append("\n");
        return sb.toString();
    }

    private Redis getRedis(String redisAddress) throws IOException {
        String[] split = redisAddress.split(":");
        if (split.length > 1) {
            return new nl.melp.redis.Redis(new Socket(split[0].trim(), Integer.parseInt(split[1].trim())));
        }
        throw new IllegalArgumentException("wrong redis address: " + redisAddress);
    }

    public static class RedisClientProvider implements RestClientProvider {
        @Override
        public RestClient createClient() {
            return new RedisRestClient();
        }
    }

    private String toJson(Object obj) {
        if (obj == null || (obj instanceof CharSequence && ((CharSequence) obj).length() == 0)) {
            return "";
        }
        return new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create().toJson(obj);
    }

    private <T> List<T> fromJsonArr(String json, Class<T> classOfT) throws JsonSyntaxException {
        return new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create().fromJson(json, TypeToken.getParameterized(List.class, classOfT).getType());
    }
}
