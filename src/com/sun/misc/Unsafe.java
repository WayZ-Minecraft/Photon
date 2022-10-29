package com.sun.misc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;

import com.sun.reflect.Reflection;

@SuppressWarnings({ "deprecation", "rawtypes" })
public final class Unsafe
{
    private static final Unsafe theUnsafe;
    public static final int INVALID_FIELD_OFFSET = -1;
    public static final int ARRAY_BOOLEAN_BASE_OFFSET;
    public static final int ARRAY_BYTE_BASE_OFFSET;
    public static final int ARRAY_SHORT_BASE_OFFSET;
    public static final int ARRAY_CHAR_BASE_OFFSET;
    public static final int ARRAY_INT_BASE_OFFSET;
    public static final int ARRAY_LONG_BASE_OFFSET;
    public static final int ARRAY_FLOAT_BASE_OFFSET;
    public static final int ARRAY_DOUBLE_BASE_OFFSET;
    public static final int ARRAY_OBJECT_BASE_OFFSET;
    public static final int ARRAY_BOOLEAN_INDEX_SCALE;
    public static final int ARRAY_BYTE_INDEX_SCALE;
    public static final int ARRAY_SHORT_INDEX_SCALE;
    public static final int ARRAY_CHAR_INDEX_SCALE;
    public static final int ARRAY_INT_INDEX_SCALE;
    public static final int ARRAY_LONG_INDEX_SCALE;
    public static final int ARRAY_FLOAT_INDEX_SCALE;
    public static final int ARRAY_DOUBLE_INDEX_SCALE;
    public static final int ARRAY_OBJECT_INDEX_SCALE;
    public static final int ADDRESS_SIZE;
    
    static {
        registerNatives();
        Reflection.registerMethodsToFilter(Unsafe.class, "getUnsafe");
        theUnsafe = new Unsafe();
        ARRAY_BOOLEAN_BASE_OFFSET = Unsafe.theUnsafe.arrayBaseOffset(boolean[].class);
        ARRAY_BYTE_BASE_OFFSET = Unsafe.theUnsafe.arrayBaseOffset(byte[].class);
        ARRAY_SHORT_BASE_OFFSET = Unsafe.theUnsafe.arrayBaseOffset(short[].class);
        ARRAY_CHAR_BASE_OFFSET = Unsafe.theUnsafe.arrayBaseOffset(char[].class);
        ARRAY_INT_BASE_OFFSET = Unsafe.theUnsafe.arrayBaseOffset(int[].class);
        ARRAY_LONG_BASE_OFFSET = Unsafe.theUnsafe.arrayBaseOffset(long[].class);
        ARRAY_FLOAT_BASE_OFFSET = Unsafe.theUnsafe.arrayBaseOffset(float[].class);
        ARRAY_DOUBLE_BASE_OFFSET = Unsafe.theUnsafe.arrayBaseOffset(double[].class);
        ARRAY_OBJECT_BASE_OFFSET = Unsafe.theUnsafe.arrayBaseOffset(Object[].class);
        ARRAY_BOOLEAN_INDEX_SCALE = Unsafe.theUnsafe.arrayIndexScale(boolean[].class);
        ARRAY_BYTE_INDEX_SCALE = Unsafe.theUnsafe.arrayIndexScale(byte[].class);
        ARRAY_SHORT_INDEX_SCALE = Unsafe.theUnsafe.arrayIndexScale(short[].class);
        ARRAY_CHAR_INDEX_SCALE = Unsafe.theUnsafe.arrayIndexScale(char[].class);
        ARRAY_INT_INDEX_SCALE = Unsafe.theUnsafe.arrayIndexScale(int[].class);
        ARRAY_LONG_INDEX_SCALE = Unsafe.theUnsafe.arrayIndexScale(long[].class);
        ARRAY_FLOAT_INDEX_SCALE = Unsafe.theUnsafe.arrayIndexScale(float[].class);
        ARRAY_DOUBLE_INDEX_SCALE = Unsafe.theUnsafe.arrayIndexScale(double[].class);
        ARRAY_OBJECT_INDEX_SCALE = Unsafe.theUnsafe.arrayIndexScale(Object[].class);
        ADDRESS_SIZE = Unsafe.theUnsafe.addressSize();
    }
    
    private static native void registerNatives();
    
    private Unsafe() {
    }
    
