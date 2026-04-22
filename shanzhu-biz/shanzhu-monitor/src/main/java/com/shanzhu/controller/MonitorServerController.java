package com.shanzhu.controller;

import com.shanzhu.common.model.response.ApiResponseModel;
import com.shanzhu.common.model.response.basecontroller.ApiResponseController;
import com.shanzhu.model.ServerInfo;
import com.shanzhu.service.MonitorServerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "服务监控")
@RestController
@RequestMapping("monitor/server")
public class MonitorServerController extends ApiResponseController {

    @Resource
    private MonitorServerService monitorServerService;

    @Operation(summary = "服务信息")
    @GetMapping
    public ApiResponseModel<ServerInfo> serverInfo() {
        return success(monitorServerService.serverInfo());
    }
}
