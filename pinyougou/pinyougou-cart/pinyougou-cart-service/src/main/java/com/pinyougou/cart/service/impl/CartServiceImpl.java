package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.vo.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    //品优购系统的购物车在redis中的键名
    private static final String REDIS_CART_LIST = "CART_LIST";
    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> addItemToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //商品是否存在、是否合法。
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!"1".equals(item.getStatus())) {
            throw new RuntimeException("商品非法");
        }
        Cart cart = findCartInCartListBySellerId(cartList, item.getSellerId());
        if(cart == null) {
            //1. 商品对应的商家(cart)不存在在购物车列表（cartList)
            //   创建对应的商家（cart)的orderItemList属性添加一个订单商品（orderItem)；将该商家加入到购物车列表
            cart = new Cart();
            cart.setSellerId(item.getSellerId());
            cart.setSellerName(item.getSeller());

            //订单商品列表
            List<TbOrderItem> orderItemList = new ArrayList<>();
            TbOrderItem orderItem = createOrderItem(item, num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);

            cartList.add(cart);
        } else {
            //2. 商品对应的商家（cart）存在在购物车列表（cartList)
            TbOrderItem orderItem = findOrderItemByItemId(cart.getOrderItemList(), itemId);
            if(orderItem != null) {
                //   2.1. 商品存在在商家对应的订单商品列表（orderItemList）
                //   2.1.1. 将订单商品的购买数量叠加；重新计算订单商品的总价
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(orderItem.getPrice()*orderItem.getNum());

                //   2.1.2. 如果订单商品的购买数量为小于1的时候需要将该订单商品从购物车对象cart的orderItemList中删除
                if (orderItem.getNum() < 1) {
                    cart.getOrderItemList().remove(orderItem);
                }
                //   2.1.3. 如果购物车对象cart中的orderItemList的大小为0的时候，需要将该购物车对象cart从购物车列表cartList中删除
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            } else {
                //   2.2. 商品不存在在商家对应的订单商品列表（orderItemList）；创建一个订单商品对象加入到当前的购物车对象cart中
                orderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            }
        }
        return cartList;
    }

    @Override
    public List<Cart> findCartListByUsername(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps(REDIS_CART_LIST).get(username);
        if (cartList != null) {
            return cartList;
        }
        return new ArrayList<>();
    }

    @Override
    public void saveCartListInRedisByUsername(List<Cart> newCartList, String username) {
        redisTemplate.boundHashOps(REDIS_CART_LIST).put(username, newCartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList1) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                addItemToCartList(cartList2, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList2;
    }

    /**
     * 根据商品sku id从订单商品列表中查询订单商品
     * @param orderItemList 订单商品列表
     * @param itemId 商品sku id
     * @return 订单商品
     */
    private TbOrderItem findOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (itemId.equals(orderItem.getItemId())) {
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 构造订单商品
     * @param item 商品sku
     * @param num 购买数量
     * @return 订单商品orderItem
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setNum(num);
        orderItem.setTitle(item.getTitle());
        orderItem.setPrice(item.getPrice());
        orderItem.setPicPath(item.getImage());
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setSellerId(item.getSellerId());
        //总价格 = 单价*购买数量
        orderItem.setTotalFee(orderItem.getPrice()*orderItem.getNum());

        return orderItem;
    }

    /**
     * 根据商家id查询购物车列表中是否有该商家对应的购物车对象cart
     * @param cartList 购物车列表
     * @param sellerId 商家id
     * @return 购物车对象cart
     */
    private Cart findCartInCartListBySellerId(List<Cart> cartList, String sellerId) {
        if (cartList != null && cartList.size() > 0) {
            for (Cart cart : cartList) {
                if (sellerId.equals(cart.getSellerId())) {
                    return cart;
                }
            }
        }
        return null;
    }
}
