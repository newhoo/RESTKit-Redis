package io.github.newhoo.restkit.ext.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * RedisSettingComponent
 *
 * @author huzunrong
 * @since 1.0.0
 */
@State(name = "RESTKit_Setting_Ext", storages = {@Storage("restkit/RESTKit_Setting_Ext.xml")})
public class RedisSettingComponent implements PersistentStateComponent<RedisSetting> {

    private final RedisSetting redisSetting = new RedisSetting();

    public static RedisSettingComponent getInstance(Project project) {
        return project.getService(RedisSettingComponent.class);
    }

    @NotNull
    @Override
    public RedisSetting getState() {
        return redisSetting;
    }

    @Override
    public void loadState(@NotNull RedisSetting state) {
        XmlSerializerUtil.copyBean(state, Objects.requireNonNull(getState()));
    }
}
