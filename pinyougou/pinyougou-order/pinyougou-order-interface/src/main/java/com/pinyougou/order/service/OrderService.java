package com.pinyougou.order.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.service.BaseService;

import java.util.List;

public interface OrderService extends BaseService<TbOrder> {
    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param order 搜索条件
     * @return 分页信息
     */
    PageInfo<TbOrder> search(Integer pageNum, Integer pageSize, TbOrder order);

    /**
     * 保存购物车列表数据为订单的相关数据
     * @param order 订单基本信息
     * @return 支付日志id（交易编号）
     */
    String addOrder(TbOrder order);
}
