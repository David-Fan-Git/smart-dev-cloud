package com.develop.mvp.pk.framework.desensitize.core.regex.handler;

import com.develop.mvp.pk.framework.desensitize.core.regex.annotation.RegexDesensitize;

/**
 * {@link RegexDesensitize} 的正则脱敏处理器
 *
 * @author David
 */
public class DefaultRegexDesensitizationHandler extends AbstractRegexDesensitizationHandler<RegexDesensitize> {

    @Override
    String getRegex(RegexDesensitize annotation) {
        return annotation.regex();
    }

    @Override
    String getReplacer(RegexDesensitize annotation) {
        return annotation.replacer();
    }

    @Override
    public String getDisable(RegexDesensitize annotation) {
        return annotation.disable();
    }

}
