package com.etms.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 用户请求DTO
 */
@Data
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long id;

    /** 用户名 */
    @NotBlank(message = "用户名不能为空", groups = {Add.class, Update.class})
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    private String username;

    /** 密码 */
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间", groups = {Add.class})
    private String password;

    /** 真实姓名 */
    @NotBlank(message = "真实姓名不能为空", groups = {Add.class, Update.class})
    @Size(max = 50, message = "真实姓名不能超过50个字符")
    private String realName;

    /** 性别 */
    private Integer gender;

    /** 邮箱 */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱不能超过100个字符")
    private String email;

    /** 手机号 */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 头像URL */
    private String avatar;

    /** 部门ID */
    private Long deptId;

    /** 岗位ID */
    private Long positionId;

    /** 状态 */
    private Integer status;

    /** 备注 */
    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;

    /** 角色ID列表 */
    private List<Long> roleIds;

    /** 新增校验组 */
    public interface Add {}

    /** 更新校验组 */
    public interface Update {}
}
