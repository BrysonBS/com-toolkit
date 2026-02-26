package cn.com.toolkit.tools.cpbio.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SapTableInfoDTO {
    private String tableText;
    private String tableName;
    private String fieldName;
    private String dataType;
    private Integer length;
    private String content;
    private String keyFlag;
}
