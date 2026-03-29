package com.visualspider.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.visualspider.domain.FieldValidation;
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
 * MyBatis TypeHandler：将 List<FieldValidation> 序列化/反序列化为 PostgreSQL JSONB
 */
public class FieldValidationListTypeHandler implements TypeHandler<List<FieldValidation>> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<FieldValidation>> TYPE_REF = new TypeReference<>() {};

    @Override
    public void setParameter(PreparedStatement ps, int i, List<FieldValidation> param, JdbcType jdbcType)
            throws SQLException {
        if (param == null) {
            ps.setNull(i, Types.OTHER);
        } else {
            try {
                String json = OBJECT_MAPPER.writeValueAsString(param);
                ps.setString(i, json);
            } catch (JsonProcessingException e) {
                throw new TypeException("Failed to serialize FieldValidation list", e);
            }
        }
    }

    @Override
    public List<FieldValidation> getResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }

    @Override
    public List<FieldValidation> getResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJson(json);
    }

    @Override
    public List<FieldValidation> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJson(json);
    }

    private List<FieldValidation> parseJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return OBJECT_MAPPER.readValue(json, TYPE_REF);
        } catch (JsonProcessingException e) {
            throw new TypeException("Failed to deserialize FieldValidation list", e);
        }
    }
}
