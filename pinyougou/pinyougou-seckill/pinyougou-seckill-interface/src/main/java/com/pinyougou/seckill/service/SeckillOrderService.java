package com.pinyougou.seckill.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.service.BaseService;

import java.util.List;

public interface SeckillOrderService extends BaseService<TbSeckillOrder> {
    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param seckillOrder 搜索条件
     * @return 分页信息
     */
    PageInfo<TbSeckillOrder> search(Integer pageNum, Integer pageSize, TbSeckillOrder seckillOrder);

    /**
     * 根据秒杀商品id进行下单
     * @param seckillGoodsId 秒杀商品id
     * @return 操作结果
     */
    String submitOrder(Long seckillGoodsId, String userId) throws Exception;

    /**
     * 根据订单id查询秒杀订单
     * @param outTradeNo 订单id
     * @return 秒杀订单
     */
    TbSeckillOrder getSeckillOrderInRedisByOrderId(String outTradeNo);

    /**
     *  将redis中的订单保存到mysql
     * @param outTradeNo 秒杀订单号
     * @param transactionId 微信订单号
     */
    void saveSeckillOrderInRedisToDb(String outTradeNo, String transactionId);

    /**
     * 将redis中订单删除并库存加回
     * @param outTradeNo 订单号
     */
    void deleteSeckillOrder(String outTradeNo) throws InterruptedException, Exception;
}
