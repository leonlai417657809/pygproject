package com.pinyougou.search.activemq.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

/**
 * 接收商品sku列表调用业务方法保存商品sku列表（对每个商品的spec字符串转换为map）到es
 */
public class ItemImportMessageListener extends AbstractAdaptableMessageListener {
    @Autowired
    private ItemSearchService itemSearchService;


    @Override
    public void onMessage(Message message, Session session) throws JMSException {
        if(message instanceof TextMessage){
            //1、接收消息并转换为列表
            TextMessage textMessage = (TextMessage) message;
            List<TbItem> itemList = JSON.parseArray(textMessage.getText(), TbItem.class);

            //2、将列表中的每个sku的spec转换为map并设置到specMap
            for (TbItem tbItem : itemList) {
                Map specMap = JSON.parseObject(tbItem.getSpec(),Map.class);
                tbItem.setSpecMap(specMap);
            }

            //3、保存到es
            itemSearchService.importItemList(itemList);

            System.out.println("同步索引库数据完成。 ");
        }
    }
}
