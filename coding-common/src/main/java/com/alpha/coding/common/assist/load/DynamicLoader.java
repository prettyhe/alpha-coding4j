package com.alpha.coding.common.assist.load;

import java.util.Arrays;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * DynamicLoader
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class DynamicLoader {

    /**
     * @param javaName the name of your public class,eg: <code>com.example.TestClass.java</code>
     * @param javaSrc  source code string
     *
     * @return return the Map, the KEY means ClassName, the VALUE means bytecode.
     */
    public static Map<String, byte[]> compile(String javaName, String javaSrc) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager stdManager = compiler.getStandardFileManager(null, null, null);
        try (MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager)) {
            JavaFileObject javaFileObject = manager.makeStringSource(javaName, javaSrc);
            JavaCompiler.CompilationTask task =
                    compiler.getTask(null, manager, null, null, null, Arrays.asList(javaFileObject));
            if (task.call()) {
                return manager.getClassBytes();
            }
        } catch (Exception e) {
            log.error("dynamic compile java error for {}", javaName, e);
        }
        return null;
    }

}
