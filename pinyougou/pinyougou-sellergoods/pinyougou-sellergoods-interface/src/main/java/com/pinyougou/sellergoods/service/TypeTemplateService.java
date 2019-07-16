package com.pinyougou.sellergoods.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.service.BaseService;

import java.util.List;
import java.util.Map;

public interface TypeTemplateService extends BaseService<TbTypeTemplate> {
    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param typeTemplate 搜索条件
     * @return 分页信息
     */
    PageInfo<TbTypeTemplate> search(Integer pageNum, Integer pageSize, TbTypeTemplate typeTemplate);

    /**
     * 根据分类模版id查询规格及其选项；数据结构如下：
     * [
     *     {"id":27,"text":"网络", "options":[{"id":1,"optionName":"移动3G"}...]},
     *     {"id":32,"text":"机身内存", "options":[{"id":2,"optionName":"16G"}...]}
     * ]
     * @param id 分类模版id
     * @return 规格及其选项
     */
    List<Map> findSpecList(Long id);
}
