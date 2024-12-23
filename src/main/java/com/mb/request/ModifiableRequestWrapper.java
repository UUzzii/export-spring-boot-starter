package com.mb.request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;

public class ModifiableRequestWrapper extends HttpServletRequestWrapper {
    private final Map<String, String[]> modifiedParameters;
    private final Map<String, String> headerMap;

    public ModifiableRequestWrapper(HttpServletRequest request) {
        super(request);
        // 复制原始参数
        this.modifiedParameters = new HashMap<>(request.getParameterMap());
        this.headerMap = new HashMap<>();
    }

    // 修改参数值
    public void setParameter(String name, String value) {
        modifiedParameters.put(name, new String[]{value});
    }

    // 修改参数值（数组）
    public void setParameter(String name, String[] values) {
        modifiedParameters.put(name, values);
    }

    // 添加参数
    public void addParameter(String name, String value) {
        String[] values = modifiedParameters.get(name);
        if (values == null) {
            setParameter(name, value);
        } else {
            String[] newValues = Arrays.copyOf(values, values.length + 1);
            newValues[values.length] = value;
            modifiedParameters.put(name, newValues);
        }
    }

    // 删除参数
    public void removeParameter(String name) {
        modifiedParameters.remove(name);
    }

    // 设置请求头
    public void setHeader(String name, String value) {
        headerMap.put(name, value);
    }

    @Override
    public String getParameter(String name) {
        String[] values = modifiedParameters.get(name);
        return values != null && values.length > 0 ? values[0] : null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(modifiedParameters);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(modifiedParameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return modifiedParameters.get(name);
    }

    @Override
    public String getHeader(String name) {
        String headerValue = headerMap.get(name);
        return headerValue != null ? headerValue : super.getHeader(name);
    }
}