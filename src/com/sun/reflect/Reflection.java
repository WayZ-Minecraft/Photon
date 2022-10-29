package com.sun.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class Reflection
{
    private static volatile Map<Class<?>, String[]> fieldFilterMap;
    private static volatile Map<Class<?>, String[]> methodFilterMap;
    
    static {
        final Map<Class<?>, String[]> map = new HashMap<Class<?>, String[]>();
        map.put(Reflection.class, new String[] { "fieldFilterMap", "methodFilterMap" });
        map.put(System.class, new String[] { "security" });
        map.put(Class.class, new String[] { "classLoader" });
        Reflection.fieldFilterMap = map;
        Reflection.methodFilterMap = new HashMap<Class<?>, String[]>();
    }
    
    public static native Class<?> getCallerClass();
    
    @Deprecated
    public static native Class<?> getCallerClass(final int p0);
    
    public static native int getClassAccessFlags(final Class<?> p0);
    
    public static boolean quickCheckMemberAccess(final Class<?> memberClass, final int modifiers) {
        return Modifier.isPublic(getClassAccessFlags(memberClass) & modifiers);
    }
    
    public static void ensureMemberAccess(final Class<?> currentClass, final Class<?> memberClass, final Object target, final int modifiers) throws IllegalAccessException {
        if (currentClass == null || memberClass == null) {
            throw new InternalError();
        }
        if (!verifyMemberAccess(currentClass, memberClass, target, modifiers)) {
            throw new IllegalAccessException("Class " + currentClass.getName() + " can not access a member of class " + memberClass.getName() + " with modifiers \"" + Modifier.toString(modifiers) + "\"");
        }
    }
    
    public static boolean verifyMemberAccess(final Class<?> currentClass, final Class<?> memberClass, final Object target, final int modifiers) {
        boolean gotIsSameClassPackage = false;
        boolean isSameClassPackage = false;
        if (currentClass == memberClass) {
            return true;
        }
        if (!Modifier.isPublic(getClassAccessFlags(memberClass))) {
            isSameClassPackage = isSameClassPackage(currentClass, memberClass);
            gotIsSameClassPackage = true;
            if (!isSameClassPackage) {
                return false;
            }
        }
        if (Modifier.isPublic(modifiers)) {
            return true;
        }
        boolean successSoFar = false;
        if (Modifier.isProtected(modifiers) && isSubclassOf(currentClass, memberClass)) {
            successSoFar = true;
        }
        if (!successSoFar && !Modifier.isPrivate(modifiers)) {
            if (!gotIsSameClassPackage) {
                isSameClassPackage = isSameClassPackage(currentClass, memberClass);
                gotIsSameClassPackage = true;
            }
            if (isSameClassPackage) {
                successSoFar = true;
            }
        }
        if (!successSoFar) {
            return false;
        }
        if (Modifier.isProtected(modifiers)) {
            final Class<?> targetClass = (target == null) ? memberClass : target.getClass();
            if (targetClass != currentClass) {
                if (!gotIsSameClassPackage) {
                    isSameClassPackage = isSameClassPackage(currentClass, memberClass);
                    gotIsSameClassPackage = true;
                }
                if (!isSameClassPackage && !isSubclassOf(targetClass, currentClass)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private static boolean isSameClassPackage(final Class<?> c1, final Class<?> c2) {
        return isSameClassPackage(c1.getClassLoader(), c1.getName(), c2.getClassLoader(), c2.getName());
    }
    
    private static boolean isSameClassPackage(final ClassLoader loader1, final String name1, final ClassLoader loader2, final String name2) {
        if (loader1 != loader2) {
            return false;
        }
        final int lastDot1 = name1.lastIndexOf(46);
        final int lastDot2 = name2.lastIndexOf(46);
        if (lastDot1 == -1 || lastDot2 == -1) {
            return lastDot1 == lastDot2;
        }
        int idx1 = 0;
        int idx2 = 0;
        if (name1.charAt(idx1) == '[') {
            do {
                ++idx1;
            } while (name1.charAt(idx1) == '[');
            if (name1.charAt(idx1) != 'L') {
                throw new InternalError("Illegal class name " + name1);
            }
        }
        if (name2.charAt(idx2) == '[') {
            do {
                ++idx2;
            } while (name2.charAt(idx2) == '[');
            if (name2.charAt(idx2) != 'L') {
                throw new InternalError("Illegal class name " + name2);
            }
        }
        final int length1 = lastDot1 - idx1;
        final int length2 = lastDot2 - idx2;
        return length1 == length2 && name1.regionMatches(false, idx1, name2, idx2, length1);
    }
    
    static boolean isSubclassOf(Class<?> queryClass, final Class<?> ofClass) {
        while (queryClass != null) {
            if (queryClass == ofClass) {
                return true;
            }
            queryClass = queryClass.getSuperclass();
        }
        return false;
    }
    
    public static synchronized void registerFieldsToFilter(final Class<?> containingClass, final String... fieldNames) {
        Reflection.fieldFilterMap = registerFilter(Reflection.fieldFilterMap, containingClass, fieldNames);
    }
    
    public static synchronized void registerMethodsToFilter(final Class<?> containingClass, final String... methodNames) {
        Reflection.methodFilterMap = registerFilter(Reflection.methodFilterMap, containingClass, methodNames);
    }
    
    private static Map<Class<?>, String[]> registerFilter(Map<Class<?>, String[]> map, final Class<?> containingClass, final String... names) {
        if (map.get(containingClass) != null) {
            throw new IllegalArgumentException("Filter already registered: " + containingClass);
        }
        map = new HashMap<Class<?>, String[]>(map);
        map.put(containingClass, names);
        return map;
    }
    
    public static Field[] filterFields(final Class<?> containingClass, final Field[] fields) {
        if (Reflection.fieldFilterMap == null) {
            return fields;
        }
        return (Field[])filter(fields, Reflection.fieldFilterMap.get(containingClass));
    }
    
    public static Method[] filterMethods(final Class<?> containingClass, final Method[] methods) {
        if (Reflection.methodFilterMap == null) {
            return methods;
        }
        return (Method[])filter(methods, Reflection.methodFilterMap.get(containingClass));
    }
    
    private static Member[] filter(final Member[] members, final String[] filteredNames) {
        if (filteredNames == null || members.length == 0) {
            return members;
        }
        int numNewMembers = 0;
        for (final Member member : members) {
            boolean shouldSkip = false;
            for (final String filteredName : filteredNames) {
                if (member.getName() == filteredName) {
                    shouldSkip = true;
                    break;
                }
            }
            if (!shouldSkip) {
                ++numNewMembers;
            }
        }
        final Member[] newMembers = (Member[])Array.newInstance(members[0].getClass(), numNewMembers);
        int destIdx = 0;
        for (final Member member2 : members) {
            boolean shouldSkip2 = false;
            for (final String filteredName2 : filteredNames) {
                if (member2.getName() == filteredName2) {
                    shouldSkip2 = true;
                    break;
                }
            }
            if (!shouldSkip2) {
                newMembers[destIdx++] = member2;
            }
        }
        return newMembers;
    }
    
    private static boolean isExtClassLoader(final ClassLoader loader) {
        for (ClassLoader cl = ClassLoader.getSystemClassLoader(); cl != null; cl = cl.getParent()) {
            if (cl.getParent() == null && cl == loader) {
                return true;
            }
        }
        return false;
    }
}
