package com.central.user.config;

import com.central.db.config.DefaultMybatisPlusConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.central.user.mapper*"})
public class MybatisPlusConfig extends DefaultMybatisPlusConfig {
}
