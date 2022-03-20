package io.github.newhoo.restkit.ext.config;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * SettingConfigurable
 *
 * @author huzunrong
 * @since 2.0.0
 */
public class RedisSettingConfigurable implements Configurable {

    private final RedisSetting redisSetting;
    private final SettingForm settingForm;

    private RedisSettingConfigurable(Project project) {
        this.redisSetting = RedisSettingComponent.getInstance(project).getState();
        this.settingForm = new SettingForm();
    }

    @Nls(capitalization = Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Redis Config";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return settingForm.getMainPanel();
    }

    @Override
    public boolean isModified() {
        return redisSetting.isModified(settingForm.getModifiedSetting());
    }

    @Override
    public void apply() {
        settingForm.saveTo(redisSetting);
    }

    @Override
    public void reset() {
        settingForm.reset(redisSetting);
    }
}