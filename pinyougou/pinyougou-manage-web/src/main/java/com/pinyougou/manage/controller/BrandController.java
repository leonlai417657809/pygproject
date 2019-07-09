package com.pinyougou.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/brand")
@RestController
public class BrandController {

    @Reference
    private BrandService brandService;

    /**
     * 根据主键查询
     * @param id 品牌id
     * @return 品牌
     */
    //请求地址：../brand/findOne/123.do
    @GetMapping("findOne/{id}")
    public TbBrand findOne(@PathVariable Long id){
        return brandService.findOne(id);
    }

    /**
     * 新增品牌
     * @param brand 品牌
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody TbBrand brand){
        try{
            brandService.add(brand);
            //return new Result(true, "新增品牌成功");
            return Result.ok("新增品牌成功");
        }catch (Exception e){
            e.printStackTrace();
        }
        //return new Result(false, "新增品牌失败");
        return Result.fail("新增品牌失败");
    }

    /**
     * 修改品牌
     * @param brand 品牌
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result update(@RequestBody TbBrand brand){
        try {
            brandService.update(brand);
            return Result.ok("修改品牌成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改品牌失败");
    }

    /**
     * 批量删除品牌
     * @param ids 品牌id数组
     * @return 操作结果
     */
    @GetMapping("/delete")
    public Result delete(Long[] ids){
        try {
            brandService.deleteByIds(ids);
            return Result.ok("删除品牌成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除品牌失败");
    }

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

    /**
     * 条件分页查询
     * @param pageNum 页号
     * @param pageSize 页大小
     * @param brand 查询条件对象
     * @return 分页信息对象
     */
    @PostMapping("/search")
    public PageInfo<TbBrand> testPage(@RequestParam(name="pageNum",defaultValue = "1")Integer pageNum,
                                  @RequestParam(name="pageSize",defaultValue = "10")Integer pageSize,
                                  @RequestBody TbBrand brand){
        return brandService.search(pageNum, pageSize,brand);
    }
}
