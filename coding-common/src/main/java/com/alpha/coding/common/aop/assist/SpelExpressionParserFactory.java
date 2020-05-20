package com.alpha.coding.common.aop.assist;

import java.lang.reflect.Constructor;

import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import lombok.extern.slf4j.Slf4j;

/**
 * SpelExpressionParserFactory
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class SpelExpressionParserFactory {

    private static SpelExpressionParser parser;

    static {
        try {
            // since spring 4.1
            Class modeClass = Class.forName("org.springframework.expression.spel.SpelCompilerMode");
            try {
                Constructor<SpelParserConfiguration> c = SpelParserConfiguration.class
                        .getConstructor(modeClass, ClassLoader.class);
                Object mode = modeClass.getField("IMMEDIATE").get(null);
                SpelParserConfiguration config =
                        c.newInstance(mode, SpelExpressionParserFactory.class.getClassLoader());
                parser = new SpelExpressionParser(config);
            } catch (Exception e) {
                log.warn("SpelExpressionParserFactory load error", e);
                parser = new SpelExpressionParser();
            }
        } catch (ClassNotFoundException e) {
            parser = new SpelExpressionParser();
        }
    }

    public SpelExpressionParser getInstance() {
        return parser;
    }

}
