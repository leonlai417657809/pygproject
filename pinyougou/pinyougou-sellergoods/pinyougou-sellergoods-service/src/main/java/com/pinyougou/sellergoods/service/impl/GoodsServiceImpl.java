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
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

        //模糊查询
        /**if (StringUtils.isNotBlank(goods.getProperty())) {
            criteria.andLike("property", "%" + goods.getProperty() + "%");
        }*/

        List<TbGoods> list = goodsMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public void addGoods(Goods goods) {
        //保存商品基本信息
        add(goods.getGoods());

        //保存商品描述信息
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        goodsDescMapper.insertSelective(goods.getGoodsDesc());

        //保存商品sku列表信息
        saveItemList(goods);
    }

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
