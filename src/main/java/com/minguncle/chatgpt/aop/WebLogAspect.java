package com.minguncle.chatgpt.aop;

import com.alibaba.fastjson2.JSON;
import com.minguncle.chatgpt.pojo.vo.ChatRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;

@Aspect
@Component
@Slf4j
public class WebLogAspect {

    @Pointcut("execution(public * com.minguncle.chatgpt.controller.*.*(..))")
    public void webLog() {
    }

    @Around("webLog()")
    public Object doBefore(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("==========request log start==============");
        long startTime = System.currentTimeMillis();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Object param = joinPoint.getArgs()[0];
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            // 获取请求参数
            Map<String, String[]> paramMap = request.getParameterMap();
            String params = JSON.toJSONString(paramMap);
            log.info("Request URL : [{}]", request.getRequestURL().toString());
            log.info("HTTP METHOD : [{}]", request.getMethod());
            log.info("IP ADDRESS  : [{}]", request.getRemoteAddr());
            log.info("CLASS METHOD : [{}]", joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
            log.debug("BODY : [{}]", param);
            log.debug("PARAMS : [{}]", params);
        }
        //如果参数为ChatRequest，则自动注入traceId
        if (param instanceof ChatRequest) {
            ChatRequest chatRequest = (ChatRequest) param;
            String traceId = UUID.randomUUID().toString();
            chatRequest.setTraceId(traceId);
            log.info("TRACE ID : [{}]", traceId);
        }
        Object proceed = null;
        try {
            proceed = joinPoint.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        log.info("RESPONSE : [{}]", proceed);
        log.info("SPEND TIME : [{}]ms", (System.currentTimeMillis() - startTime));
        log.info("==========request log end==============");
        return proceed;
    }


}
