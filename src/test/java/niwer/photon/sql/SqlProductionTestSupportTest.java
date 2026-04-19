package niwer.photon.sql;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public final class SqlProductionTestSupportTest {

    private static final ClassLoader PARENT = SqlProductionTestSupportTest.class.getClassLoader();
    private static final ProductionSqlClassLoaderTest LOADER = new ProductionSqlClassLoaderTest(resolveUrls(), PARENT);

    private SqlProductionTestSupportTest() {}

    public static Class<?> load(String className) throws ClassNotFoundException {
        return Class.forName(className, true, LOADER);
    }

    public static Object invokeStatic(String className, String methodName, Class<?>[] parameterTypes, Object... arguments) throws Exception {
        return load(className).getDeclaredMethod(methodName, parameterTypes).invoke(null, arguments);
    }

    public static Object newInstance(String className, Class<?>[] parameterTypes, Object... arguments) throws Exception {
        return load(className).getDeclaredConstructor(parameterTypes).newInstance(arguments);
    }

    public static Class<?> nestedClass(String outerClassName, String nestedSimpleName) throws ClassNotFoundException {
        return load(outerClassName + "$" + nestedSimpleName);
    }

    private static URL[] resolveUrls() {
        try {
            final Path root = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
            return new URL[] {
                root.resolve("build/classes/java/main").toUri().toURL(),
                root.resolve("build/resources/main").toUri().toURL()
            };
        } catch (Exception e) {
            throw new IllegalStateException("Unable to resolve production classpath", e);
        }
    }

    private static final class ProductionSqlClassLoaderTest extends URLClassLoader {

        private ProductionSqlClassLoaderTest(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith("niwer.photon.sql.")) {
                Class<?> loaded = findLoadedClass(name);
                if (loaded == null) {
                    try {
                        loaded = findClass(name);
                    } catch (ClassNotFoundException ignored) {
                        // Fall through to parent.
                    }
                }
                if (loaded != null) {
                    if (resolve) resolveClass(loaded);
                    return loaded;
                }
            }
            return super.loadClass(name, resolve);
        }
    }
}
