package com.central.admin.controller;

import com.central.admin.model.IndexDto;
import com.central.admin.model.IndexVo;
import com.central.admin.properties.IndexProperties;
import com.central.admin.service.IIndexService;
import com.central.common.entity.PageResult;
import com.central.common.entity.Result;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 索引管理
 */
@Slf4j
@RestController
@Api(tags = "索引管理api")
@RequestMapping("/admin")
public class IndexController {
    @Autowired
    private IIndexService indexService;

    @Autowired
    private IndexProperties indexProperties;

    @PostMapping("/index")
    public Result createIndex(@RequestBody IndexDto indexDto) {
        if (indexDto.getNumberOfShards() == null) {
            indexDto.setNumberOfShards(1);
        }
        if (indexDto.getNumberOfReplicas() == null) {
            indexDto.setNumberOfReplicas(0);
        }
        indexService.create(indexDto);
        return Result.succeed("操作成功");
    }

    /**
     * 索引列表
     */
    @GetMapping("/indices")
    public PageResult<IndexVo> list(@RequestParam(required = false) String queryStr) {
        return indexService.list(queryStr, indexProperties.getShow());
    }

    /**
     * 索引明细
     */
    @GetMapping("/index/{indexName}")
    public Result<Map<String, Object>> showIndex(@PathVariable String indexName) {
        Map<String, Object> result = indexService.show(indexName);
        return Result.succeed(result);
    }

    /**
     * 删除索引
     */
    @DeleteMapping("/index/{indexName}")
    public Result deleteIndex(@PathVariable String indexName) {
        indexService.delete(indexName);
        return Result.succeed("操作成功");
    }
}
