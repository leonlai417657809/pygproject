package com.pinyougou.es;

import com.alibaba.fastjson.JSON;
import com.pinyougou.es.dao.ItemElasticSearchDao;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.TbItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext*.xml")
public class ItemImport2ElasticSearch {
    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ItemElasticSearchDao itemElasticSearchDao;

    @Test
    public void itemImport(){
        //- 查询已启用的sku商品列表；
        TbItem param = new TbItem();
        param.setStatus("1");
        List<TbItem> itemList = itemMapper.select(param);

        //- 遍历每个sku，将规格Json格式字符串转换为map设置到specMap属性中；
        for (TbItem tbItem : itemList) {
            Map specMap = JSON.parseObject(tbItem.getSpec(),Map.class);
            tbItem.setSpecMap(specMap);
        }
        //- 利用dao保存数据列表到es中。
        itemElasticSearchDao.saveAll(itemList);
    }
}
