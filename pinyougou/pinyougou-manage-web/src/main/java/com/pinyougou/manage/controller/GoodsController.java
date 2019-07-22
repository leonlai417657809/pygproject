package com.pinyougou.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.vo.Goods;
import com.pinyougou.vo.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/goods")
@RestController
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    @Reference
    private ItemSearchService itemSearchService;

    /**
     * 新增
     * @param goods 商品vo（商品基本、描述、sku列表）
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody Goods goods){
        try {
            //设置商家
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            goods.getGoods().setSellerId(sellerId);

            //设置商品状态为：未审核
            goods.getGoods().setAuditStatus("0");
            goodsService.addGoods(goods);

            return Result.ok("新增成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("新增失败");
    }

    /**
     * 根据主键查询
     * @param id 主键；spu id
     * @return 商品vo：基本、描述、sku列表
     */
    @GetMapping("/findOne/{id}")
    public Goods findOne(@PathVariable Long id){
        return goodsService.findGoodsById(id);
    }

    /**
     * 修改
     * @param goods 商品vo：基本、描述、sku列表
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result update(@RequestBody Goods goods){
        try {
            //老商品
            TbGoods oldGoods = goodsService.findOne(goods.getGoods().getId());
            //获取当前登录的商家
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            if(!sellerId.equals(oldGoods.getSellerId())){
                return Result.fail("操作非法");
            }

            goodsService.updateGoods(goods);
            return Result.ok("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改失败");
    }

    /**
     * 根据主键数组批量删除
     * @param ids 主键数组
     * @return 实体
     */
    @GetMapping("/delete")
    public Result delete(Long[] ids){
        try {
            goodsService.deleteGoodsByIds(ids);
            //删除搜索系统商品数据
            itemSearchService.deleteItemByIds(ids);
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除失败");
    }

    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param goods 搜索条件
     * @return 分页信息
     */
    @PostMapping("/search")
    public PageInfo<TbGoods> search(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                             @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                           @RequestBody TbGoods goods) {

        return goodsService.search(pageNum, pageSize, goods);
    }

    /**
     * 批量更新商品spu的状态
     * @param status 商品审核状态
     * @param ids 商品spu id数组
     * @return 操作结果
     */
    @GetMapping("/updateStatus")
    public Result updateStatus(String status,Long[] ids){
        try {
            goodsService.updateStatus(status,ids);
            if("2".equals(status)){
                //根据spu id数组查询其对应的启用状态的sku
                List<TbItem> itemList = goodsService.findItemListByGoodsIds(ids);
                //更新搜索系统数据
                itemSearchService.importItemList(itemList);
            }
            return Result.ok("提交更新成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("提交更新失败");
    }
}
