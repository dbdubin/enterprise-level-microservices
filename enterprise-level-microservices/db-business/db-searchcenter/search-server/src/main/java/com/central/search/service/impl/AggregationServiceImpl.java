package com.central.search.service.impl;

import com.central.common.constant.CommonConstant;
import com.central.search.model.AggItemVo;
import com.central.search.service.IAggregationService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.ExtendedBounds;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.range.date.InternalDateRange;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聚合分析服务
 *
 */
@Service
public class AggregationServiceImpl implements IAggregationService {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 访问统计聚合查询，需要es里面提供以下结构的数据
     * {
     *    ip, //访问ip
     * 	  browser, //浏览器
     * 	  operatingSystem, //操作系统
     * 	  timestamp //日志时间
     * }
     *
     * @param indexName 索引名
     * @param routing es的路由
     * @return 返回结果样例如下
     * {
     *   "currDate_uv": 219,
     *   "currDate_pv": 2730,
     *   "currWeek_pv": 10309,
     *   "currHour_uv": 20,
     *   "browser_datas": [
     *     {
     *       "name": "CHROME",
     *       "value": 7416
     *     },
     *     {
     *       "name": "SAFARI",
     *       "value": 232
     *     },
     *     ...
     *   ],
     *   "browser_legendData": [
     *     "CHROME",
     *     "SAFARI",
     *     ...
     *   ],
     *   "operatingSystem_datas": [
     *     {
     *       "name": "WINDOWS_10",
     *       "value": 6123
     *     },
     *     {
     *       "name": "MAC_OS_X",
     *       "value": 1455
     *     },
     *     ...
     *   ],
     *   "currMonth_pv": 10311,
     *   "statWeek_uv": [
     *     487,
     *     219,
     *     ...
     *   ],
     *   "operatingSystem_legendData": [
     *     "WINDOWS_10",
     *     "MAC_OS_X",
     *     ...
     *   ],
     *   "statWeek_items": [
     *     "2019-05-08",
     *     "2019-05-09",
     *     ...
     *   ],
     *   "statWeek_pv": [
     *     7567,
     *     2730
     *     ...
     *   ]
     * }
     */
    @Override
    public Map<String, Object> requestStatAgg(String indexName, String routing) {
        DateTime currDt = DateTime.now();
        LocalDate localDate = LocalDate.now();
        SearchResponse response = elasticsearchTemplate.getClient().prepareSearch(indexName)
                .setRouting(routing)
                .addAggregation(
                        //聚合查询当天的数据
                        AggregationBuilders
                                .dateRange("currDate")
                                .field("timestamp")
                                .addRange(
                                        currDt.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0), currDt
                                )
                                .subAggregation(
                                        AggregationBuilders
                                                .cardinality("uv")
                                                .field("ip.keyword")
                                )
                )
                .addAggregation(
                        //聚合查询7天内的数据
                        AggregationBuilders
                                .dateRange("currWeek")
                                .field("timestamp")
                                .addRange(currDt.minusDays(7), currDt)
                                .subAggregation(
                                        //聚合并且按日期分组查询7天内的数据
                                        AggregationBuilders
                                                .dateHistogram("statWeek")
                                                .field("timestamp")
                                                .dateHistogramInterval(DateHistogramInterval.DAY)
                                                .format(CommonConstant.DATE_FORMAT)
                                                //时区相差8小时
                                                .timeZone(DateTimeZone.forOffsetHours(8))
                                                .minDocCount(0L)
                                                .extendedBounds(new ExtendedBounds(
                                                        localDate.minusDays(6).format(DateTimeFormatter.ofPattern(CommonConstant.DATE_FORMAT)),
                                                        localDate.format(DateTimeFormatter.ofPattern(CommonConstant.DATE_FORMAT))
                                                ))
                                                .subAggregation(
                                                        AggregationBuilders
                                                                .cardinality("uv")
                                                                .field("ip.keyword")
                                                )
                                )
                )
                .addAggregation(
                        //聚合查询30天内的数据
                        AggregationBuilders
                                .dateRange("currMonth")
                                .field("timestamp")
                                .addRange(currDt.minusDays(30), currDt)
                )
                .addAggregation(
                        //聚合查询浏览器的数据
                        AggregationBuilders
                                .terms("browser")
                                .field("browser.keyword")
                )
                .addAggregation(
                        //聚合查询操作系统的数据
                        AggregationBuilders
                                .terms("operatingSystem")
                                .field("operatingSystem.keyword")
                )
                .addAggregation(
                        //聚合查询1小时内的数据
                        AggregationBuilders
                                .dateRange("currHour")
                                .field("timestamp")
                                .addRange(
                                        currDt.minusHours(1), currDt
                                )
                                .subAggregation(
                                        AggregationBuilders
                                                .cardinality("uv")
                                                .field("ip.keyword")
                                )
                )
                .setSize(0)
                .get();
        Aggregations aggregations = response.getAggregations();
        Map<String, Object> result = new HashMap<>(9);
        if (aggregations != null) {
            setCurrDate(result, aggregations);
            setCurrWeek(result, aggregations);
            setCurrMonth(result, aggregations);
            setTermsData(result, aggregations, "browser");
            setTermsData(result, aggregations, "operatingSystem");
            setCurrHour(result, aggregations);
        }
        return result;
    }
    /**
     * 赋值当天统计
     */
    private void setCurrDate(Map<String, Object> result, Aggregations aggregations) {
        InternalDateRange currDate = aggregations.get("currDate");
        InternalDateRange.Bucket bucket = currDate.getBuckets().get(0);
        Cardinality cardinality = bucket.getAggregations().get("uv");
        result.put("currDate_pv", bucket.getDocCount());
        result.put("currDate_uv", cardinality.getValue());
    }
    /**
     * 赋值周统计
     */
    private void setCurrWeek(Map<String, Object> result, Aggregations aggregations) {
        InternalDateRange currWeek = aggregations.get("currWeek");
        InternalDateRange.Bucket bucket = currWeek.getBuckets().get(0);
        result.put("currWeek_pv", bucket.getDocCount());
        //赋值周趋势统计
        setStatWeek(result, bucket.getAggregations());
    }
    /**
     * 赋值月统计
     */
    private void setCurrMonth(Map<String, Object> result, Aggregations aggregations) {
        InternalDateRange currMonth = aggregations.get("currMonth");
        InternalDateRange.Bucket bucket = currMonth.getBuckets().get(0);
        result.put("currMonth_pv", bucket.getDocCount());
    }
    /**
     * 赋值单字段统计
     */
    private void setTermsData(Map<String, Object> result, Aggregations aggregations, String key) {
        Terms terms = aggregations.get(key);
        List<String> legendData = new ArrayList<>();
        List<AggItemVo> datas = new ArrayList<>();
        for (Terms.Bucket bucket : terms.getBuckets()) {
            legendData.add((String)bucket.getKey());
            AggItemVo item = new AggItemVo();
            item.setName((String)bucket.getKey());
            item.setValue(bucket.getDocCount());
            datas.add(item);
        }
        result.put(key+"_legendData", legendData);
        result.put(key+"_datas", datas);
    }
    /**
     * 赋值周趋势统计
     */
    private void setStatWeek(Map<String, Object> result, Aggregations aggregations) {
        InternalDateHistogram agg = aggregations.get("statWeek");
        List<String> items = new ArrayList<>();
        List<Long> uv = new ArrayList<>();
        List<Long> pv = new ArrayList<>();
        Cardinality cardinality;
        for (InternalDateHistogram.Bucket bucket : agg.getBuckets()) {
            items.add(bucket.getKeyAsString());
            pv.add(bucket.getDocCount());

            cardinality = bucket.getAggregations().get("uv");
            uv.add(cardinality.getValue());
        }
        result.put("statWeek_items", items);
        result.put("statWeek_uv", uv);
        result.put("statWeek_pv", pv);
    }
    /**
     * 赋值小时内统计-当前在线数
     */
    private void setCurrHour(Map<String, Object> result, Aggregations aggregations) {
        InternalDateRange currDate = aggregations.get("currHour");
        InternalDateRange.Bucket bucket = currDate.getBuckets().get(0);
        Cardinality cardinality = bucket.getAggregations().get("uv");
        result.put("currHour_uv", cardinality.getValue());
    }
}
