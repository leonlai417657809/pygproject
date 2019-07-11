package com.pinyougou.mapper;

import com.pinyougou.pojo.TbSpecificationOption;

import java.util.List;
import java.util.Map;

public interface SpecificationOptionMapper extends BaseMapper<TbSpecificationOption> {
    List<Map<String, Object>> selectOptionList();
}
