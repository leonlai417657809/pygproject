package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.Goods;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Transactional
@Service
public class GoodsServiceImpl extends BaseServiceImpl<TbGoods> implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsDescMapper goodsDescMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private SellerMapper sellerMapper;

    @Autowired
    private ItemCatMapper itemCatMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Override
    public PageInfo<TbGoods> search(Integer pageNum, Integer pageSize, TbGoods goods) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbGoods.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //查询非删除状态的数据
        criteria.andNotEqualTo("isDelete",1);

        //商家
        if (StringUtils.isNotBlank(goods.getSellerId())) {
            criteria.andEqualTo("sellerId", goods.getSellerId());
        }

        //审核状态
        if (StringUtils.isNotBlank(goods.getAuditStatus())) {
            criteria.andEqualTo("auditStatus", goods.getAuditStatus());
        }

        //商品名称模糊查询
        if (StringUtils.isNotBlank(goods.getGoodsName())) {
            criteria.andLike("goodsName", "%" + goods.getGoodsName() + "%");
        }

        List<TbGoods> list = goodsMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public void addGoods(Goods goods) {
        //保存商品基本信息
        add(goods.getGoods());

        //int i = 1/0;

        //保存商品描述信息
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        goodsDescMapper.insertSelective(goods.getGoodsDesc());

        //保存商品sku列表信息
        saveItemList(goods);
    }


    @Override
    public Goods findGoodsById(Long id) {
        Goods goods =new Goods();

        //根据spu id查询基本信息
        goods.setGoods(findOne(id));

        //根据spu id查询描述信息
        goods.setGoodsDesc(goodsDescMapper.selectByPrimaryKey(id));

        //根据spu id查询sku列表信息
        //sql -> select * from tb_item where goods_id=?
        TbItem item = new TbItem();
        item.setGoodsId(id);
        List<TbItem> itemList = itemMapper.select(item);

        goods.setItemList(itemList);

        return goods;
    }

    @Override
    public void updateGoods(Goods goods) {


        //- 更新基本信息
        //如果修改了则需要重新审核，也就是修改审核状态为0
        goods.getGoods().setAuditStatus("0");
        goodsMapper.updateByPrimaryKeySelective(goods.getGoods());

        //- 更新描述信息
        goodsDescMapper.updateByPrimaryKeySelective(goods.getGoodsDesc());

        //- 更新sku列表（先删除，再保存）
        //根据商品spu id删除其对应的所有的sku
        //DELETE FROM tb_item WHERE goods_id=?
        TbItem tbItem = new TbItem();
        tbItem.setGoodsId(goods.getGoods().getId());
        itemMapper.delete(tbItem);

        //保存sku
        saveItemList(goods);
    }

    @Override
    public void updateStatus(String status, Long[] ids) {
        /**
         * -- 根据商品spu id批量更新商品spu的状态为 审核中
         * UPDATE tb_goods SET audit_status = '1' WHERE id IN (?,?...)
         */
        //要更新的数据
        TbGoods goods = new TbGoods();
        goods.setAuditStatus(status);

        //查询条件
        Example example = new Example(TbGoods.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));//property里面的id对应TbGoods.java类里面的属性id

        //批量更新商品的审核状态
        goodsMapper.updateByExampleSelective(goods,example);

        //更新商品sku的启用状态
        //sql -> update tb_item set status=? where goods_id in(?, ?,...)
        TbItem tbItem = new TbItem();
        //若前端传来审核通过:status为2
        if("2".equals(status)){
            //审核通过；sku需要启用
            tbItem.setStatus("1");
        }else{
            //审核不通过；sku不需要启用
            tbItem.setStatus("0");
        }
        Example itemExample = new Example(TbItem.class);
        itemExample.createCriteria().andIn("goodsId",Arrays.asList(ids));

        itemMapper.updateByExampleSelective(tbItem,itemExample);
    }

    @Override
    public void deleteGoodsByIds(Long[] ids) {
        //根据商品spu id数组更新商品spu 的isDelete属性值为1
        //update tb_goods set is_delete='1' where id in(?,?...)
        TbGoods goods = new TbGoods();
        goods.setIsDelete("1");

        Example example = new Example(TbGoods.class);
        example.createCriteria().andIn("id",Arrays.asList(ids));

        goodsMapper.updateByExampleSelective(goods,example);
    }

    @Override
    public List<TbItem> findItemListByGoodsIds(Long[] ids) {
        /**
         * -- 根据spu id数组查询其对应的启用状态的sku
         * select * from tb_item where status='1' and goods_id in(?,?...)
         */
        Example example = new Example(TbItem.class);
        example.createCriteria().andEqualTo("status","1")
                                .andIn("goodsId",Arrays.asList(ids));
        return itemMapper.selectByExample(example);
    }

    @Override
    public Goods findGoodsByIdAndStatus(Long goodsId, String itemStatus) {
        Goods goods = new Goods();

        //根据spu id查询基本信息
        goods.setGoods(findOne(goodsId));

        //根据spu id查询描述信息
        goods.setGoodsDesc(goodsDescMapper.selectByPrimaryKey(goodsId));

        //根据spu id查询sku列表信息
        //sql -> select * from tb_item where goods_id=? and status='1' order by is_defautl desc
        Example example = new Example(TbItem.class);
        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("goodsId", goodsId);
        //调用方法，itemStatus可能传空，因此需要判空
        if (itemStatus != null) {
            criteria.andEqualTo("status", itemStatus);
        }
        example.orderBy("isDefault").desc();

        List<TbItem> itemList = itemMapper.selectByExample(example);
        goods.setItemList(itemList);

        return goods;
    }

    //调用方法，itemStatus可能传空，因此需要判空

    /**
     * 保存商品sku列表信息
     * @param goods 商品信息：基本、描述、sku列表
     */
    private void saveItemList(Goods goods) {
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            //启用规格
            if (goods.getItemList() != null && goods.getItemList().size() > 0) {
                for (TbItem tbItem : goods.getItemList()) {

                    //标题
                    String title = goods.getGoods().getGoodsName();
                    //获取规格选项
                    Map<String, String> specMap = JSON.parseObject(tbItem.getSpec(), Map.class);
                    for (Map.Entry<String, String> entry : specMap.entrySet()) {
                        title += " " + entry.getValue();
                    }
                    tbItem.setTitle(title);
                    setItemValue(goods, tbItem);

                    //保存到数据库
                    itemMapper.insertSelective(tbItem);
                }
            }
        } else {
            //不启用规格
            TbItem tbItem = new TbItem();

            //价格：从spu的价格中复制而来
            tbItem.setPrice(goods.getGoods().getPrice());
            //库存：可以指定9999
            tbItem.setNum(9999);
            //是否启用：不启用，0
            tbItem.setStatus("0");
            //是否默认：默认，1
            tbItem.setIsDefault("1");
            //规格：{}
            tbItem.setSpec("{}");

            tbItem.setTitle(goods.getGoods().getGoodsName());

            //设置商品sku的数据
            setItemValue(goods, tbItem);

            itemMapper.insertSelective(tbItem);

        }

    }

    //由Ctrl+Alt+M 抽取方法而来
    private void setItemValue(Goods goods, TbItem tbItem) {
        //图片；获取spu描述中图片的第1张
        if (StringUtils.isNotBlank(goods.getGoodsDesc().getItemImages())) {
            //将spu描述中图片json字符串转换为List
            List<Map> images = JSONArray.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
            if (images != null && images.size() > 0) {
                tbItem.setImage(images.get(0).get("url")+"");
            }
        }

        //卖家
        TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        tbItem.setSellerId(seller.getSellerId());
        tbItem.setSeller(seller.getName());

        //分类；spu的第3级分类id
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
        tbItem.setCategoryid(itemCat.getId());
        tbItem.setCategory(itemCat.getName());

        //品牌
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        tbItem.setBrand(brand.getName());

        tbItem.setGoodsId(goods.getGoods().getId());

        tbItem.setCreateTime(new Date());
        tbItem.setUpdateTime(tbItem.getCreateTime());
    }

}
