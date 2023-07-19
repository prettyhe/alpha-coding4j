package com.alpha.coding.common.mybatis.mapper;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * CommonMapMapper
 *
 * @version 1.0
 * Date: 2023/7/19
 */
@Mapper
public interface CommonMapMapper {

    /**
     * execute insert by custom statement
     *
     * @param insertStatement insert sql statement, like: insert into table_a(code,name) values(#{record.code},#{
     *                        record.name})
     * @param record          params for sql, like: (a=xxx)
     * @return insert result
     */
    @Insert({"${insertStatement}"})
    int insertByStatement(@Param("insertStatement") String insertStatement, @Param("record") Map record);

    /**
     * execute delete by custom statement
     *
     * @param deleteStatement delete sql statement, like: delete from table_a where column_a = #{params.a}
     * @param params          params for sql, like: (a=xxx)
     * @return delete result
     */
    @Delete({"${deleteStatement}"})
    int deleteByStatement(@Param("deleteStatement") String deleteStatement, @Param("params") Map params);

    /**
     * execute update by custom statement
     *
     * @param updateStatement update sql statement, like: update table_a set column_a = #{record.a} where column_a =
     *                        #{params.a})
     * @param record          set params for sql, like: (a=xxx)
     * @param params          condition params for sql, like: (a=xxx)
     * @return update result
     */
    @Update({"${updateStatement}"})
    int updateByStatement(@Param("updateStatement") String updateStatement, @Param("record") Map record,
                          @Param("params") Map params);

    /**
     * execute count by custom statement
     *
     * @param countStatement select sql statement, like: select count(*) from table_a where column_a = #{params.a}
     * @param params         params for sql, like: (a=xxx)
     * @return count of select result
     */
    @Select({"${countStatement}"})
    long countByStatement(@Param("countStatement") String countStatement, @Param("params") Map params);

    /**
     * execute select one by custom statement
     *
     * @param selectStatement select sql statement, like: select * from table_a where column_a = #{params.a}
     * @param params          params for sql, like: (a=xxx)
     * @return select result
     */
    @Select({"${selectStatement}"})
    Map selectOneByStatement(@Param("selectStatement") String selectStatement, @Param("params") Map params);

    /**
     * execute select by custom statement
     *
     * @param selectStatement select sql statement, like: select * from table_a where column_a = #{params.a}
     * @param params          params for sql, like: (a=xxx)
     * @return select results as List of Map
     */
    @Select({"${selectStatement}"})
    List<Map> selectByStatement(@Param("selectStatement") String selectStatement, @Param("params") Map params);

    /**
     * execute insert selective
     *
     * @param tableName table name
     * @param record    params for sql, like: (a=xxx)
     * @return insert result
     */
    @Insert({"<script>",
            "INSERT INTO ${tableName}",
            "  <trim prefix='(' suffix=')' suffixOverrides=','>",
            "    <foreach collection='record' index='key' item='value' separator=','>",
            "      <if test='value != null'>${key}</if>",
            "    </foreach>",
            "  </trim>",
            "VALUES",
            "  <trim prefix='(' suffix=')' suffixOverrides=','>",
            "    <foreach collection='record' index='key' item='value' separator=','>",
            "      <if test='value != null'>#{value}</if>",
            "    </foreach>",
            "  </trim>",
            "</script>"})
    int insertSelective(@Param("tableName") String tableName, @Param("record") Map record);

    /**
     * execute insert or update selective
     *
     * @param tableName table name
     * @param record    params for sql, like: (a=xxx)
     * @return insert result
     */
    @Insert({"<script>",
            "INSERT INTO ${tableName}",
            "  <trim prefix='(' suffix=')' suffixOverrides=','>",
            "    <foreach collection='record' index='key' item='value' separator=','>",
            "      <if test='value != null'>${key}</if>",
            "    </foreach>",
            "  </trim>",
            "VALUES",
            "  <trim prefix='(' suffix=')' suffixOverrides=','>",
            "    <foreach collection='record' index='key' item='value' separator=','>",
            "      <if test='value != null'>#{value}</if>",
            "    </foreach>",
            "  </trim>",
            "ON DUPLICATE KEY UPDATE ",
            "  <trim suffixOverrides=','>",
            "    <foreach collection='record' index='key' item='value' separator=','>",
            "      <if test='value != null'>${key} = #{value}</if>",
            "    </foreach>",
            "  </trim>",
            "</script>"})
    int insertOrUpdateSelective(@Param("tableName") String tableName, @Param("record") Map record);

