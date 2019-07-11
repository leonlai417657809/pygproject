package com.pinyougou.sellergoods.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.Specification;

import java.util.List;
import java.util.Map;

public interface SpecificationService extends BaseService<TbSpecification> {
    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param specification 搜索条件
     * @return 分页信息
     */
    PageInfo<TbSpecification> search(Integer pageNum, Integer pageSize, TbSpecification specification);

    /**
     * 新增
     * @param specification 规格及其选项列表
     */
    void addSpecification(Specification specification);

    /**
     * 根据主键查询
     * @param id 主键
     * @return 规格及其选项列表
     */
    Specification findSpecification(Long id);

    /**
     * 修改
     * @param specification 规格及其选项列表
     */
    void updateSpecification(Specification specification);

    /**
     * 根据主键数组批量删除
     * @param ids 主键数组
     */
    void deleteSpecificationByIds(Long[] ids);

    /**
     * 获取规格下拉框数据；数据结构如返回结果
     * @return [{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
     */
    List<Map<String, Object>> selectOptionList();
}
