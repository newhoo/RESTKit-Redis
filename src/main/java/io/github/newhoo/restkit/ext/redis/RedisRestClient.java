package io.github.newhoo.restkit.ext.redis;

import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.KV;
import io.github.newhoo.restkit.common.Request;
import io.github.newhoo.restkit.common.RequestInfo;
import io.github.newhoo.restkit.common.Response;
import io.github.newhoo.restkit.common.RestClientData;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.ext.config.RedisSetting;
import io.github.newhoo.restkit.ext.config.RedisSettingComponent;
import io.github.newhoo.restkit.restful.RestClient;
import io.github.newhoo.restkit.restful.ep.RestClientProvider;
import io.github.newhoo.restkit.util.JsonUtils;
import nl.melp.redis.Redis;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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
    public List<KV> getConfig(@NotNull RestItem restItem, @NotNull Project project) {
        RedisSetting redisSetting = RedisSettingComponent.getInstance(project).getState();
        return Arrays.asList(
                new KV("address", redisSetting.getRedisAddress())
        );
    }

    @NotNull
    @Override
    public Request createRequest(RestClientData restClientData, Project project) {
        Request request = new Request();
        request.setUrl(restClientData.getUrl());
        request.setMethod(restClientData.getMethod());
        request.setConfig(restClientData.getConfig());
        request.setHeaders(restClientData.getHeaders());
        request.setParams(restClientData.getConfig());
        request.setBody(restClientData.getBody());

        return request;
    }

    @NotNull
    @Override
    public RequestInfo sendRequest(Request request, Project project) {
        long startTs = System.currentTimeMillis();
        Object[] args;
        try {
            args = JsonUtils.fromJsonArr(request.getBody(), Object.class).toArray(new Object[0]);
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
                response.setBody(JsonUtils.toJson(result));
            }
            return new RequestInfo(request, response, address, System.currentTimeMillis() - startTs);
        } catch (Exception e) {
            RequestInfo requestInfo = new RequestInfo(request, e.toString());
            requestInfo.setCost(System.currentTimeMillis() - startTs);
            return requestInfo;
        }
    }

    @NotNull
    @Override
    public String formatResponseInfo(RequestInfo requestInfo) {
        return JsonUtils.toJson(requestInfo);
    }

    @NotNull
    @Override
    public String formatLogInfo(RequestInfo requestInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("############################# ").append(LocalDateTime.now()).append(" #############################").append("\n");
        sb.append(JsonUtils.toJson(requestInfo)).append("\n");
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
}
