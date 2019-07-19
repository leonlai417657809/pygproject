package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.ContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.service.impl.BaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Service
public class ContentServiceImpl extends BaseServiceImpl<TbContent> implements ContentService {
    //内容列表在redis中的key的名称
    private static final String REDIS_CONTENT = "CONTENT_LIST";
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ContentMapper contentMapper;

    @Override
    public PageInfo<TbContent> search(Integer pageNum, Integer pageSize, TbContent content) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbContent.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //模糊查询
        /**if (StringUtils.isNotBlank(content.getProperty())) {
            criteria.andLike("property", "%" + content.getProperty() + "%");
        }*/

        List<TbContent> list = contentMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public List<TbContent> findContentListByCategoryId(Long categoryId) {

        List<TbContent> contentList = null;

        try {
            //利用RedisTemplate查询当前分类对应的内容列表,从redis(缓存)中查询数据
            //返回contentList列表，因此查找结果需要进行强转
            contentList = (List<TbContent>) redisTemplate.boundHashOps(REDIS_CONTENT).get(categoryId);
            if(contentList !=null && contentList.size() >0){
                return contentList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /**
         * --根据内容分类id查询其对应的所有有效的内容列表并且根据排序字段降序排序。
         * SELECT * FROM tb_content WHERE category_id = ? AND status = 1 ORDER BY sort_order DESC
         */
        Example example = new Example(TbContent.class);

        example.createCriteria().andEqualTo("categoryId",categoryId)
                                .andEqualTo("status","1");
        //降序排序
        example.orderBy("sortOrder").desc();

        contentList = contentMapper.selectByExample(example);

        try {
            //查询不到则到mysql查询并存入redis再返回,设置某个分类对应的广告内容列表到缓存中
            redisTemplate.boundHashOps(REDIS_CONTENT).put(categoryId,contentList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contentList;
    }

    /**
     * 根据内容分类id更新其在redis中的内容列表
     * @param categoryId 内容分类id
     */
    private void updateContentListRedisByCategoryId(Long categoryId){
        redisTemplate.boundHashOps(REDIS_CONTENT).delete(categoryId);
    }

    @Override
    public void add(TbContent tbContent) {
        super.add(tbContent);
        //对新增的内容对应的内容分类在redis中的缓存进行更新；直接将该分类对应的内容列表删除
        updateContentListRedisByCategoryId(tbContent.getCategoryId());
    }

    @Override
    public void update(TbContent tbContent) {
        //查询老内容
        TbContent oldContent = findOne(tbContent.getId());
        if(!oldContent.getCategoryId().equals(tbContent.getCategoryId())){
            //更新旧分类对应的缓存
            updateContentListRedisByCategoryId(oldContent.getCategoryId());
        }

        super.update(tbContent);
        //更新当前内容对应的内容分类缓存
        updateContentListRedisByCategoryId(tbContent.getCategoryId());
    }

    @Override
    public void deleteByIds(Serializable[] ids) {
        //根据内容id数组查询内容列表；遍历每个内容，根据其分类id到redis删除缓存
        //select * from tb_content where id in(?,?..)
        Example example = new Example(TbContent.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));
        List<TbContent> contentList = contentMapper.selectByExample(example);
        for (TbContent tbContent : contentList) {
            updateContentListRedisByCategoryId(tbContent.getCategoryId());
        }

        super.deleteByIds(ids);
    }
}
