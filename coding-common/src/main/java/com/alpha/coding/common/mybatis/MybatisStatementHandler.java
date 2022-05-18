package com.alpha.coding.common.mybatis;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;

/**
 * MybatisStatementHandler
 *
 * @version 1.0
 * Date: 2022/5/10
 */
public class MybatisStatementHandler {

    private final MetaObject statementHandler;

    MybatisStatementHandler(MetaObject statementHandler) {
        this.statementHandler = statementHandler;
    }

    public ParameterHandler parameterHandler() {
        return (ParameterHandler) this.get("parameterHandler");
    }

    public MappedStatement mappedStatement() {
        return (MappedStatement) this.get("mappedStatement");
    }

    public Executor executor() {
        return (Executor) this.get("executor");
    }

    public BoundSql boundSql() {
        return (BoundSql) this.get("boundSql");
    }

    public Configuration configuration() {
        return (Configuration) this.get("configuration");
    }

    private <T> T get(String property) {
        return (T) this.statementHandler.getValue(property);
    }

}
