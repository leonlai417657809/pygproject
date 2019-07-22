package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import com.pinyougou.search.dao.ItemDao;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.elasticsearch.index.query.Operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Autowired
    private ItemDao itemDao;


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
            String keywords = searchMap.get("keywords") + "";
            if (StringUtils.isNotBlank(keywords)) {
                //在标题、分类、品牌、商家名称4个域中都查询keyword
                builder.withQuery(QueryBuilders.multiMatchQuery(keywords, "title", "category", "brand", "seller").operator(Operator.AND));

                //设置高亮
                highlight = true;
                //设置高亮的域名和高亮的起始、结束标签
                HighlightBuilder.Field highlightField = new HighlightBuilder.Field("title")
                        .preTags("<span style='color:red'>")
                        .postTags("</span>");
                builder.withHighlightFields(highlightField);
            }


        //设置过滤查询（创建组合查询构造对象）
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //商品分类
        String category = searchMap.get("category") + "";
        if(StringUtils.isNotBlank(category)){
            boolQuery.must(QueryBuilders.termQuery("category",category));
        }

        //品牌
        String brand = searchMap.get("brand") + "";
        if(StringUtils.isNotBlank(brand)){
            boolQuery.must(QueryBuilders.termQuery("brand",brand));
        }

        //规格
        if (searchMap.get("spec")!=null) {
            //嵌套域 = specMap.机身内存.keyword
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            for (Map.Entry<String, String> entry : specMap.entrySet()) {
                String field = "specMap." + entry.getKey() + ".keyword";
                NestedQueryBuilder nestedQuery =
                        QueryBuilders.nestedQuery("specMap", QueryBuilders.matchQuery(field, entry.getValue()), ScoreMode.Max);
                boolQuery.must(nestedQuery);
            }
        }

        //价格过滤查询
        String priceStr = searchMap.get("price") + "";
        if(StringUtils.isNotBlank(priceStr)){
            String[] prices = priceStr.split("-");

            //价格下限 gte => greater than & equal to 大于等于第一个元素prices[0]
            boolQuery.must(QueryBuilders.rangeQuery("price").gte(prices[0]));

            //价格上限 lt=>less than 小于第二个元素prices[1]
            if(!"*".equals(prices[1])){
                boolQuery.must(QueryBuilders.rangeQuery("price").lt(prices[1]));
            }
        }

        builder.withFilter(boolQuery);
        }

        //设置分页
        //页号
        int pageNo = 1;
        String pageNoStr = searchMap.get("pageNo") + "";
        if(StringUtils.isNotBlank(pageNoStr)){
            pageNo = Integer.parseInt(pageNoStr);
        }
        //页大小
        int pageSize = 20;
        String pageSizeStr = searchMap.get("pageSize") + "";
        if(StringUtils.isNotBlank(pageSizeStr)){
            pageSize = Integer.parseInt(pageSizeStr);
        }

        builder.withPageable(PageRequest.of(pageNo - 1,pageSize));

        //设置排序
        String sortField = searchMap.get("sortField") + "";
        String sort = searchMap.get("sort") + "";
        if(StringUtils.isNotBlank(sortField) && StringUtils.isNotBlank(sort)) {
            FieldSortBuilder sortBuilder = SortBuilders.fieldSort(sortField).order("DESC".equals(sort)? SortOrder.DESC : SortOrder.ASC);
            builder.withSort(sortBuilder);
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
        //总页数
        resultMap.put("totalPages",pageResult.getTotalPages());
        //总记录数
        resultMap.put("total",pageResult.getTotalElements());

        return resultMap;
    }

    @Override
    public void importItemList(List<TbItem> itemList) {
        itemDao.saveAll(itemList);
    }

    @Override
    public void deleteItemByIds(Long[] goodsIds) {
        /*
        if (goodsIds != null && goodsIds.length > 0) {
            for (Long goodsId : goodsIds) {
                TbItem tbItem = new TbItem();
                tbItem.setGoodsId(goodsId);
                itemDao.delete(tbItem);
            }
        }
*/
        itemDao.deleteByGoodsIdIn(goodsIds);
    }
}
