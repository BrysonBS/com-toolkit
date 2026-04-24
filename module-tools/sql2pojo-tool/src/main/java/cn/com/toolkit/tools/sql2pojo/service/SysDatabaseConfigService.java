package cn.com.toolkit.tools.sql2pojo.service;

import cn.com.toolkit.tools.sql2pojo.domain.po.SysDatabaseConfig;
import com.baomidou.mybatisplus.extension.service.IService;


public interface SysDatabaseConfigService extends IService<SysDatabaseConfig> {
    void updateDefault(SysDatabaseConfig sysDatabaseConfig);
}
