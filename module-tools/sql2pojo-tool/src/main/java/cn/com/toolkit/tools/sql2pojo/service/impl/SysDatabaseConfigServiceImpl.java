package cn.com.toolkit.tools.sql2pojo.service.impl;

import cn.com.toolkit.framework.core.annotation.Transaction;
import cn.com.toolkit.tools.sql2pojo.domain.po.SysDatabaseConfig;
import cn.com.toolkit.tools.sql2pojo.mapper.SysDatabaseConfigMapper;
import cn.com.toolkit.tools.sql2pojo.service.SysDatabaseConfigService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

public class SysDatabaseConfigServiceImpl extends ServiceImpl<SysDatabaseConfigMapper, SysDatabaseConfig> implements SysDatabaseConfigService {
    @Transaction
    @Override
    public void updateDefault(SysDatabaseConfig sysDatabaseConfig) {
        update(Wrappers.<SysDatabaseConfig>lambdaUpdate()
                .eq(SysDatabaseConfig::getIsDefault,true)
                .set(SysDatabaseConfig::getIsDefault,false));
        update(Wrappers.<SysDatabaseConfig>lambdaUpdate()
                .eq(SysDatabaseConfig::getName,sysDatabaseConfig.getName())
                .set(SysDatabaseConfig::getIsDefault,true));
    }
}
