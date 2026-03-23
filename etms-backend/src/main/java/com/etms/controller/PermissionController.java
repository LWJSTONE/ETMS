package com.etms.controller;

import com.etms.common.Result;
import com.etms.entity.Permission;
import com.etms.service.PermissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 权限管理控制器
 */
@Api(tags = "权限管理")
@RestController
@RequestMapping("/system/permissions")
@RequiredArgsConstructor
public class PermissionController {
    
    private final PermissionService permissionService;
    
    /**
     * 获取权限树
     * 用于角色权限分配
     * @return 权限树列表
     */
    @ApiOperation(value = "获取权限树")
    @GetMapping("/tree")
    public Result<List<Permission>> getPermissionTree() {
        List<Permission> tree = permissionService.getPermissionTree();
        return Result.success(tree);
    }
}
