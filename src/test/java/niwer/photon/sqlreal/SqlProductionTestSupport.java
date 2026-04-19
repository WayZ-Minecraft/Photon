package niwer.photon.sqlreal;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Objects;

final class SqlProductionTestSupport {

    private static final ClassLoader PARENT = SqlProductionTestSupport.class.getClassLoader();
    private static final ProductionSqlClassLoader LOADER = new ProductionSqlClassLoader(resolveUrls(), PARENT);

    private SqlProductionTestSupport() {}

    static Class<?> load(String className) throws ClassNotFoundException {
        return Class.forName(className, true, LOADER);
    }

    static Object invokeStatic(String className, String methodName, Class<?>[] parameterTypes, Object... arguments) throws Exception {
        return load(className).getDeclaredMethod(methodName, parameterTypes).invoke(null, arguments);
    }

    static Object newInstance(String className, Class<?>[] parameterTypes, Object... arguments) throws Exception {
        return load(className).getDeclaredConstructor(parameterTypes).newInstance(arguments);
    }

    static Class<?> nestedClass(String outerClassName, String nestedSimpleName) throws ClassNotFoundException {
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

    private static final class ProductionSqlClassLoader extends URLClassLoader {

        private ProductionSqlClassLoader(URL[] urls, ClassLoader parent) {
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