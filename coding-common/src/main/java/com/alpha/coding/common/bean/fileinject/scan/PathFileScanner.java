package com.alpha.coding.common.bean.fileinject.scan;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.StringSubstitutor;

import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.common.bean.fileinject.scan.filter.ScanFilter;
import com.alpha.coding.common.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

/**
 * PathFileScanner
 *
 * @version 1.0
 * Date: 2020-03-18
 */
@Slf4j
public class PathFileScanner {

    private final String[] basePath;
    private final List<Tuple<ScanFilter, Object>> includeFilters = new LinkedList<>();
    private final List<Tuple<ScanFilter, Object>> excludeFilters = new LinkedList<>();

    public PathFileScanner(String[] basePath) {
        this.basePath = basePath;
    }

    public PathFileScanner addIncludeScanFilter(Tuple<ScanFilter, Object> filterDefinition) {
        this.includeFilters.add(filterDefinition);
        return this;
    }

    public PathFileScanner addExcludeScanFilter(Tuple<ScanFilter, Object> filterDefinition) {
        this.excludeFilters.add(filterDefinition);
        return this;
    }

    public List<File> scan() throws IOException {
        if (this.basePath == null || this.basePath.length == 0) {
            return Collections.emptyList();
        }
        List<File> list = Lists.newArrayList();
        Map<String, Object> properties = Maps.newHashMap();
        System.getProperties().forEach((k, v) -> properties.put(String.valueOf(k), v));
        for (String path : basePath) {
            StringSubstitutor substitutor = new StringSubstitutor(properties);
            substitutor.setEnableSubstitutionInVariables(true);
            String realPath = substitutor.replace(path);
            if (StringUtils.isBlank(realPath)) {
                log.warn("Invalid path {}", path);
                continue;
            }
            doScan(new File(realPath), list);
        }
        return list.stream()
                .filter(file -> excludeFilters.size() == 0
                        || !excludeFilters.stream().filter(f -> f.getF().match(file, f.getS())).findAny().isPresent())
                .filter(file -> includeFilters.size() == 0
                        || !includeFilters.stream().filter(f -> !f.getF().match(file, f.getS())).findAny().isPresent())
                .collect(Collectors.toList());
    }

    private void doScan(File file, List<File> list) throws IOException {
        if (!file.exists()) {
            log.warn("File {} not exist", file.getCanonicalPath());
            return;
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                doScan(f, list);
            }
        } else {
            list.add(file);
        }
    }

}
