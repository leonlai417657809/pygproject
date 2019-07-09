package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.BrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import com.pinyougou.service.impl.BaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandServiceImpl extends BaseServiceImpl<TbBrand> implements BrandService {


    @Autowired
    private BrandMapper brandMapper;
/*

    @Autowired
    public void setBrandMapper(BrandMapper brandMapper) {
        super.setMapper(brandMapper);
        this.brandMapper = brandMapper;
    }
*/

    @Override
    public List<TbBrand> queryAll() {
        return brandMapper.queryAll();
    }

    @Override
    public List<TbBrand> testPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return brandMapper.selectAll();
    }

    @Override
    public PageInfo<TbBrand> search(Integer pageNum, Integer pageSize, TbBrand brand) {
        PageHelper.startPage(pageNum,pageSize);

        Example example = new Example(TbBrand.class);

        Example.Criteria criteria = example.createCriteria();

        if(StringUtils.isNotBlank(brand.getFirstChar())){
            criteria.andEqualTo("firstChar",brand.getFirstChar());
        }

        if(StringUtils.isNotBlank(brand.getName())){
            criteria.andLike("name","%"+brand.getName()+"%");
        }

        List<TbBrand> list = brandMapper.selectByExample(example);

        return new PageInfo<>(list);
    }
}
