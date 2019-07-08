package com.pinyougou.mapper;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbBrand;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/*.xml")
public class BrandMapperTest {

    @Autowired
    private BrandMapper brandMapper;


    /**
     * 新增
     * insert into tb_brand(id, name, first_char) values(?,?,?);
     * 选择性新增 如果brand实例不设置firstChar属性值的话；那么插入语句：
     * insert into tb_brand(id, name) values(?,?);
     */
    @Test
    public void insertSelective() {
        TbBrand brand = new TbBrand();
        brand.setName("test1");
        brand.setFirstChar("T");
        brandMapper.insertSelective(brand);
    }

    /**
     * 更新
     * update tb_brand set name=?,first_char=?(null) where id=?
     * 选择性更新 如果brand实例不设置firstChar属性值的话；那么插入语句：
     * update tb_brand set name=? where id=?;
     */
    @Test
    public void updateByPrimaryKeySelective() {
        TbBrand brand = new TbBrand();
        brand.setId(23L);
        brand.setName("i hate jbl");
        brandMapper.updateByPrimaryKeySelective(brand);
    }
    @Test
    public void selectAll() {
        List<TbBrand> list = brandMapper.selectAll();
        for (TbBrand tbBrand : list) {
            System.out.println(tbBrand);
        }
    }

    @Test
    public void selectByPrimaryKey() {
        TbBrand tbBrand = brandMapper.selectByPrimaryKey(1L);
        System.out.println(tbBrand);
    }

    @Test
    public void select() {
        TbBrand param = new TbBrand();
        param.setFirstChar("C");
        List<TbBrand> list = brandMapper.select(param);
        for (TbBrand tbBrand : list) {
            System.out.println(tbBrand);
        }
    }

    @Test
    public void deleteByPrimaryKey() {
        brandMapper.deleteByPrimaryKey(23L);
    }

    @Test
    public void selectByExample() {
        //设置分页,第1页，每页2条
        PageHelper.startPage(1, 2);
        //创建查询对象
        Example example = new Example(TbBrand.class);
        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("firstChar", "C");

        List<TbBrand> list = brandMapper.selectByExample(example);

        //转换为分页信息对象
        PageInfo<TbBrand> pageInfo = new PageInfo<>(list);

        System.out.println("总记录数为：" + pageInfo.getTotal());
        System.out.println("总页数为：" + pageInfo.getPages());
        System.out.println("当前页号为：" + pageInfo.getPageNum());
        System.out.println("页大小为：" + pageInfo.getPageSize());

        for (TbBrand brand : pageInfo.getList()) {
            System.out.println(brand);
        }

    }

    //批量新增
    @Test
    public void insertList(){
        List<TbBrand> list = new ArrayList<>();
        TbBrand brand = new TbBrand();
        brand.setName("test1");
        brand.setFirstChar("T");
        list.add(brand);

        brand = new TbBrand();
        brand.setName("test2");
        brand.setFirstChar("T");
        list.add(brand);

        brandMapper.insertList(list);
    }

    //批量删除
    @Test
    public void deleteByIds(){
        Long[] ids = {26L, 27L};
        //StringUtils.join(ids, ",") 表示将数组所有元素使用,连接
        brandMapper.deleteByIds(StringUtils.join(ids, ","));
    }
}