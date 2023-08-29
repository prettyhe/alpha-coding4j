package com.alpha.coding.common.mybatis.mapper;

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
     * @param record          record for sql, like: (a=xxx)
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
     * @param record    record for insert, like: (column_a=xxx,column_b=yyy)
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
     * @param record    record for insert, like: (column_a=xxx,column_b=yyy)
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
            "ON DUPLICATE KEY UPDATE",
            "  <trim suffixOverrides=','>",
            "    <foreach collection='record' index='key' item='value' separator=','>",
            "      <if test='value != null'>${key} = #{value}</if>",
            "    </foreach>",
            "  </trim>",
            "</script>"})
    int insertOrUpdateSelective(@Param("tableName") String tableName, @Param("record") Map record);

    /**
     * execute batch insert, insert columns base on the given column names
     *
     * @param tableName   table name
     * @param columnNames table column names, like: [column_a, column_b]
     * @param records     records for insert, like: [(xxx,yyy), (mmm,nnn)]
     * @return insert result
     */
    @Insert({"<script>",
            "INSERT INTO ${tableName}",
            "  <trim prefix='(' suffix=')' suffixOverrides=','>",
            "    <foreach collection='columnNames' index='index' item='name' separator=','>",
            "      ${name}",
            "    </foreach>",
            "  </trim>",
            "VALUES",
            "  <foreach collection='records' item='record' open='' separator=',' close=''>",
            "    <foreach collection='record' index='index' item='value' open='(' separator=',' close=')'>",
            "      #{value}",
            "    </foreach>",
            "  </foreach>",
            "</script>"})
    int batchInsert(@Param("tableName") String tableName, @Param("columnNames") String[] columnNames,
                    @Param("records") List<Object[]> records);

    /**
     * execute batch insert selective, insert columns base on the first record keys
     *
     * @param tableName table name
     * @param records   records for insert, like: [(column_a=xxx,column_b=yyy),(column_a=mmm,column_b=nnn)]
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
     * execute batch insert or update, insert columns base on the given column names
     *
     * @param tableName   table name
     * @param columnNames table column names, like: [column_a, column_b]
     * @param records     records for insert, like: [(xxx,yyy), (mmm,nnn)]
     * @return insert result
     */
    @Insert({"<script>",
            "INSERT INTO ${tableName}",
            "  <trim prefix='(' suffix=')' suffixOverrides=','>",
            "    <foreach collection='columnNames' index='index' item='name' separator=','>",
            "      ${name}",
            "    </foreach>",
            "  </trim>",
            "VALUES",
            "  <foreach collection='records' item='record' open='' separator=',' close=''>",
            "    <foreach collection='record' index='index' item='value' open='(' separator=',' close=')'>",
            "      #{value}",
            "    </foreach>",
            "  </foreach>",
            "ON DUPLICATE KEY UPDATE",
            "  <trim suffixOverrides=','>",
            "    <foreach collection='columnNames' index='index' item='name' separator=','>",
            "      ${name} = values(${name})",
            "    </foreach>",
            "  </trim>",
            "</script>"})
    int batchInsertOrUpdate(@Param("tableName") String tableName, @Param("columnNames") String[] columnNames,
                            @Param("records") List<Object[]> records);

    /**
     * execute batch insert or update selective, insert columns base on the first record keys
     *
     * @param tableName table name
     * @param records   records for insert, like: [(column_a=xxx,column_b=yyy),(column_a=mmm,column_b=nnn)]
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
            "ON DUPLICATE KEY UPDATE",
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
     * @param conditionStatement condition statement, like: column_a = #{params.a}
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
     * @param primaryKeyName  primaryKey name
     * @param primaryKeyValue primaryKey value
     * @return delete result
     */
    @Delete({"<script>",
            "DELETE FROM ${tableName}",
            "WHERE ${primaryKeyName} = #{primaryKeyValue}",
            "</script>"})
    int deleteByPrimaryKey(@Param("tableName") String tableName, @Param("primaryKeyName") String primaryKeyName,
                           @Param("primaryKeyValue") Object primaryKeyValue);

    /**
     * execute update
     *
     * @param tableName          table name
     * @param record             record for update, like: (column_a=xxx)
     * @param conditionStatement condition statement, like: column_a = #{params.a}
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
            "</set>",
            "<if test='conditionStatement != null and conditionStatement.length() > 0'>",
            "  WHERE ${conditionStatement}",
            "</if>",
            "</script>"})
    int updateByCondition(@Param("tableName") String tableName, @Param("record") Map record,
                          @Param("conditionStatement") String conditionStatement, @Param("params") Map params);

    /**
     * execute update selective by PrimaryKey
     *
     * @param tableName       table name
     * @param record          record for update, like: (column_a=xxx)
     * @param primaryKeyName  primaryKey name
     * @param primaryKeyValue primaryKey value
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
            "</set>",
            "WHERE ${primaryKeyName} = #{primaryKeyValue}",
            "</script>"})
    int updateByPrimaryKey(@Param("tableName") String tableName, @Param("record") Map record,
                           @Param("primaryKeyName") String primaryKeyName,
                           @Param("primaryKeyValue") Object primaryKeyValue);

    /**
     * execute update selective by PrimaryKey, value of PrimaryKey must in record
     *
     * @param tableName      table name
     * @param record         record for update, like: (column_primary_key=1,column_a=xxx)
     * @param primaryKeyName primaryKey name
     * @return update result
     * @see CommonMapMapper#updateByPrimaryKey(String, Map, String, Object)
     */
    default int updateByPrimaryKey(String tableName, Map record, String primaryKeyName) {
        Object primaryKeyValue = record.get(primaryKeyName);
        Map dataRecord = new LinkedHashMap(record);
        dataRecord.remove(primaryKeyName);
        return updateByPrimaryKey(tableName, dataRecord, primaryKeyName, primaryKeyValue);
    }

    /**
     * execute update selective
     *
     * @param tableName          table name
     * @param record             record for update, like: (column_a=xxx)
     * @param conditionStatement condition statement, like: column_a = #{params.a}
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
            "</set>",
            "<if test='conditionStatement != null and conditionStatement.length() > 0'>",
            "  WHERE ${conditionStatement}",
            "</if>",
            "</script>"})
    int updateSelectiveByCondition(@Param("tableName") String tableName, @Param("record") Map record,
                                   @Param("conditionStatement") String conditionStatement, @Param("params") Map params);

    /**
     * execute update selective by PrimaryKey
     *
     * @param tableName       table name
     * @param record          record for update, like: (column_a=xxx)
     * @param primaryKeyName  primaryKey name
     * @param primaryKeyValue primaryKey value
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
            "</set>",
            "WHERE ${primaryKeyName} = #{primaryKeyValue}",
            "</script>"})
    int updateSelectiveByPrimaryKey(@Param("tableName") String tableName, @Param("record") Map record,
                                    @Param("primaryKeyName") String primaryKeyName,
                                    @Param("primaryKeyValue") Object primaryKeyValue);

    /**
     * execute update selective by PrimaryKey, value of PrimaryKey must in record
     *
     * @param tableName      table name
     * @param record         record for update, like: (column_primary_key=1,column_a=xxx)
     * @param primaryKeyName primaryKey name
     * @return update result
     * @see CommonMapMapper#updateSelectiveByPrimaryKey(String, Map, String, Object)
     */
    default int updateSelectiveByPrimaryKey(String tableName, Map record, String primaryKeyName) {
        final Object primaryKeyValue = record.get(primaryKeyName);
        Map dataRecord = new LinkedHashMap(record);
        dataRecord.remove(primaryKeyName);
        return updateSelectiveByPrimaryKey(tableName, dataRecord, primaryKeyName, primaryKeyValue);
    }

    /**
     * execute count
     *
     * @param tableName          table name
     * @param columnName         column name
     * @param conditionStatement condition statement, like: column_a = #{params.a}
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
     * @param conditionStatement condition statement, like: column_a = #{params.a}
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
     * @param primaryKeyName  primaryKey name
     * @param primaryKeyValue primaryKey value
     * @return select result
     */
    @Select({"<script>",
            "SELECT",
            "<foreach collection='columnNames' index='index' item='item' separator=','>",
            "  ${item}",
            "</foreach>",
            "FROM ${tableName}",
            "WHERE ${primaryKeyName} = #{primaryKeyValue}",
            "</script>"})
    Map selectByPrimaryKey(@Param("tableName") String tableName, @Param("columnNames") List<String> columnNames,
                           @Param("primaryKeyName") String primaryKeyName,
                           @Param("primaryKeyValue") Object primaryKeyValue);

    /**
     * execute select many
     *
     * @param tableName          table name
     * @param columnNames        column names
     * @param conditionStatement condition statement, like: column_a = #{params.a}
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
