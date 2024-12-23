package com.mb.aspect;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.alibaba.excel.EasyExcel;
import com.mb.annotation.Export;
import com.mb.properties.ExportProperties;
import com.mb.utils.GenericTypeUtils;
import com.mb.utils.ReflectionFieldUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 导出切面
 * @author cxn
 * @date 2024/12/18 11:29
 */
@Slf4j
@Aspect
public class ExportAspect {

    @Autowired
    private ExportProperties exportProperties;


    /**
     * 环绕通知
     * @param pjp
     * @return
     */
    @Around("@annotation(export)")
    public Object export(ProceedingJoinPoint pjp, Export export) throws Throwable {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (sra == null) {
            return pjp.proceed();
        }

        /* 参数采用最近原则，优先取注解的，如果注解没有指定才取全局的 */
        String exportParam = StringUtils.isNotBlank(export.exportParam()) ? export.exportParam() : exportProperties.getExportParam();
        String exportAllParam = StringUtils.isNotBlank(export.exportAllParam()) ? export.exportAllParam() : exportProperties.getExportAllParam();
        String pageNumParam = StringUtils.isNotBlank(export.pageNumParam()) ? export.pageNumParam() : exportProperties.getPageNumParam();
        String pageSizeParam = StringUtils.isNotBlank(export.pageSizeParam()) ? export.pageSizeParam() : exportProperties.getPageSizeParam();

        // 根据请求方式获取请求参数，判断是否为导出
        Map<String, Boolean> exportMap = this.checkExport(pjp, sra.getRequest(), exportParam, exportAllParam);
        if (!exportMap.get(exportParam)) {
            return pjp.proceed();
        }

        log.info("开始处理导出...");

        // 获取方法的参数，
        Object[] args = pjp.getArgs();

        if (exportMap.get(exportAllParam)) {
            this.handleExportAll(pjp, pageNumParam, pageSizeParam, args);
        }

        // 执行方法
        Object proceed = pjp.proceed(args);

        HttpServletResponse response = sra.getResponse();
        if (response == null) {
            return proceed;
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(export.fileName(), "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 响应数据的字段名
        String responseData = StringUtils.isNotBlank(export.responseData()) ? export.responseData() : exportProperties.getResponseData();

        // 解析响应数据
        if (StringUtils.isBlank(responseData) || "default".equals(responseData)) {
            Collection<?> data = (Collection<?>) proceed;
            if (CollUtil.isEmpty(data)) {
                EasyExcel.write(response.getOutputStream()).sheet("").doWrite(new ArrayList<>());
            } else {
                Optional<Class<?>> listElementType = GenericTypeUtils.getListElementType(proceed);
                EasyExcel.write(response.getOutputStream(), listElementType.get()).sheet("").doWrite(data);
            }
        } else {
            Class<?> proceedClass = proceed.getClass();
            Field field = proceedClass.getDeclaredField(responseData);
            field.setAccessible(true);
            Collection<?> data = (Collection<?>) field.get(proceed);
            if (CollUtil.isEmpty(data)) {
                EasyExcel.write(response.getOutputStream()).sheet("").doWrite(new ArrayList<>());
            } else {
                Optional<Class<?>> listElementType = GenericTypeUtils.getListElementType(data);
                EasyExcel.write(response.getOutputStream(), listElementType.get()).sheet("").doWrite(data);
            }
        }

        log.info("导出完成！");
        return null;
    }

    /**
     * 校验是否为导出请求
     * @param pjp
     * @param request
     * @param exportParam
     * @param exportAllParam
     * @return
     * @throws Exception
     */
    private Map<String, Boolean> checkExport(ProceedingJoinPoint pjp, HttpServletRequest request, String exportParam, String exportAllParam) throws Exception {
        // 判断是否导出请求，导出的话是否为导出全部
        boolean isExport = false;
        boolean isExportAll = false;

        String exportParamValue = request.getParameter(exportParam);
        if (StringUtils.isNotBlank(exportParamValue)
                && ("1".equals(exportParamValue) || "true".equals(exportParamValue))) {
            isExport = true;
            // 导出请求，接着判断是否为导出全部
            String exportAllParamValue = request.getParameter(exportAllParam);
            if (StringUtils.isNotBlank(exportAllParamValue)
                    && ("1".equals(exportAllParamValue) || "true".equals(exportAllParamValue))) {
                isExportAll = true;
            }
        }

        if ("POST".equals(request.getMethod()) && StringUtils.isBlank(exportParamValue)) {
            // 如果是POST请求，那么可能是在body里传的
            Object[] args = pjp.getArgs();
            for (Object arg : args) {
                // 过滤基本数据类型
                if (this.filterBasic(arg)) continue;
                // 排除掉所有的基本数据类型和他们的包装类，剩下的就是实体类，获取实体类的所有属性
                List<Field> fieldList = ReflectionFieldUtils.getAllFieldsOfName(arg.getClass(), ListUtil.toList(exportParam, exportAllParam));
                if (CollUtil.isEmpty(fieldList)) {
                    continue;
                }
                // 修改分页参数
                for (Field field : fieldList) {
                    field.setAccessible(true);
                    if (field.getName().equals(exportParam)) {
                        Object o = field.get(arg);
                        if (o != null && ("1".equals(o.toString()) || "true".equals(o.toString()))) {
                            isExport = true;
                        }
                    } else if (field.getName().equals(exportAllParam)) {
                        Object o = field.get(arg);
                        if (o != null && ("1".equals(o.toString()) || "true".equals(o.toString()))) {
                            isExportAll = true;
                        }
                    }
                }
            }
        }

        Map<String, Boolean> map = new HashMap<>();
        map.put(exportParam, isExport);
        map.put(exportAllParam, isExportAll);
        return map;
    }

    /**
     * 过滤基本数据类型
     * @param arg
     * @return
     */
    private boolean filterBasic(Object arg) {
        if (arg instanceof Integer) {
            return true;
        } else if (arg instanceof Long) {
            return true;
        } else if (arg instanceof String) {
            return true;
        } else if (arg instanceof Boolean) {
            return true;
        } else if (arg instanceof Float) {
            return true;
        } else if (arg instanceof Double) {
            return true;
        } else if (arg instanceof Character) {
            return true;
        } else if (arg instanceof Byte) {
            return true;
        } else if (arg instanceof Short) {
            return true;
        }
        return false;
    }

    /**
     * 处理导出全部
     * @param pjp
     * @param pageNumParam
     * @param pageSizeParam
     * @param args
     * @throws Exception
     */
    private void handleExportAll(ProceedingJoinPoint pjp, String pageNumParam, String pageSizeParam, Object[] args) throws Exception {
        // 导出全部，那么就需要修改分页参数
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        // 使用 Spring 的工具类获取参数名
        String[] parameterNames = new LocalVariableTableParameterNameDiscoverer().getParameterNames(signature.getMethod());
        if (parameterNames == null || parameterNames.length == 0) {
            throw new RuntimeException("方法没有参数，你是怎么获取的？用HttpServletRequest吗？");
        }

        // 先判断分页参数是否在方法参数中
        boolean isReturn = false;

        for (int i = 0; i < parameterNames.length; i++) {
            String parameterName = parameterNames[i];
            if (parameterName.equals(pageNumParam) || parameterName.equals(pageSizeParam)) {
                args[i] = null;
                isReturn = true;
            }
        }

        if (isReturn) {
            return;
        }

        // 如果分页参数不在方法参数中，那么就在实体类中
        for (Object arg : args) {
            // 过滤基本数据类型
            if (this.filterBasic(arg)) continue;
            // 排除掉所有的基本数据类型和他们的包装类，剩下的就是实体类，获取实体类的所有属性
            List<Field> fieldList = ReflectionFieldUtils.getAllFieldsOfName(arg.getClass(), ListUtil.toList(pageNumParam, pageSizeParam));
            if (CollUtil.isEmpty(fieldList)) {
                continue;
            }
            // 修改分页参数
            for (Field field : fieldList) {
                field.setAccessible(true);
                if (field.getName().equals(pageNumParam) || field.getName().equals(pageSizeParam)) {
                    field.set(arg, null);
                }
            }
        }
    }
}
