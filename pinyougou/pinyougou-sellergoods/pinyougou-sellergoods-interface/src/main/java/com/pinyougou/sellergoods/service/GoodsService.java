package com.pinyougou.sellergoods.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.Goods;

import java.util.List;

public interface GoodsService extends BaseService<TbGoods> {
    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param goods 搜索条件
     * @return 分页信息
     */
    PageInfo<TbGoods> search(Integer pageNum, Integer pageSize, TbGoods goods);

    /**
     * 新增
     * @param goods 商品vo（商品基本、描述、sku列表）
     */
    void addGoods(Goods goods);

    /**
     * 根据主键查询
     * @param id 主键；spu id
     * @return 商品vo：基本、描述、sku列表
     */
    Goods findGoodsById(Long id);

    /**
     * 修改
     * @param goods 商品vo：基本、描述、sku列表
     */
    void updateGoods(Goods goods);

    /**
     * 批量更新商品spu的状态
     * @param status 商品审核状态
     * @param ids 商品spu id数组
     */
    void updateStatus(String status, Long[] ids);

    /**
     * 根据主键数组批量逻辑删除
     * @param ids 主键数组
     */
    void deleteGoodsByIds(Long[] ids);

    /**
     * 根据spu id数组查询其对应的启用状态的sku
     * @param ids spu id数组
     * @return sku商品列表
     */
    List<TbItem> findItemListByGoodsIds(Long[] ids);

    /**
     * 根据商品spu id查询商品基本、描述、sku列表（已启用，根据是否默认降序排序）
     * @param goodsId 商品spu id
     * @param itemStatus 商品sku的状态
     * @return 商品信息vo
     */
    Goods findGoodsByIdAndStatus(Long goodsId, String itemStatus);
}
