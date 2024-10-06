package com.interface21.webmvc.servlet.mvc.tobe;

import com.interface21.BeanRegistry;
import com.interface21.webmvc.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class HandlerExecution {

    private final Method method;
    private final Object instance;

    public HandlerExecution(Method method) {
        this.method = method;
        Class<?> declaringClass = method.getDeclaringClass();
        BeanRegistry instance1 = BeanRegistry.getInstance();
        List<?> handler = instance1.getBeans(declaringClass);
        this.instance = handler.getFirst();
    }

    public ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            return (ModelAndView) method.invoke(instance, request, response);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
