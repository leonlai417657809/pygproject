package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.common.util.CookieUtils;
import com.pinyougou.vo.Cart;
import com.pinyougou.vo.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/cart")
@RestController
public class CartController {

    //在浏览器中品优购项目的购物车列表的cookie的名字
    private static final String COOKIE_CART_LIST = "PYG_CART_LIST";
    //在浏览器中品优购项目的购物车列表的cookie的最大生成时间；1天
    private static final int COOKIE_CART_LIST_MAX_AGE = 3600 * 24;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Reference
    private CartService cartService;

    /**
     * 加入购物车
     * 未登录：将新购物车列表存入cookie
     * 已登录：将新购物车列表存入redis
     * @param itemId 商品sku id
     * @param num 购买数量
     * @return 操作结果
     */
    @GetMapping("/addItemToCartList")
    public Result addItemToCartList(Long itemId, Integer num){
        try {
            //可以设置响应头部属性
            //设置允许可以跨域请求的域名
            response.setHeader("Access-Control-Allow-Origin", "http://item.pinyougou.com");
            //设置允许可以读取cookie中的数据
            response.setHeader("Access-Control-Allow-Credentials", "true");

            //获取当前登录的用户名；因为允许匿名访问，如果为匿名访问的时候用户名为anonymousUser
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            //查询当前购物车列表
            List<Cart> cartList = findCartList();

            //加入购物车列表
            List<Cart> newCartList = cartService.addItemToCartList(cartList, itemId, num);

            if ("anonymousUser".equals(username)) {
                //未登录：将新购物车列表存入cookie
                String cartListJsonStr = JSON.toJSONString(newCartList);
                CookieUtils.setCookie(request, response, COOKIE_CART_LIST, cartListJsonStr, COOKIE_CART_LIST_MAX_AGE, true);
            } else {
                //已登录；将新购物车列表存入redis
                cartService.saveCartListInRedisByUsername(newCartList, username);
            }
            return Result.ok("加入购物车成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.fail("加入购物车失败！");
    }

    /**
     * 查询登录、未登录状态下的购物车列表
     * @return 购物车列表
     */
    @GetMapping("/findCartList")
    public List<Cart> findCartList(){
        //获取当前登录的用户名；因为允许匿名访问，如果为匿名访问的时候用户名为anonymousUser
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        List<Cart> cookieCartList = new ArrayList<>();
        //未登录；购物车数据来自cookie
        String cartListJsonStr = CookieUtils.getCookieValue(request, COOKIE_CART_LIST, true);
        if (StringUtils.isNotBlank(cartListJsonStr)) {
            cookieCartList = JSON.parseArray(cartListJsonStr, Cart.class);
        }
        if ("anonymousUser".equals(username)) {
            return cookieCartList;
        } else {
            //已登录；购物车数据来自redis
            List<Cart> redisCartList = cartService.findCartListByUsername(username);

            if (cookieCartList.size() > 0) {
                //- 将cookie的购物车数据与redis中的购物车进行合并到一个新购物车列表
                redisCartList = cartService.mergeCartList(cookieCartList, redisCartList);
                //- 将新购物车列表保存到redis中
                cartService.saveCartListInRedisByUsername(redisCartList, username);

                //- 将cookie中的数据删除
                CookieUtils.deleteCookie(request, response, COOKIE_CART_LIST);
            }

            return redisCartList;
        }
    }

    /**
     * 获取用户信息
     * @return 用户信息
     */
    @GetMapping("/getUsername")
    public Map<String, Object> getUsername(){
        Map<String, Object> map = new HashMap<>();
        //获取当前登录的用户名；因为允许匿名访问，如果为匿名访问的时候用户名为anonymousUser
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("username", username);

        return map;
    }
}
