package com.pinyougou.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/brand")
@RestController
public class BrandController {

    @Reference
    private BrandService brandService;

    /**
     * 分页查询品牌列表
     * @param pageNum 页号
     * @param pageSize 页大小
     * @return 分页信息对象
     */
    @GetMapping("/findPage")
    public PageInfo<TbBrand> findPage(@RequestParam(name="pageNum",defaultValue = "1")Integer pageNum,
                             @RequestParam(name="pageSize",defaultValue = "10")Integer pageSize){
        return brandService.findPage(pageNum, pageSize);
    }

    /**
     * 分页查询品牌列表
     * @param pageNum 页号
     * @param pageSize 页大小
     * @return 品牌列表
     */
    @GetMapping("/testPage")
    public List<TbBrand> testPage(@RequestParam(name="pageNum",defaultValue = "1")Integer pageNum,
                                  @RequestParam(name="pageSize",defaultValue = "10")Integer pageSize){
        //return brandService.testPage(pageNum, pageSize);
        return brandService.findPage(pageNum, pageSize).getList();
    }

    @GetMapping("/findAll")
    public List<TbBrand> findAll(){
        //return brandService.queryAll();
        return brandService.findAll();
    }
}
