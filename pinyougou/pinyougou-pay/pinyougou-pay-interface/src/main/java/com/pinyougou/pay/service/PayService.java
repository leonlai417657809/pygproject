package com.pinyougou.pay.service;

import java.util.Map;

public interface PayService {
    /**
     * 发送请求到支付系统中统一下单返回需要的数据
     * @param outTradeNo 交易号
     * @param totalFee 金额
     * @return 包含了支付二维码链接等的信息
     */
    Map<String, String> createNative(String outTradeNo, String totalFee);

    /**
     * 根据交易号查询订单支付状态
     * @param outTradeNo 交易号
     * @return 查询结果
     */
    Map<String, String> queryPayStatus(String outTradeNo);

    /**
     * 根据交易号关闭订单
     * @param outTradeNo 订单号
     * @return 操作结果
     */
    Map<String, String> closeOrder(String outTradeNo);
}
