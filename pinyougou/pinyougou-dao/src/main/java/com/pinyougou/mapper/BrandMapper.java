package com.pinyougou.mapper;

import com.pinyougou.pojo.TbBrand;
import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.ids.DeleteByIdsMapper;

import java.util.List;

public interface BrandMapper extends BaseMapper<TbBrand> {
    List<TbBrand> queryAll();
}
