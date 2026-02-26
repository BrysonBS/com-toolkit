package cn.com.toolkit.tools.cpbio.service.impl;

import cn.com.toolkit.tools.cpbio.domain.dto.SapTableInfoDTO;
import cn.com.toolkit.tools.cpbio.mapper.SapTableInfoMapper;
import cn.com.toolkit.tools.cpbio.service.SapTableInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SapTableInfoServiceImpl extends ServiceImpl<SapTableInfoMapper, SapTableInfoDTO>
        implements SapTableInfoService {
    @Override
    public List<SapTableInfoDTO> selectFieldList(String tableName) {
        return baseMapper.selectFieldList(Optional.ofNullable(tableName)
                .map(String::toUpperCase)
                .orElse(null));
    }

    @Override
    public List<SapTableInfoDTO> selectTableList(String keyword) {
        return baseMapper.selectTableList(Optional.ofNullable(keyword)
                .map(String::toUpperCase)
                .orElse(null));
    }

    @Override
    public String generateDDL(List<SapTableInfoDTO> columnsList){
        if(columnsList == null || columnsList.isEmpty()) return null;
        String tableName = columnsList.get(0).getTableName();
        String tableText = columnsList.get(0).getTableText();
        String uniqueKey = columnsList.stream()
                .filter(e -> Strings.CI.equals("X",e.getKeyFlag()))
                .map(SapTableInfoDTO::getFieldName).collect(Collectors.joining("`,`"));

        String newLine =  System.lineSeparator();
        String space = " ";
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE `ods_")
                .append(tableName.toLowerCase())
                .append("` (").append(newLine);

        for(int i = 0; i< columnsList.size(); i++){
            SapTableInfoDTO column = columnsList.get(i);

            builder.append("  `").append(column.getFieldName()).append("`").append(space);

            switch (column.getDataType().toUpperCase()){
                case "CURR":
                case "QUAN":
                case "D16D":
                case "D16R":
                case "D16S":
                case "D34D":
                case "D34R":
                case "D34S":
                case "DEC":
                case "FLTP":
                    builder.append("decimal(").append(column.getLength() + 3).append(",3) ");
                    break;
                case "INT1":
                case "INT2":
                case "INT4":
                case "INT8":
                    builder.append("int NULL ");
                    break;
                default:
                    builder.append("varchar(").append(column.getLength() * 3).append(") ");
                    break;
            }

            builder.append("NULL").append(space);
            builder.append("COMMENT \"").append(column.getContent()).append("\"");

            if(i < columnsList.size() - 1) builder.append(",");

            builder.append(newLine);
        }
        builder.append(") ENGINE=OLAP").append(newLine)
                .append("UNIQUE KEY(`").append(uniqueKey).append("`)").append(newLine)
                .append("COMMENT '").append(tableText).append("'").append(newLine)
                .append("DISTRIBUTED BY HASH(``) BUCKETS AUTO");
        return builder.toString();
    }
}
