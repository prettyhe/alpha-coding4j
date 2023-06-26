package com.alpha.coding.common.bean.fileinject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.springframework.core.annotation.AnnotationAttributes;

import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.common.bean.fileinject.annotation.EnableFileInject;
import com.alpha.coding.common.bean.fileinject.annotation.EnableFileInjects;
import com.alpha.coding.common.bean.fileinject.scan.PathFileScanner;
import com.alpha.coding.common.bean.fileinject.scan.filter.RegexPatternScanFilter;
import com.alpha.coding.common.bean.fileinject.scan.filter.ScanFilter;
import com.alpha.coding.common.bean.spi.ConfigurationRegisterHandler;
import com.alpha.coding.common.bean.spi.RegisterBeanDefinitionContext;
import com.alpha.coding.common.utils.IOUtils;
import com.alpha.coding.common.utils.SpringAnnotationConfigUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * EnableFileInjectRegisterHandler
 *
 * @version 1.0
 * Date: 2020-03-18
 */
@Slf4j
public class EnableFileInjectRegisterHandler implements ConfigurationRegisterHandler {

    private static final String TMP_PATH_DIR = IOUtils.joinPath(System.getProperty("java.io.tmpdir"),
            "tmp_path_" + UUID.randomUUID().toString().replaceAll("-", ""));

    @Override
    public void registerBeanDefinitions(RegisterBeanDefinitionContext context) {
        Set<AnnotationAttributes> annotationAttributes = SpringAnnotationConfigUtils.attributesForRepeatable(
                context.getImportingClassMetadata(), EnableFileInjects.class, EnableFileInject.class);
        if (annotationAttributes.isEmpty()) {
            return;
        }
        for (AnnotationAttributes extFileInject : annotationAttributes) {
            final String[] basePaths = extFileInject.getStringArray("basePath");
            PathFileScanner scanner = new PathFileScanner(basePaths);
            for (AnnotationAttributes filter : extFileInject.getAnnotationArray("includeFilters")) {
                parseFilters(filter).forEach(scanner::addIncludeScanFilter);
            }
            for (AnnotationAttributes filter : extFileInject.getAnnotationArray("excludeFilters")) {
                parseFilters(filter).forEach(scanner::addExcludeScanFilter);
            }
            try {
                final List<File> files = scanner.scan();
                if (log.isDebugEnabled()) {
                    log.debug("Scan files: {}",
                            files.stream().map(this::getFilePath).reduce((x, y) -> x + "," + y).orElse(""));
                }
                injectFilesToClasspath(files);
            } catch (IOException e) {
                log.error("Scan file and inject fail for {}", String.join(",", basePaths), e);
            }
        }
    }

    private List<Tuple<ScanFilter, Object>> parseFilters(AnnotationAttributes filterAttributes) {
        List<Tuple<ScanFilter, Object>> tuples = new ArrayList<>();

        for (String expression : filterAttributes.getStringArray("pattern")) {
            tuples.add(new Tuple<>(new RegexPatternScanFilter(f -> ((File) f).getName(), String::valueOf),
                    expression));
        }

        return tuples;
    }

    private synchronized void injectFilesToClasspath(List<File> files) throws IOException {
        File file = new File(TMP_PATH_DIR);
        if (!file.exists()) {
            file.mkdirs();
            if (log.isDebugEnabled()) {
                log.debug("config file dir is {}", TMP_PATH_DIR);
            }
        }
        for (File f : files) {
            File destFile = new File(IOUtils.joinPath(TMP_PATH_DIR, f.getName()));
            if (destFile.exists()) {
                destFile.delete();
            }
            FileUtils.copyFileToDirectory(f, file);
        }
        ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[] {file.toURI().toURL()}, currentThreadClassLoader);
        Thread.currentThread().setContextClassLoader(urlClassLoader);
    }

    private String getFilePath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            return file.getAbsolutePath();
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
