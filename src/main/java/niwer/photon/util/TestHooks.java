package niwer.photon.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class TestHooks {

    private TestHooks() {}

    public static boolean invokeStaticVoid(String className, String methodName, Class<?>[] parameterTypes, Object... arguments) {
        try {
            final Class<?> targetClass = Class.forName(className);
            final Method method = targetClass.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            method.invoke(null, arguments);
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            return false;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to access test hook " + className + "#" + methodName, e);
        } catch (InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("Test hook " + className + "#" + methodName + " failed", cause);
        }
    }

    public static Object invokeStatic(String className, String methodName, Class<?>[] parameterTypes, Object... arguments) {
        try {
            final Class<?> targetClass = Class.forName(className);
            final Method method = targetClass.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(null, arguments);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            return null;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to access test hook " + className + "#" + methodName, e);
        } catch (InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("Test hook " + className + "#" + methodName + " failed", cause);
        }
    }
}