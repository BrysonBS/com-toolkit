package cn.com.toolkit.tools.cpbio.service;


import cn.com.toolkit.tools.cpbio.domain.dto.SapTableInfoDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SapTableInfoService extends IService<SapTableInfoDTO> {
    String generateDDL(List<SapTableInfoDTO> columnsList);
    List<SapTableInfoDTO> selectFieldList(String tableName);
    List<SapTableInfoDTO> selectTableList(String keyword);
}
