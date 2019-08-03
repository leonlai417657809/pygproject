package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.common.util.HttpClient;
import com.pinyougou.pay.service.PayService;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Service
public class PayServiceImpl implements PayService {

    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;

    @Value("${notifyurl}")
    private String notifyurl;

    @Override
    public Map<String, String> createNative(String outTradeNo, String totalFee) {
        Map<String, String> resultMap = new HashMap<>();
        try {
            //1、 组装请求参数
            Map<String, String> paramMap = new HashMap<>();
            //公众账号ID
            paramMap.put("appid", appid);
            //商户号
            paramMap.put("mch_id", partner);
            //随机字符串
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //签名；可以在转换map为xml的时候自动生成
            //paramMap.put("sign", null);
            //商品描述
            paramMap.put("body", "107品优购");
            //商户订单号
            paramMap.put("out_trade_no", outTradeNo);
            //标价金额
            paramMap.put("total_fee", totalFee);
            //终端IP
            paramMap.put("spbill_create_ip", "127.0.0.1");
            //通知地址
            paramMap.put("notify_url", notifyurl);
            //交易类型
            paramMap.put("trade_type", "NATIVE");

            //2、发送请求
            String signedXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            System.out.println("发送 统一下单 的请求参数为：" + signedXml);
            
            //创建发送对象
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setXmlParam(signedXml);
            httpClient.setHttps(true);
            httpClient.post();

            //获取到数据
            String content = httpClient.getContent();
            System.out.println("发送 统一下单 的返回结果为：" + content);

            Map<String, String> map = WXPayUtil.xmlToMap(content);

            //3、处理结果
            resultMap.put("outTradeNo", outTradeNo);
            resultMap.put("totalFee", totalFee);
            resultMap.put("result_code", map.get("result_code"));
            resultMap.put("code_url", map.get("code_url"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    @Override
    public Map<String, String> queryPayStatus(String outTradeNo) {
        try {
            //1、 组装请求参数
            Map<String, String> paramMap = new HashMap<>();
            //公众账号ID
            paramMap.put("appid", appid);
            //商户号
            paramMap.put("mch_id", partner);
            //随机字符串
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //签名；可以在转换map为xml的时候自动生成
            //paramMap.put("sign", null);
            //商户订单号
            paramMap.put("out_trade_no", outTradeNo);

            //2、发送请求
            String signedXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            System.out.println("发送 查询订单 的请求参数为：" + signedXml);

            //创建发送对象
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setXmlParam(signedXml);
            httpClient.setHttps(true);
            httpClient.post();

            //获取到数据
            String content = httpClient.getContent();
            System.out.println("发送 查询订单 的返回结果为：" + content);

            return WXPayUtil.xmlToMap(content);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Map<String, String> closeOrder(String outTradeNo) {
        try {
            //1、 组装请求参数
            Map<String, String> paramMap = new HashMap<>();
            //公众账号ID
            paramMap.put("appid", appid);
            //商户号
            paramMap.put("mch_id", partner);
            //随机字符串
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //签名；可以在转换map为xml的时候自动生成
            //paramMap.put("sign", null);
            //商户订单号
            paramMap.put("out_trade_no", outTradeNo);

            //2、发送请求
            String signedXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            System.out.println("发送 关闭订单 的请求参数为：" + signedXml);

            //创建发送对象
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            httpClient.setXmlParam(signedXml);
            httpClient.setHttps(true);
            httpClient.post();

            //获取到数据
            String content = httpClient.getContent();
            System.out.println("发送 关闭订单 的返回结果为：" + content);

            return WXPayUtil.xmlToMap(content);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new HashMap<>();

    }
}
