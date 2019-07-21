package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Override
    public Map<String, Object> search(Map<String, Object> searchMap) {
        Map<String, Object> resultMap = new HashMap<>();

        //创建查询条件构造对象
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();

        //没有搜索条件的时候查询全部
        builder.withQuery(QueryBuilders.matchAllQuery());

        //是否要高亮
        boolean highlight = false;

        if (searchMap != null) {
            //搜索关键字
            String keyword = searchMap.get("keyword") + "";
            if (StringUtils.isNotBlank(keyword)) {
                //在标题、分类、品牌、商家名称4个域中都查询keyword
                builder.withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "category", "brand", "seller"));

                //设置高亮
                highlight = true;
                //设置高亮的域名和高亮的起始、结束标签
                HighlightBuilder.Field highlightField = new HighlightBuilder.Field("title")
                        .preTags("<span style='color:red'>")
                        .postTags("</span>");
                builder.withHighlightFields(highlightField);
            }
        }

        //获取查询对象
        NativeSearchQuery searchQuery = builder.build();

        //查询
        AggregatedPage<TbItem> pageResult;
        if (highlight) {
            pageResult = esTemplate.queryForPage(searchQuery, TbItem.class, new SearchResultMapper() {
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                    List<T> itemList = new ArrayList<>();

                    //将符合本次查询条件的所有sku数据添加到上述列表；并且设置高亮标题
                    for (SearchHit hit : searchResponse.getHits()) {
                        //hit.getSourceAsString()//sku商品的json格式字符串
                        TbItem tbItem = JSON.parseObject(hit.getSourceAsString(), TbItem.class);


                        //获取高亮的标题数据
                        HighlightField highlightField = hit.getHighlightFields().get("title");
                        if (highlightField != null && highlightField.getFragments().length > 0) {
                            StringBuilder sb = new StringBuilder();
                            for (Text fragment : highlightField.getFragments()) {
                                //高亮片段fragment
                                sb.append(fragment.toString());
                            }
                            tbItem.setTitle(sb.toString());
                        }

                        itemList.add((T)tbItem);
                    }

                    return new AggregatedPageImpl<>(itemList, pageable, searchResponse.getHits().getTotalHits());
                }

            });
        } else {
            //不需要高亮
            pageResult = esTemplate.queryForPage(searchQuery, TbItem.class);
        }

        //商品列表
        resultMap.put("itemList", pageResult.getContent());

        return resultMap;
    }
}
