package cn.itcast.es;

import cn.itcast.es.dao.ItemDao;
import com.pinyougou.pojo.TbItem;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-es.xml")
public class ElasticSearchTest {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ItemDao itemDao;

    //创建索引和映射
    @Test
    public void createIndexAndMappling(){
        //创建索引
        elasticsearchTemplate.createIndex(TbItem.class);
        //添加映射
        elasticsearchTemplate.putMapping(TbItem.class);
    }

    //新增或者更新
    @Test
    public void save(){
        TbItem item = new TbItem();
        item.setId(579794586729L);
        item.setTitle("Apple/苹果 iPhone XR 全网通4G手机双卡双待 苹果iPhoneXR 苹果xr");
        item.setPrice(5588.00);
        item.setImage("https://img.alicdn.com/imgextra/i2/2616970884/O1CN01VsSoS71IOufN5EYsf_!!2616970884.jpg");
        item.setCategory("手机");
        item.setBrand("Apple");
        item.setSeller("Job");
        item.setGoodsId(1L);
        item.setUpdateTime(new Date());

        //添加规格
        Map<String, String> specMap = new HashMap<>();
        specMap.put("机身内存", "8G");
        specMap.put("屏幕尺寸", "5.5");

        item.setSpecMap(specMap);

        itemDao.save(item);
    }

    //查询所有
    @Test
    public void findAll(){
        Iterable<TbItem> items = itemDao.findAll();
        for (TbItem item : items) {
            System.out.println(item);
        }
    }

    //分页查询所有
    @Test
    public void findAllByPage(){
        PageRequest pageRequest = PageRequest.of(0, 2);

        Page<TbItem> items = itemDao.findAll(pageRequest);
        for (TbItem item : items) {
            System.out.println(item);
        }
    }

    //删除
    @Test
    public void delete(){
        itemDao.deleteById(579794586729L);
    }

    //通配符搜索
    @Test
    public void wildCardQuery(){
        //创建查询构造对象
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        /**
         * 参数1：域名（与实体类中@Field配置的一致）
         */
        queryBuilder.withQuery(QueryBuilders.wildcardQuery("title","手*"));

        //获取查询对象
        NativeSearchQuery query = queryBuilder.build();

        //查询
        AggregatedPage<TbItem> items = elasticsearchTemplate.queryForPage(query, TbItem.class);
        System.out.println("总记录数"+items.getTotalElements());
        System.out.println("总页数"+items.getTotalPages());
        for (TbItem item : items) {
            System.out.println(item);
        }
    }

    //分词搜索
    @Test
    public void matchQuery(){
        //创建查询构造对象
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        /**
         * 参数1：域名（与实体类中@Field配置的一致）
         */
        queryBuilder.withQuery(QueryBuilders.matchQuery("title","双卡双待"));

        //获取查询对象
        NativeSearchQuery query = queryBuilder.build();

        //查询
        AggregatedPage<TbItem> items = elasticsearchTemplate.queryForPage(query, TbItem.class);
        System.out.println("总记录数"+items.getTotalElements());
        System.out.println("总页数"+items.getTotalPages());
        for (TbItem item : items) {
            System.out.println(item);
        }
    }

    //复制域搜索
    @Test
    public void copyFieldQuery(){
        //创建查询构造对象
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        /**
         * 参数1：域名（与实体类中@Field配置的一致）
         */
        queryBuilder.withQuery(QueryBuilders.matchQuery("brand","Apple"));

        //获取查询对象
        NativeSearchQuery query = queryBuilder.build();

        //查询
        AggregatedPage<TbItem> items = elasticsearchTemplate.queryForPage(query, TbItem.class);
        System.out.println("总记录数"+items.getTotalElements());
        System.out.println("总页数"+items.getTotalPages());
        for (TbItem item : items) {
            System.out.println(item);
        }
    }

    //嵌套域搜索
    @Test
    public void nestedQuery(){
        //创建查询构造对象
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //组合查询对象
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //基本查询条件
        boolQuery.must(QueryBuilders.matchQuery("title","苹果"));

        //添加过滤查询
        /**
         * 参数1：指定实体类中嵌套域对应属性名称
         * 参数2：查询对象：域名，值
         * 参数3：得分模式；在有多个查询文档的时候；选择这些文档中得分最大的作为本次查询的score分值
         */
        NestedQueryBuilder queryBuilder1 =
                new NestedQueryBuilder("specMap", QueryBuilders.wildcardQuery("specMap.机身内存.keyword", "8G"), ScoreMode.Max);
        boolQuery.filter(queryBuilder1);

        NestedQueryBuilder queryBuilder2 =
                new NestedQueryBuilder("specMap", QueryBuilders.wildcardQuery("specMap.屏幕尺寸.keyword", "5.5"), ScoreMode.Max);
        boolQuery.filter(queryBuilder2);

        /**
         * 参数1：域名（与实体类中@Field配置的一致）
         */
        queryBuilder.withQuery(boolQuery);

        //获取查询对象
        NativeSearchQuery query = queryBuilder.build();

        //查询
        AggregatedPage<TbItem> items = elasticsearchTemplate.queryForPage(query, TbItem.class);
        System.out.println("总记录数：" + items.getTotalElements());
        System.out.println("总页数：" + items.getTotalPages());
        for (TbItem item : items) {
            System.out.println(item);
        }
    }

}