    public static Unsafe getUnsafe() {
		final Class cc = Reflection.getCallerClass(2);
        if (cc.getClassLoader() != null) {
            throw new SecurityException("Unsafe");
        }
        return Unsafe.theUnsafe;
    }
    
    public native int getInt(final Object p0, final long p1);
    
    public native void putInt(final Object p0, final long p1, final int p2);
    
    public native Object getObject(final Object p0, final long p1);
    
    public native void putObject(final Object p0, final long p1, final Object p2);
    
    public native boolean getBoolean(final Object p0, final long p1);
    
    public native void putBoolean(final Object p0, final long p1, final boolean p2);
    
    public native byte getByte(final Object p0, final long p1);
    
    public native void putByte(final Object p0, final long p1, final byte p2);
    
    public native short getShort(final Object p0, final long p1);
    
    public native void putShort(final Object p0, final long p1, final short p2);
    
    public native char getChar(final Object p0, final long p1);
    
    public native void putChar(final Object p0, final long p1, final char p2);
    
    public native long getLong(final Object p0, final long p1);
    
    public native void putLong(final Object p0, final long p1, final long p2);
    
    public native float getFloat(final Object p0, final long p1);
    
    public native void putFloat(final Object p0, final long p1, final float p2);
    
    public native double getDouble(final Object p0, final long p1);
    
    public native void putDouble(final Object p0, final long p1, final double p2);
    
    @Deprecated
    public int getInt(final Object o, final int offset) {
        return this.getInt(o, (long)offset);
    }
    
    @Deprecated
    public void putInt(final Object o, final int offset, final int x) {
        this.putInt(o, (long)offset, x);
    }
    
    @Deprecated
    public Object getObject(final Object o, final int offset) {
        return this.getObject(o, (long)offset);
    }
    
    @Deprecated
    public void putObject(final Object o, final int offset, final Object x) {
        this.putObject(o, (long)offset, x);
    }
    
    @Deprecated
    public boolean getBoolean(final Object o, final int offset) {
        return this.getBoolean(o, (long)offset);
    }
    
    @Deprecated
    public void putBoolean(final Object o, final int offset, final boolean x) {
        this.putBoolean(o, (long)offset, x);
    }
    
    @Deprecated
    public byte getByte(final Object o, final int offset) {
        return this.getByte(o, (long)offset);
    }
    
    @Deprecated
    public void putByte(final Object o, final int offset, final byte x) {
        this.putByte(o, (long)offset, x);
    }
    
    @Deprecated
    public short getShort(final Object o, final int offset) {
        return this.getShort(o, (long)offset);
    }
    
    @Deprecated
    public void putShort(final Object o, final int offset, final short x) {
        this.putShort(o, (long)offset, x);
    }
    
    @Deprecated
    public char getChar(final Object o, final int offset) {
        return this.getChar(o, (long)offset);
    }
    
    @Deprecated
    public void putChar(final Object o, final int offset, final char x) {
        this.putChar(o, (long)offset, x);
    }
    
    @Deprecated
    public long getLong(final Object o, final int offset) {
        return this.getLong(o, (long)offset);
    }
    
    @Deprecated
    public void putLong(final Object o, final int offset, final long x) {
        this.putLong(o, (long)offset, x);
    }
    
    @Deprecated
    public float getFloat(final Object o, final int offset) {
        return this.getFloat(o, (long)offset);
    }
    
    @Deprecated
    public void putFloat(final Object o, final int offset, final float x) {
        this.putFloat(o, (long)offset, x);
    }
    
    @Deprecated
    public double getDouble(final Object o, final int offset) {
        return this.getDouble(o, (long)offset);
    }
    
    @Deprecated
    public void putDouble(final Object o, final int offset, final double x) {
        this.putDouble(o, (long)offset, x);
    }
    
    public native byte getByte(final long p0);
    
    public native void putByte(final long p0, final byte p1);
    
    public native short getShort(final long p0);
    
    public native void putShort(final long p0, final short p1);
    
    public native char getChar(final long p0);
    
    public native void putChar(final long p0, final char p1);
    
    public native int getInt(final long p0);
    
    public native void putInt(final long p0, final int p1);
    
    public native long getLong(final long p0);
    
    public native void putLong(final long p0, final long p1);
    
    public native float getFloat(final long p0);
    
    public native void putFloat(final long p0, final float p1);
    
    public native double getDouble(final long p0);
    
    public native void putDouble(final long p0, final double p1);
    
