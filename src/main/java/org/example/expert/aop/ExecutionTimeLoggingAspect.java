package org.example.expert.aop;

import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ExecutionTimeLoggingAspect {
  @Around("@annotation(org.example.expert.domain.common.annotation.ExecutionLoggable)")
  public Object logAroundExecution(ProceedingJoinPoint joinPoint) throws Throwable{
    long start = System.currentTimeMillis();
    log.info("START EXECUTION: "+joinPoint.toString());
    try {
      return joinPoint.proceed();
    } finally {
      long finish = System.currentTimeMillis();
      long executionTime = finish-start;
      log.info("END EXECUTION: "+joinPoint.toString()+" === TIME: "+executionTime+" ms");
    }
  }
}
