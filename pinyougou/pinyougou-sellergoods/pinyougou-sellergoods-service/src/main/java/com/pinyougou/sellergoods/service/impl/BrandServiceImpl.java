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
import java.util.Map;

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
        //分页
        PageHelper.startPage(pageNum,pageSize);

        //设置查询条件
        //sql---> SELECT * FROM tb_brand WHERE first_char=? AND `name` LIKE '%name%'

        //查询
        Example example = new Example(TbBrand.class);
        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();
        //首字母
        if(StringUtils.isNotBlank(brand.getFirstChar())){
            criteria.andEqualTo("firstChar",brand.getFirstChar());
        }
        //名称模糊查询
        if(StringUtils.isNotBlank(brand.getName())){
            criteria.andLike("name","%"+brand.getName()+"%");
        }

        //排序，根据首字母降序排序
        //example.orderBy("firstChar").desc();
        List<TbBrand> list = brandMapper.selectByExample(example);

        //返回分页信息对象
        return new PageInfo<>(list);
    }

    @Override
    public List<Map<String, Object>> selectOptionList() {
        return brandMapper.selectOptionList();
    }
}