    public native long getAddress(final long p0);
    
    public native void putAddress(final long p0, final long p1);
    
    public native long allocateMemory(final long p0);
    
    public native long reallocateMemory(final long p0, final long p1);
    
    public native void setMemory(final Object p0, final long p1, final long p2, final byte p3);
    
    public void setMemory(final long address, final long bytes, final byte value) {
        this.setMemory(null, address, bytes, value);
    }
    
    public native void copyMemory(final Object p0, final long p1, final Object p2, final long p3, final long p4);
    
    public void copyMemory(final long srcAddress, final long destAddress, final long bytes) {
        this.copyMemory(null, srcAddress, null, destAddress, bytes);
    }
    
    public native void freeMemory(final long p0);
    
    @Deprecated
    public int fieldOffset(final Field f) {
        if (Modifier.isStatic(f.getModifiers())) {
            return (int)this.staticFieldOffset(f);
        }
        return (int)this.objectFieldOffset(f);
    }
    
    @Deprecated
    public Object staticFieldBase(final Class c) {
        final Field[] fields = c.getDeclaredFields();
        for (int i = 0; i < fields.length; ++i) {
            if (Modifier.isStatic(fields[i].getModifiers())) {
                return this.staticFieldBase(fields[i]);
            }
        }
        return null;
    }
    
    public native long staticFieldOffset(final Field p0);
    
    public native long objectFieldOffset(final Field p0);
    
    public native Object staticFieldBase(final Field p0);
    
    public native void ensureClassInitialized(final Class p0);
    
    public native int arrayBaseOffset(final Class p0);
    
    public native int arrayIndexScale(final Class p0);
    
    public native int addressSize();
    
    public native int pageSize();
    
    public native Class defineClass(final String p0, final byte[] p1, final int p2, final int p3, final ClassLoader p4, final ProtectionDomain p5);
    
    public native Class defineClass(final String p0, final byte[] p1, final int p2, final int p3);
    
    public native Class defineAnonymousClass(final Class p0, final byte[] p1, final Object[] p2);
    
    public native Object allocateInstance(final Class p0) throws InstantiationException;
    
    public native void monitorEnter(final Object p0);
    
    public native void monitorExit(final Object p0);
    
    public native boolean tryMonitorEnter(final Object p0);
    
    public native void throwException(final Throwable p0);
    
    public final native boolean compareAndSwapObject(final Object p0, final long p1, final Object p2, final Object p3);
    
    public final native boolean compareAndSwapInt(final Object p0, final long p1, final int p2, final int p3);
    
    public final native boolean compareAndSwapLong(final Object p0, final long p1, final long p2, final long p3);
    
    public native Object getObjectVolatile(final Object p0, final long p1);
    
    public native void putObjectVolatile(final Object p0, final long p1, final Object p2);
    
    public native int getIntVolatile(final Object p0, final long p1);
    
    public native void putIntVolatile(final Object p0, final long p1, final int p2);
    
    public native boolean getBooleanVolatile(final Object p0, final long p1);
    
    public native void putBooleanVolatile(final Object p0, final long p1, final boolean p2);
    
    public native byte getByteVolatile(final Object p0, final long p1);
    
    public native void putByteVolatile(final Object p0, final long p1, final byte p2);
    
    public native short getShortVolatile(final Object p0, final long p1);
    
    public native void putShortVolatile(final Object p0, final long p1, final short p2);
    
    public native char getCharVolatile(final Object p0, final long p1);
    
    public native void putCharVolatile(final Object p0, final long p1, final char p2);
    
    public native long getLongVolatile(final Object p0, final long p1);
    
    public native void putLongVolatile(final Object p0, final long p1, final long p2);
    
    public native float getFloatVolatile(final Object p0, final long p1);
    
    public native void putFloatVolatile(final Object p0, final long p1, final float p2);
    
    public native double getDoubleVolatile(final Object p0, final long p1);
    
    public native void putDoubleVolatile(final Object p0, final long p1, final double p2);
    
    public native void putOrderedObject(final Object p0, final long p1, final Object p2);
    
    public native void putOrderedInt(final Object p0, final long p1, final int p2);
    
    public native void putOrderedLong(final Object p0, final long p1, final long p2);
    
    public native void unpark(final Object p0);
    
    public native void park(final boolean p0, final long p1);
    
    public native int getLoadAverage(final double[] p0, final int p1);
}
