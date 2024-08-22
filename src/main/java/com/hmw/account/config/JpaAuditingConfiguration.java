package com.hmw.account.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.lang.annotation.Documented;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration {

}