package com.giraone.io.copier.common;

/**
 * Miscellaneous java.lang.Class utility methods inspired by org.springframework.util.ClassUtils.
 */
public class ClassUtils {

    /**
     * Return the default ClassLoader to use:
     * <ul>
     *     <li>typically the thread context ClassLoader, if available.</li>
     *     <li>otherwise the ClassLoader that loaded this ClassUtils class</li>
     * </ul>
     * Call this method if you intend to use the thread context ClassLoader in a scenario where you clearly prefer a non-null
     * ClassLoader reference: for example, for class path resource loading.
     * @return a class loader or null
     */
    public static ClassLoader getDefaultClassLoader() {

        ClassLoader ret = null;
        try {
            ret = Thread.currentThread().getContextClassLoader();
        }
        catch (Throwable ex) {
            // Cannot get thread context ClassLoader - using fallback...
        }
        if (ret == null) {
            // No thread context class loader -> use class loader of this class.
            ret = ClassUtils.class.getClassLoader();
            if (ret == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader.
                try {
                    ret = ClassLoader.getSystemClassLoader();
                }
                catch (Throwable ex) {
                    // Cannot access system ClassLoader - the caller must live with null.
                }
            }
        }
        return ret;
    }
}
