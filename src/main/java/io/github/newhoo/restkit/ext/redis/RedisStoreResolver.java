package io.github.newhoo.restkit.ext.redis;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.ext.config.RedisSetting;
import io.github.newhoo.restkit.ext.config.RedisSettingComponent;
import io.github.newhoo.restkit.ext.config.RedisSettingConfigurable;
import io.github.newhoo.restkit.restful.RequestResolver;
import io.github.newhoo.restkit.restful.ep.RestfulResolverProvider;
import io.github.newhoo.restkit.util.HtmlUtil;
import io.github.newhoo.restkit.util.JsonUtils;
import io.github.newhoo.restkit.util.NotifierUtils;
import nl.melp.redis.Redis;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class RedisStoreResolver implements RequestResolver {

    private final Project project;
    private final String projectName;
    private String hostname;
    private Integer port;

    public RedisStoreResolver(Project project) {
        this.project = project;

        RedisSetting setting = RedisSettingComponent.getInstance(project).getState();
        this.projectName = setting.getRedisProject();
        String redisAddress = setting.getRedisAddress();
        String[] split = redisAddress.split(":");
        if (split.length > 1) {
            this.hostname = split[0].trim();
            this.port = Integer.parseInt(split[1].trim());
        }
    }

    @Override
    public String getFrameworkName() {
        return "Redis Store";
    }

    @Override
    public ScanType getScanType() {
        return ScanType.STORAGE;
    }

    @Override
    public boolean checkConfig() {
        if (StringUtils.isAnyEmpty(hostname, projectName) || port == null) {
            NotifierUtils.infoBalloon("", "Redis server config is empty. " + HtmlUtil.link("Edit", "Edit"), new NotificationListener.Adapter() {
                @Override
                protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, RedisSettingConfigurable.class);
                }
            }, project);
            return false;
        }
        return true;
    }

    @Override
    public List<RestItem> findRestItemInProject(@NotNull Project project) {
        try {
            Redis redis = getRedis();
            List<Object> result = redis.call("LRANGE", getKey(), "0", "100000");
            if (CollectionUtils.isNotEmpty(result)) {
                return result.stream()
                             .map(e -> JsonUtils.fromJson(new String((byte[]) e), RestItem.class))
                             .collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public void add(List<RestItem> itemList) {
        AppExecutorUtil.getAppExecutorService().submit(() -> {
            try {
                Redis redis = getRedis();
                for (RestItem restItem : itemList) {
                    restItem.setId(UUID.randomUUID().toString());
                    redis.call("RPUSH", getKey(), JsonUtils.toJson(restItem));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void update(List<RestItem> itemList) {
        AppExecutorUtil.getAppExecutorService().submit(() -> {
            Map<String, RestItem> idMap = itemList.stream()
                                                  .collect(Collectors.toMap(RestItem::getId, o -> o, (o1, o2) -> o1));
            List<RestItem> list = new ArrayList<>(findRestItemInProject(project));
            list.stream()
                .filter(item -> idMap.containsKey(item.getId()))
                .forEach(item -> {
                    RestItem restItem = idMap.get(item.getId());
                    item.setUrl(restItem.getUrl());
                    item.setMethod(restItem.getMethod());
                    item.setHeaders(restItem.getHeaders());
                    item.setParams(restItem.getParams());
                    item.setBodyJson(restItem.getBodyJson());
                    item.setDescription(restItem.getDescription());
                    item.setModuleName(restItem.getModuleName());
                    item.setFramework(restItem.getFramework());
                    item.setTs(restItem.getTs());
                });
            replaceAll(list);
        });
    }

    @Override
    public void delete(List<RestItem> itemList) {
        AppExecutorUtil.getAppExecutorService().submit(() -> {
            Set<String> idSet = itemList.stream().map(RestItem::getId).collect(Collectors.toSet());
            List<RestItem> list = new ArrayList<>(findRestItemInProject(project));
            list.removeIf(item -> idSet.contains(item.getId()));
            replaceAll(list);
        });
    }

    public void replaceAll(List<RestItem> itemList) {
        try {
            Redis redis = getRedis();
            redis.call("DEL", getKey());
            for (RestItem restItem : itemList) {
                redis.call("RPUSH", getKey(), JsonUtils.toJson(restItem));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Redis getRedis() throws IOException {
        return new nl.melp.redis.Redis(new Socket(hostname, port));
    }

    private String getKey() {
        return "RESTKit:" + projectName;
    }

    public static class RedisStoreResolverProvider implements RestfulResolverProvider {

        @Override
        public RequestResolver createRequestResolver(@NotNull Project project) {
            return new RedisStoreResolver(project);
        }
    }
}
