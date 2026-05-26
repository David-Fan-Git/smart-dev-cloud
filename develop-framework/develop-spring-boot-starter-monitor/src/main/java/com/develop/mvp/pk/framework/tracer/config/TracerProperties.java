package com.develop.mvp.pk.framework.tracer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * BizTracer配置类
 *
 * @author David
 */
@ConfigurationProperties("develop.tracer")
@Data
public class TracerProperties {
}
