package com.eversec.llmops.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author NiYongZhe
 * @since 2026-04-03
 */
@Getter
@Setter
@TableName("pro_user")
public class ProUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 项目id
     */
    @TableField("pro_id")
    private Integer proId;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 是否管理员,0:是，1:否
     */
    @TableField("is_admin")
    private Integer isAdmin;

    /**
     * 创建时间
     */
    @TableField("createtime")
    private LocalDateTime createtime;

    /**
     * 修改时间
     */
    @TableField("updatetime")
    private LocalDateTime updatetime;

    /**
     * 用户名称
     */
    @TableField("username")
    private String username;
}
