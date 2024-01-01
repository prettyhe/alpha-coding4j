package com.alpha.coding.common.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * CommonDDLMapper
 *
 * @version 1.0
 * Date: 2023/7/19
 */
@Mapper
public interface CommonDDLMapper {

    /**
     * judge table exist
     * <p>
     * for MySQL just like this:
     * <pre>{@code SELECT COUNT(*) FROM information_schema.TABLES WHERE LCASE(TABLE_SCHEMA) = #{tableSchema} AND LCASE(TABLE_NAME) = #{tableName}}</pre>
     *
     * @param existStatement exist table statement
     * @param tableSchema    table schema
     * @param tableName      table name
     * @return exist result
     */
    @Select({"${existStatement}"})
    int existTable(@Param("existStatement") String existStatement,
                   @Param("tableSchema") String tableSchema, @Param("tableName") String tableName);

    /**
     * truncate table
     *
     * @param tableName table name
     * @return truncate result
     */
    @Update({"<script>",
            "TRUNCATE TABLE ${tableName}",
            "</script>"})
    int truncateTable(@Param("tableName") String tableName);

    /**
     * drop table
     *
     * @param tableName table name
     * @return drop result
     */
    @Update({"<script>",
            "DROP TABLE IF EXISTS ${tableName}",
            "</script>"})
    int dropTable(@Param("tableName") String tableName);

    /**
     * create table
     *
     * @param createStatement create table statement
     * @return create result
     */
    @Update({"${createStatement}"})
    int createTable(@Param("createStatement") String createStatement);

    /**
     * alter table
     *
     * @param alterStatement alter table statement
     * @return alter result
     */
    @Update({"${alterStatement}"})
    int alterTable(@Param("alterStatement") String alterStatement);

    /**
     * create table like exist table
     *
     * @param newTableName   new table name
     * @param existTableName exist table name
     */
    @Update({"<script>",
            "CREATE TABLE ${newTableName} LIKE ${existTableName}",
            "</script>"})
    int createTableLike(@Param("newTableName") String newTableName, @Param("existTableName") String existTableName);

}
