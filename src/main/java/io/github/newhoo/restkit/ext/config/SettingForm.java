package io.github.newhoo.restkit.ext.config;

import lombok.Getter;

import javax.swing.*;

/**
 * SettingForm
 *
 * @author huzunrong
 * @since 1.0
 */
public class SettingForm {

    @Getter
    private JPanel mainPanel;
    private JTextField redisAddressTextField;
    private JTextField redisProjectTextField;

    public SettingForm() {
        // 要单独写一个方法，直接用获取到的组件是null
        initView();
        initEvent();
    }

    private void initView() {
//        requestPanel.setBorder(IdeBorderFactory.createTitledBorder("Redis Config", false));
    }

    private void initEvent() {
    }

    public RedisSetting getModifiedSetting() {
        RedisSetting redisSetting = new RedisSetting();
        saveTo(redisSetting);
        return redisSetting;
    }

    public void saveTo(RedisSetting redisSetting) {
        redisSetting.setRedisAddress(redisAddressTextField.getText());
        redisSetting.setRedisProject(redisProjectTextField.getText());
    }

    public void reset(RedisSetting redisSetting) {
        redisAddressTextField.setText(redisSetting.getRedisAddress());
        redisProjectTextField.setText(redisSetting.getRedisProject());
    }
}
