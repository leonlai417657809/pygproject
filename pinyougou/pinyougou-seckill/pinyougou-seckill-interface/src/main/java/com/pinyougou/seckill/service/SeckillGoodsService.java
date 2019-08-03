package com.pinyougou.seckill.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.service.BaseService;

import java.util.List;

public interface SeckillGoodsService extends BaseService<TbSeckillGoods> {
    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param seckillGoods 搜索条件
     * @return 分页信息
     */
    PageInfo<TbSeckillGoods> search(Integer pageNum, Integer pageSize, TbSeckillGoods seckillGoods);

    /**
     * 查询库存大于0，已审核，开始时间小于等于当前时间，结束时间大于当前时间的秒杀商品并根据开始时间升序排序
     * @return 秒杀商品列表
     */
    List<TbSeckillGoods> findList();

    /**
     * 根据商品Id到redis中查询商品
     * @param id 秒杀商品id
     * @return 秒杀商品
     */
    TbSeckillGoods findOneInRedis(Long id);
}
