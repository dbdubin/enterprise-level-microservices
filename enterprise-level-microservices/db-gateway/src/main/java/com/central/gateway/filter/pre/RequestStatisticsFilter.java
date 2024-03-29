package com.central.gateway.filter.pre;

import com.central.common.utils.AddrUtil;
import com.central.log.monitor.PointUtil;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 请求统计分析埋点过滤器
 */
@Slf4j
@Component
public class RequestStatisticsFilter extends ZuulFilter {
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest req = ctx.getRequest();
        UserAgent userAgent = UserAgent.parseUserAgentString(req.getHeader("User-Agent"));

        //埋点
        PointUtil.debug("0", "request-statistics",
                "ip=" + AddrUtil.getRemoteAddr(req)
                        + "&browser=" + userAgent.getBrowser()
                        + "&operatingSystem=" + userAgent.getOperatingSystem());

        return null;
    }
}
