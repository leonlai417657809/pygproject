package com.pinyougou.item.activemq.listener;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.ItemCatService;
import com.pinyougou.vo.Goods;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 接收商品spu id数组的发布与订阅消息；接收到消息之后更新指定路径下的商品详情静态html页面
 */
public class ItemAdultMessageListener extends AbstractAdaptableMessageListener {
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Reference
    private GoodsService goodsService;

    @Reference
    private ItemCatService itemCatService;

    //存放商品静态页面的路径
    @Value("${ITEM_HTML_PATH}")
    private String ITEM_HTML_PATH;

    @Override
    public void onMessage(Message message, Session session) throws JMSException {
        if(message instanceof Object){
            ObjectMessage objectMessage= (ObjectMessage) message;

            //获取商品spu id数组
            Long[] goodsIds = (Long[]) objectMessage.getObject();

            try {
                if(goodsIds !=null && goodsIds.length > 0){
                    for (Long goodsId : goodsIds) {
                        genHtml(goodsId);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 商品spu id根据其查询商品信息集合freemarker生成静态页面到指定路径
     * @param goodsId 商品spu id
     */
    private void genHtml(Long goodsId) throws Exception {
        Configuration configuration = freeMarkerConfigurer.getConfiguration();
        //模板
        Template template = configuration.getTemplate("item.ftl");

        //数据
        Map<String, Object> dataModel = new HashMap<>();

        //根据商品spu id查询商品基本、描述、sku列表（已启用，根据是否默认降序排序）
        Goods goods = goodsService.findGoodsByIdAndStatus(goodsId, "1");

        //goods 商品基本信息
        dataModel.put("goods", goods.getGoods());
        //goodsDesc 商品描述信息
        dataModel.put("goodsDesc", goods.getGoodsDesc());
        //itemList 商品sku列表；（已启用；需要根据是否默认排序，降序排序）
        dataModel.put("itemList", goods.getItemList());

        //itemCat1 第1级商品分类中文名称
        TbItemCat itemCat1 = itemCatService.findOne(goods.getGoods().getCategory1Id());
        dataModel.put("itemCat1", itemCat1.getName());
        //itemCat2 第2级商品分类中文名称
        TbItemCat itemCat2 = itemCatService.findOne(goods.getGoods().getCategory2Id());
        dataModel.put("itemCat2", itemCat2.getName());
        //itemCat3 第3级商品分类中文名称
        TbItemCat itemCat3 = itemCatService.findOne(goods.getGoods().getCategory3Id());
        dataModel.put("itemCat3", itemCat3.getName());

        //输出
        String fileName = ITEM_HTML_PATH + goodsId + ".html";
        FileWriter fileWriter = new FileWriter(fileName);
        template.process(dataModel, fileWriter);
        fileWriter.close();
    }


}
