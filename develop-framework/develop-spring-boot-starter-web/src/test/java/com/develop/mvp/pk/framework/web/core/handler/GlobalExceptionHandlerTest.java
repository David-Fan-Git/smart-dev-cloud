package com.develop.mvp.pk.framework.web.core.handler;

import com.develop.mvp.pk.framework.common.biz.infra.logger.ApiErrorLogCommonApi;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.MultipartException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler("test", mock(ApiErrorLogCommonApi.class));

    @Test
    void handlesMismatchedJsonBodyAsBadRequest() {
        MismatchedInputException cause = MismatchedInputException.from((JsonParser) null, String.class, "bad json type");
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("JSON parse error", cause);

        CommonResult<?> result = handler.methodArgumentTypeInvalidFormatExceptionHandler(exception);

        assertEquals(400, result.getCode());
    }

    @Test
    void handlesMultipartExceptionAsBadRequest() {
        CommonResult<?> result = handler.allExceptionHandler(mock(HttpServletRequest.class),
                new MultipartException("Current request is not a multipart request"));

        assertEquals(400, result.getCode());
    }

    @Test
    void handlesIllegalArgumentExceptionAsBadRequest() {
        CommonResult<?> result = handler.allExceptionHandler(mock(HttpServletRequest.class),
                new IllegalArgumentException("旧密码不匹配"));

        assertEquals(400, result.getCode());
    }

    @Test
    void handlesClassNotFoundExceptionAsBadRequest() {
        CommonResult<?> result = handler.allExceptionHandler(mock(HttpServletRequest.class),
                new ClassNotFoundException("ct20260527134105"));

        assertEquals(400, result.getCode());
    }
}
