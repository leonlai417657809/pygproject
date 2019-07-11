package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.SpecificationMapper;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.sellergoods.service.SpecificationService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.Specification;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationServiceImpl extends BaseServiceImpl<TbSpecification> implements SpecificationService {

    @Autowired
    private SpecificationMapper specificationMapper;

    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    @Override
    public PageInfo<TbSpecification> search(Integer pageNum, Integer pageSize, TbSpecification specification) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbSpecification.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //模糊查询
        if (StringUtils.isNotBlank(specification.getSpecName())) {
            criteria.andLike("specName", "%" + specification.getSpecName() + "%");
        }

        List<TbSpecification> list = specificationMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public void addSpecification(Specification specification) {
        //保存规格；在执行下面一行代码之后；通用Mapper已经将规格实体的主键回填了。
        add(specification.getSpecification());

        //保存规格选项列表
        for(TbSpecificationOption tbSpecificationOption: specification.getSpecificationOptionList()){
            //对所有的选项设置规格id
            tbSpecificationOption.setSpecId(specification.getSpecification().getId());
        }
        //批量新增
        specificationOptionMapper.insertList(specification.getSpecificationOptionList());
    }

    @Override
    public Specification findSpecification(Long id) {
        Specification specification = new Specification();

        //根据规格id查询规格
        specification.setSpecification(findOne(id));

        //根据规格id查询规格选项列表
        //SELECT * FROM tb_specification_option WHERE spec_id = ?
        TbSpecificationOption tbSpecificationOption = new TbSpecificationOption();
        tbSpecificationOption.setSpecId(id);
        List<TbSpecificationOption> specificationOptionList = specificationOptionMapper.select(tbSpecificationOption);
        specification.setSpecificationOptionList(specificationOptionList);

        return  specification;
    }

    @Override
    public void updateSpecification(Specification specification) {
        //1. 更新规格；
        update(specification.getSpecification());

        //2. 根据规格id删除其对应的所有规格选项；
        // delete from tb_specification_option where spec_id=?
        TbSpecificationOption param = new TbSpecificationOption();
        param.setSpecId(specification.getSpecification().getId());
        specificationOptionMapper.delete(param);

        //3. 保存规格选项
        for(TbSpecificationOption tbSpecificationOption: specification.getSpecificationOptionList()){
            //对所有的选项设置规格id
            tbSpecificationOption.setSpecId(specification.getSpecification().getId());
        }
        //批量新增
        specificationOptionMapper.insertList(specification.getSpecificationOptionList());
    }

    @Override
    public void deleteSpecificationByIds(Long[] ids) {
        //根据选择了的那些规格id批量删除规格；
        // delete from tb_specification where id in(?,?...)
        deleteByIds(ids);

        //根据选择了的那些规格id批量删除规格选项；
        // delete from tb_specification_option where spec_id in(?,?...)
        Example example = new Example(TbSpecificationOption.class);
        example.createCriteria().andIn("specId", Arrays.asList(ids));
        specificationOptionMapper.deleteByExample(example);
    }

    @Override
    public List<Map<String, Object>> selectOptionList() {
        return specificationMapper.selectOptionList();
    }

}
