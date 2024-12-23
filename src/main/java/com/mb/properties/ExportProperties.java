package com.mb.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 导出配置
 * @author 石鹏
 * @date 2024/12/20 16:49
 */
@ConfigurationProperties(prefix = "export")
@Data
public class ExportProperties {

    /** 导出参数名 */
    private String exportParam = "export";

    /** 导出全部参数名 */
    private String exportAllParam = "exportAll";

    /** 页码参数名 */
    private String pageNumParam = "pageNum";

    /** 每页大小参数名 */
    private String pageSizeParam = "pageSize";

    /**
     * 响应数据
     * 如果为空或default，就直接取方法返回值
     * 如果是统一返回类，那么就写类中数据的字段名，比如data
     * */
    private String responseData;
}
