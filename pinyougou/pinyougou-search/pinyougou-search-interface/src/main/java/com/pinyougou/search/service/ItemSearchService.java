package com.pinyougou.search.service;

import java.util.Map;

public interface ItemSearchService {
    /**
     * 根据查询条件查询es中的商品数据
     * @param searchMap 查询条件
     * @return 返回数据
     */
    Map<String, Object> search(Map<String, Object> searchMap);
}
