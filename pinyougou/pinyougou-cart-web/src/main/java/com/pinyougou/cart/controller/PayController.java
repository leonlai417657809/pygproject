package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.PayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/pay")
@RestController
public class PayController {

    @Reference
    private OrderService orderService;

    @Reference(timeout = 3000)
    private PayService payService;

    /**
     * 根据支付日志id获取二维码链接等信息
     * @param outTradeNo 支付日志id
     * @return code_url支付二维码链接地址、totalFee总金额、outTradeNo 交易号、result_code操作结果
     */
    @GetMapping("/createNative")
    public Map<String, String> createNative(String outTradeNo){
        try {
            //查询支付日志
            TbPayLog payLog = orderService.findPayLogByOutTradeNo(outTradeNo);

            if (payLog != null) {
                //总金额
                String totalFee = payLog.getTotalFee().toString();
                //调用业务方法
                return payService.createNative(outTradeNo, totalFee);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    /**
     * 根据支付日志id查询支付状态
     * @param outTradeNo 支付日志id
     * @return 操作结果
     */
    @GetMapping("/queryPayStatus")
    public Result queryPayStatus(String outTradeNo){
        Result result = Result.fail("查询支付状态失败！");

        try {
            //3分钟未支付，表示支付超时
            int count = 0;
            while (true){

                //查询支付状态
                Map<String, String> resultMap = payService.queryPayStatus(outTradeNo);

                if (resultMap == null) {
                    break;
                }

                if ("SUCCESS".equals(resultMap.get("trade_state"))) {
                    //支付成功，需要更新支付日志、订单的状态
                    orderService.updateOrderStatus(outTradeNo, resultMap.get("transaction_id"));
                    result = Result.ok("查询支付状态成功！");
                    break;
                }
                count++;

                if (count > 60) {
                    result = Result.fail("支付超时");
                    break;
                }

                //每隔3秒
                Thread.sleep(3000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;

    }
}
