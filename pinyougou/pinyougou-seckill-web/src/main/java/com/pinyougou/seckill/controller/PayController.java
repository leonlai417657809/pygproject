package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.PayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/pay")
@RestController
public class PayController {

    @Reference(timeout = 10000)
    private SeckillOrderService orderService;

    @Reference(timeout = 3000)
    private PayService payService;

    /**
     * 根据订单id获取二维码链接等信息
     * @param outTradeNo 订单id
     * @return code_url支付二维码链接地址、totalFee总金额、outTradeNo 交易号、result_code操作结果
     */
    @GetMapping("/createNative")
    public Map<String, String> createNative(String outTradeNo){
        try {
            //查询秒杀订单
            TbSeckillOrder seckillOrder = orderService.getSeckillOrderInRedisByOrderId(outTradeNo);

            if (seckillOrder != null) {
                //总金额
                String totalFee = (long)(seckillOrder.getMoney()*100)+"";
                //调用业务方法
                return payService.createNative(outTradeNo, totalFee);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    /**
     * 根据秒杀订单id查询支付状态
     * @param outTradeNo 秒杀订单id
     * @return 操作结果
     */
    @GetMapping("/queryPayStatus")
    public Result queryPayStatus(String outTradeNo){
        Result result = Result.fail("查询支付状态失败！");

        try {
            //1分钟未支付，表示支付超时
            int count = 0;
            while (true){

                //查询支付状态
                Map<String, String> resultMap = payService.queryPayStatus(outTradeNo);

                if (resultMap == null) {
                    break;
                }

                if ("SUCCESS".equals(resultMap.get("trade_state"))) {
                    //支付成功，需要更新秒杀订单、订单的状态
                    orderService.saveSeckillOrderInRedisToDb(outTradeNo, resultMap.get("transaction_id"));
                    result = Result.ok("查询支付状态成功！");
                    break;
                }
                count++;

                if (count > 5) {
                    result = Result.fail("支付超时");

                    //关闭微信订单
                    Map<String, String> map = payService.closeOrder(outTradeNo);
                    if ("ORDERPAID".equals(map.get("err_code"))) {
                        //支付成功，需要更新秒杀订单、订单的状态
                        orderService.saveSeckillOrderInRedisToDb(outTradeNo, map.get("transaction_id"));
                        result = Result.ok("查询支付状态成功！");
                        break;
                    }

                    //将redis中订单删除并库存加回
                    orderService.deleteSeckillOrder(outTradeNo);

                    break;
                }

                //每隔3秒
                Thread.sleep(3000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    }
}
