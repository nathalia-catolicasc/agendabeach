package com.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Habilita as tarefas agendadas (ex.: limpeza diária de tokens expirados). */
@Configuration
@EnableScheduling
public class SchedulingConfig {}
