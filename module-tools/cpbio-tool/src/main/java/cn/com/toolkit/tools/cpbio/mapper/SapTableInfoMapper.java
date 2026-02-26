package cn.com.toolkit.tools.cpbio.mapper;


import cn.com.toolkit.tools.cpbio.domain.dto.SapTableInfoDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SapTableInfoMapper extends BaseMapper<SapTableInfoDTO> {
    List<SapTableInfoDTO> selectFieldList(@Param("tableName") String tableName);
    List<SapTableInfoDTO> selectTableList(@Param("keyword") String keyword);

}
