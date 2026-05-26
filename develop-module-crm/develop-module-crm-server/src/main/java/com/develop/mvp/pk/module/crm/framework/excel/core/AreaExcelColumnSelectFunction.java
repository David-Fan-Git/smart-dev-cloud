package com.develop.mvp.pk.module.crm.framework.excel.core;

import com.develop.mvp.pk.framework.excel.core.function.ExcelColumnSelectFunction;
import com.develop.mvp.pk.framework.ip.core.Area;
import com.develop.mvp.pk.framework.ip.core.utils.AreaUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 地区下拉框数据源的 {@link ExcelColumnSelectFunction} 实现类
 *
 * @author David
 */
@Service
public class AreaExcelColumnSelectFunction implements ExcelColumnSelectFunction {

    public static final String NAME = "getCrmAreaNameList"; // 防止和别的模块重名

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<String> getOptions() {
        // 获取地区下拉数据
        // TODO @puhui999：嘿嘿，这里改成省份、城市、区域，三个选项，难度大么？
        Area area = AreaUtils.getArea(Area.ID_CHINA);
        return AreaUtils.getAreaNodePathList(area.getChildren());
    }

}
