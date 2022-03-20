package io.github.newhoo.restkit.ext.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * common setting
 *
 * @author huzunrong
 * @since 2.0.0
 */
@Data
public class RedisSetting {

    private String redisAddress = "";
    private String redisProject = "";

    public boolean isModified(RedisSetting modifiedSetting) {
        return !StringUtils.equals(redisAddress, modifiedSetting.getRedisAddress())
                || !StringUtils.equals(redisProject, modifiedSetting.getRedisProject());
    }
}
