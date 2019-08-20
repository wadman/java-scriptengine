package ch.obermuhlner.scriptengine.java.execution;

import javax.script.ScriptException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DefaultExecutionStrategy implements ExecutionStrategy {

    private final Class<?> clazz;
    private final Method method;

    public DefaultExecutionStrategy(Class<?> clazz) {
        method = findCallableMethod(clazz);
        this.clazz = clazz;
    }

    @Override
    public Object execute(Object instance) throws ScriptException {
        if (instance instanceof Supplier) {
            Supplier supplier = (Supplier) instance;
            return supplier.get();
        }

        if (instance instanceof Runnable) {
            Runnable runnable = (Runnable) instance;
            runnable.run();
            return null;
        }

        if (method != null) {
            try {
                return method.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ScriptException(e);
            }
        }

        if (instance == null) {
            throw new ScriptException("No static method found to execute of type " + clazz.getName());
        }
        throw new ScriptException("No method found to execute instance of type " + clazz.getName());
    }

    private static Method findCallableMethod(Class<?> clazz) {
        List<Method> callableMethods = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            int modifiers = method.getModifiers();
            if (method.getParameterCount() == 0 && Modifier.isPublic(modifiers)) {
                callableMethods.add(method);
            }
        }

        if (callableMethods.size() == 1) {
            return callableMethods.get(0);
        }

        return null;
    }
}
