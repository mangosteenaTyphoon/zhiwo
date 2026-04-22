package com.shanzhu.security.handler;

import com.shanzhu.common.model.response.basecontroller.StrResponseController;
import com.shanzhu.common.utils.web.WebUtils;
import com.shanzhu.security.manager.LoginUserManager;
import com.shanzhu.security.model.LoginUser;
import com.shanzhu.security.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class LogoutSuccessHandlerImpl extends StrResponseController implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, Authentication authentication) {
        String token = TokenUtils.getToken(request);

        LoginUser loginUser = LoginUserManager.getLoginUser(token);

        if (loginUser != null) {
            LoginUserManager.removeLoginUserCache(token);
        }

        WebUtils.renderJson(success("退出成功"));
    }

}
