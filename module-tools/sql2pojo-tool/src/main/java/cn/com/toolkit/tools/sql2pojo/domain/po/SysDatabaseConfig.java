package cn.com.toolkit.tools.sql2pojo.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("sys_database_config")
public class SysDatabaseConfig {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    @TableField("jdbc_url")
    private String url;
    private String username;
    private String password;
    @TableField("is_default")
    private Boolean isDefault;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;

}
