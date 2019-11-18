package com.central.common.ribbon.config;

import com.central.common.ribbon.rule.CustomIsolationRule;
import com.netflix.loadbalancer.IRule;
import org.springframework.context.annotation.Bean;

public class RuleConfigure {
    @Bean
    public IRule isolationRule() {
        return new CustomIsolationRule();
    }
}