    /**
     * execute batch insert selective
     *
     * @param tableName table name
     * @param records   records
     * @return insert result
     */
    @Insert({"<script>",
            "INSERT INTO ${tableName}",
            "  <trim prefix='(' suffix=')' suffixOverrides=','>",
            "    <foreach collection='records[0]' index='colKey' item='value' separator=','>",
            "      <if test='value != null'>${colKey}</if>",
            "    </foreach>",
            "  </trim>",
            "VALUES",
            "  <foreach collection='records' item='record' separator=','>",
            "    <foreach collection='records[0]' index='colKey' item='value' open='(' separator=',' close=')'>",
            "      <foreach collection='record' index='dataKey' item='dataValue' separator=','>",
            "        <if test='value != null and colKey == dataKey'>#{dataValue}</if>",
            "      </foreach>",
            "    </foreach>",
            "  </foreach>",
            "</script>"})
    int batchInsertSelective(@Param("tableName") String tableName, @Param("records") List<Map> records);

    /**
     * execute batch insert or update selective
     *
     * @param tableName table name
     * @param records   records
     * @return insert result
     */
    @Insert({"<script>",
            "INSERT INTO ${tableName}",
            "  <trim prefix='(' suffix=')' suffixOverrides=','>",
            "    <foreach collection='records[0]' index='colKey' item='value' separator=','>",
            "      <if test='value != null'>${colKey}</if>",
            "    </foreach>",
            "  </trim>",
            "VALUES",
            "  <foreach collection='records' item='record' separator=','>",
            "    <foreach collection='records[0]' index='colKey' item='value' open='(' separator=',' close=')'>",
            "      <foreach collection='record' index='dataKey' item='dataValue' separator=','>",
            "        <if test='value != null and colKey == dataKey'>#{dataValue}</if>",
            "      </foreach>",
            "    </foreach>",
            "  </foreach>",
            "ON DUPLICATE KEY UPDATE ",
            "  <trim suffixOverrides=','>",
            "    <foreach collection='records[0]' index='colKey' item='value' separator=','>",
            "      <if test='value != null'>${colKey} = values(${colKey})</if>",
            "    </foreach>",
            "  </trim>",
            "</script>"})
    int batchInsertOrUpdateSelective(@Param("tableName") String tableName, @Param("records") List<Map> records);

    /**
     * execute delete
     *
     * @param tableName          table name
     * @param conditionStatement condition statement, like: col_a = #{params.a}
     * @param params             condition statement params for sql, like: (a=xxx)
     * @return delete result
     */
    @Delete({"<script>",
            "DELETE FROM ${tableName}",
            "WHERE ${conditionStatement}",
            "</script>"})
    int deleteByCondition(@Param("tableName") String tableName, @Param("conditionStatement") String conditionStatement,
                          @Param("params") Map params);

    /**
     * execute delete
     *
     * @param tableName       table name
     * @param primaryKeyName  primaryKeyName
     * @param primaryKeyValue primaryKeyValue
     * @return delete result
     * @see CommonMapMapper#deleteByCondition
     */
    default int deleteByPrimaryKey(@Param("tableName") String tableName, String primaryKeyName,
                                   Object primaryKeyValue) {
        Map params = Collections.singletonMap(primaryKeyName, primaryKeyValue);
        return deleteByCondition(tableName, primaryKeyName + " = " + "#{params." + primaryKeyName + "}", params);
    }

    /**
     * execute update
     *
     * @param tableName          table name
     * @param record             set params for sql, like: (a=xxx)
     * @param conditionStatement condition statement, like: col_a = #{params.a}
     * @param params             condition statement params for sql, like: (a=xxx)
     * @return update result
     */
    @Update({"<script>",
            "UPDATE ${tableName}",
            "<set>",
            "  <trim suffixOverrides=','>",
            "    <foreach collection='record' index='key' item='value' separator=','>",
            "      <if test='value != null'>${key} = #{value}</if>",
            "      <if test='value == null'>${key} = NULL</if>",
            "    </foreach>",
            "  </trim>",
            "</set> ",
            "<if test='conditionStatement != null and conditionStatement.length() > 0'>",
            "  WHERE ${conditionStatement}",
            "</if>",
            "</script>"})
    int updateByCondition(@Param("tableName") String tableName, @Param("record") Map record,
                          @Param("conditionStatement") String conditionStatement, @Param("params") Map params);

    /**
     * execute update selective by PrimaryKey, value of PrimaryKey must in record
     *
     * @param tableName      table name
     * @param record         set params for sql, like: (a=xxx)
     * @param primaryKeyName primaryKey name
     * @return update result
     * @see CommonMapMapper#updateByCondition
     */
    default int updateByPrimaryKey(String tableName, Map record, String primaryKeyName) {
        Map params = Collections.singletonMap(primaryKeyName, record.get(primaryKeyName));
        Map dataRecord = new LinkedHashMap(record);
        dataRecord.remove(primaryKeyName);
        return updateByCondition(tableName, dataRecord,
                primaryKeyName + " = " + "#{params." + primaryKeyName + "}", params);
    }

