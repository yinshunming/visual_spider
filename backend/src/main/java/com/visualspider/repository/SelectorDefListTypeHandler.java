package com.visualspider.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.visualspider.domain.SelectorDef;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import java.sql.Types;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * MyBatis TypeHandler：将 List<SelectorDef> 序列化/反序列化为 PostgreSQL JSONB
 */
public class SelectorDefListTypeHandler implements TypeHandler<List<SelectorDef>> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<SelectorDef>> TYPE_REF = new TypeReference<>() {};

    @Override
    public void setParameter(PreparedStatement ps, int i, List<SelectorDef> param, JdbcType jdbcType)
            throws SQLException {
        if (param == null) {
            ps.setNull(i, Types.OTHER);
        } else {
            try {
                String json = OBJECT_MAPPER.writeValueAsString(param);
                ps.setObject(i, json);
            } catch (JsonProcessingException e) {
                throw new TypeException("Failed to serialize SelectorDef list", e);
            }
        }
    }

    @Override
    public List<SelectorDef> getResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }

    @Override
    public List<SelectorDef> getResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJson(json);
    }

    @Override
    public List<SelectorDef> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJson(json);
    }

    private List<SelectorDef> parseJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return OBJECT_MAPPER.readValue(json, TYPE_REF);
        } catch (JsonProcessingException e) {
            throw new TypeException("Failed to deserialize SelectorDef list", e);
        }
    }
}
