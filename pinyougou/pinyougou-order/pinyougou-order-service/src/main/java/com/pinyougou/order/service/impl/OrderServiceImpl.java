package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.OrderItemMapper;
import com.pinyougou.mapper.OrderMapper;
import com.pinyougou.mapper.PayLogMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.Cart;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl extends BaseServiceImpl<TbOrder> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayLogMapper payLogMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    //品优购系统的购物车在redis中的键名
    private static final String REDIS_CART_LIST = "CART_LIST";

    @Override
    public PageInfo<TbOrder> search(Integer pageNum, Integer pageSize, TbOrder order) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbOrder.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //模糊查询
        /**if (StringUtils.isNotBlank(order.getProperty())) {
            criteria.andLike("property", "%" + order.getProperty() + "%");
        }*/

        List<TbOrder> list = orderMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public String addOrder(TbOrder order) {
        String outTradeNo = "";
        //1. 查询当前登录用户的购物车列表（cartList）
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps(REDIS_CART_LIST).get(order.getUserId());

        //2. 遍历购物车列表的每一个购物车对象（cart，对应订单）；遍历cart对应的orderItemList的数据并保存
        TbOrder tbOrder = null;
        //订单号
        String orderId = "";
        //本次交易对应的订单号列表
        String orderIds = "";
        //本次交易的总金额
        double totalFee = 0;

        for (Cart cart : cartList) {
        tbOrder = new TbOrder();
            //订单id
            orderId = idWorker.nextId()+"";
            tbOrder.setOrderId(orderId);
            tbOrder.setSourceType(order.getSourceType());
            tbOrder.setUserId(order.getUserId());
            tbOrder.setPaymentType(order.getPaymentType());
            tbOrder.setReceiverAreaName(order.getReceiverAreaName());
            tbOrder.setReceiverMobile(order.getReceiverMobile());
            tbOrder.setReceiver(order.getReceiver());
            tbOrder.setSellerId(cart.getSellerId());
            tbOrder.setCreateTime(new Date());
            tbOrder.setUpdateTime(tbOrder.getCreateTime());
            //订单状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价',
            tbOrder.setStatus("1");
            //本笔订单的总金额 = 所有该订单的订单商品的总金额之和
            double payment = 0;
        //处理订单商品
            for(TbOrderItem orderItem : cart.getOrderItemList()){
                orderItem.setId(idWorker.nextId());
                orderItem.setOrderId(orderId);
                orderItemMapper.insertSelective(orderItem);

                payment += orderItem.getTotalFee();
            }
            tbOrder.setPayment(payment);
            //累加订单号
            if(orderIds.length()> 0){
                orderIds += "," +orderId;
            }else {
                orderIds = orderId;
            }
            //累计本次交易的总金额
            totalFee += payment;
            //保存订单
            add(tbOrder);
        }
        //3. 保存支付日志，微信支付 的支付状态应该为未支付，货到付款支付状态为支付成功
        TbPayLog tbPayLog = new TbPayLog();
        outTradeNo = idWorker.nextId()+"";
        tbPayLog.setOutTradeNo(outTradeNo);
        tbPayLog.setUserId(order.getUserId());
        tbPayLog.setPayType(order.getPaymentType());
        tbPayLog.setCreateTime(new Date());
        if("1".equals(order.getPaymentType())){
            //如果为微信支付 未支付0
            tbPayLog.setTradeState("0");
        }else{
            //货到付款 支付状态为1，已支付
            tbPayLog.setTradeState("1");
            tbPayLog.setPayTime(new Date());
        }

        //订单号
        tbPayLog.setOrderList(orderIds);
        //本次要支付的总金额 = 所有订单的金额之和；在电商开发中；如果涉及到金钱，那么字段一般情况下都需要使用整型；单位精确到分
        tbPayLog.setTotalFee((long) (totalFee*100));

        payLogMapper.insertSelective(tbPayLog);
        //4. 清空当前登录用户的购物车列表
        redisTemplate.boundHashOps(REDIS_CART_LIST).delete(order.getUserId());
        //5. 返回支付日志id,即支付订单号id,如果是货到付款则支付状态为已支付
        return outTradeNo;
    }

}
