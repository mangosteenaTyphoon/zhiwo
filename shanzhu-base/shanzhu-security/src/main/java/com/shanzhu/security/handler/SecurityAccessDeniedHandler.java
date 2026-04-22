package com.shanzhu.security.handler;

import com.shanzhu.common.enums.ResultCodeEnum;
import com.shanzhu.common.model.response.response.StrResponse;
import com.shanzhu.common.utils.web.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;


/**
 * 权限异常处理器
 * 403
 */
@Component
public class SecurityAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
        WebUtils.renderJson(StrResponse.error(ResultCodeEnum.ACCESS_ERROR));
    }
}