    /**
     * execute update selective
     *
     * @param tableName          table name
     * @param record             set params for sql, like: (a=xxx)
     * @param conditionStatement condition statement, like: col_a = #{params.a}
     * @param params             condition statement params for sql, like: (a=xxx)
     * @return update result
     */
    @Update({"<script>",
            "UPDATE ${tableName}",
            "<set>",
            "  <trim suffixOverrides=','>",
            "    <foreach collection='record' index='key' item='value' separator=','>",
            "      <if test='value != null'>${key} = #{value}</if>",
            "    </foreach>",
            "  </trim>",
            "</set> ",
            "<if test='conditionStatement != null and conditionStatement.length() > 0'>",
            "  WHERE ${conditionStatement}",
            "</if>",
            "</script>"})
    int updateSelectiveByCondition(@Param("tableName") String tableName, @Param("record") Map record,
                                   @Param("conditionStatement") String conditionStatement, @Param("params") Map params);

    /**
     * execute update selective by PrimaryKey, value of PrimaryKey must in record
     *
     * @param tableName      table name
     * @param record         set params for sql, like: (a=xxx)
     * @param primaryKeyName primaryKey name
     * @return update result
     * @see CommonMapMapper#updateSelectiveByCondition
     */
    default int updateSelectiveByPrimaryKey(String tableName, Map record, String primaryKeyName) {
        Map params = Collections.singletonMap(primaryKeyName, record.get(primaryKeyName));
        Map dataRecord = new LinkedHashMap(record);
        dataRecord.remove(primaryKeyName);
        return updateSelectiveByCondition(tableName, dataRecord,
                primaryKeyName + " = " + "#{params." + primaryKeyName + "}", params);
    }

    /**
     * execute count
     *
     * @param tableName          table name
     * @param columnName         column name
     * @param conditionStatement condition statement, like: col_a = #{params.a}
     * @param params             condition statement params for sql, like: (a=xxx)
     * @return count result
     */
    @Select({"<script>",
            "SELECT COUNT(${columnName}) FROM ${tableName}",
            "<if test='conditionStatement != null and conditionStatement.length() > 0'>",
            "  WHERE ${conditionStatement}",
            "</if>",
            "</script>"})
    long countByCondition(@Param("tableName") String tableName, @Param("columnName") String columnName,
                          @Param("conditionStatement") String conditionStatement, @Param("params") Map params);

    /**
     * execute select one
     *
     * @param tableName          table name
     * @param columnNames        column names
     * @param conditionStatement condition statement, like: col_a = #{params.a}
     * @param params             condition statement params for sql, like: (a=xxx)
     * @return select result
     */
    @Select({"<script>",
            "SELECT",
            "<foreach collection='columnNames' index='index' item='item' separator=','>",
            "  ${item}",
            "</foreach>",
            "FROM ${tableName}",
            "<if test='conditionStatement != null and conditionStatement.length() > 0'>",
            "  WHERE ${conditionStatement}",
            "</if>",
            "</script>"})
    Map selectOne(@Param("tableName") String tableName, @Param("columnNames") List<String> columnNames,
                  @Param("conditionStatement") String conditionStatement, @Param("params") Map params);

    /**
     * execute select by PrimaryKey
     *
     * @param tableName       table name
     * @param columnNames     column names
     * @param primaryKeyName  primaryKeyName
     * @param primaryKeyValue primaryKeyValue
     * @return select result
     * @see CommonMapMapper#selectOne
     */
    default Map selectByPrimaryKey(@Param("tableName") String tableName, @Param("columnNames") List<String> columnNames,
                                   String primaryKeyName, Object primaryKeyValue) {
        Map params = Collections.singletonMap(primaryKeyName, primaryKeyValue);
        return selectOne(tableName, columnNames, primaryKeyName + " = " + "#{params." + primaryKeyName + "}", params);
    }

    /**
     * execute select many
     *
     * @param tableName          table name
     * @param columnNames        column names
     * @param conditionStatement condition statement, like: col_a = #{params.a}
     * @param params             condition statement params for sql, like: (a=xxx)
     * @return select result
     */
    @Select({"<script>",
            "SELECT",
            "<foreach collection='columnNames' index='index' item='item' separator=','>",
            "  ${item}",
            "</foreach>",
            "FROM ${tableName}",
            "<if test='conditionStatement != null and conditionStatement.length() > 0'>",
            "  WHERE ${conditionStatement}",
            "</if>",
            "</script>"})
    List<Map> selectMany(@Param("tableName") String tableName, @Param("columnNames") List<String> columnNames,
                         @Param("conditionStatement") String conditionStatement, @Param("params") Map params);

}
