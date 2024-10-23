package com.interface21.jdbc.core.sql;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.interface21.jdbc.core.SqlParameterSource;

public class Sql {

    private static final int FIRST_GROUP_INDEX = 1;

    private final String value;

    public Sql(final String value, final SqlParameterSource parameterSource) {
        this.value = bindingParameters(value, parameterSource);
    }

    private String bindingParameters(final String sql, final SqlParameterSource parameterSource) {
        validateParameterSourceIsNull(parameterSource);
        final List<String> bindingParameterNames = parseBindingParameterNames(sql);
        String result = sql;
        for (final String bindingParameterName : bindingParameterNames) {
            final Object parameterValue = parameterSource.getParameter(bindingParameterName);
            result = bindingParameterValue(result, bindingParameterName, parameterValue);
        }

        return result;
    }

    public Sql(final String value, final Map<String, Object> parameters) {
        this.value = bindingParameters(value, parameters);
    }

    private String bindingParameters(final String sql, final Map<String, Object> parameters) {
        validateParametersIsNull(parameters);
        final List<String> bindingParameterNames = parseBindingParameterNames(sql);
        String result = sql;
        for (final String bindingParameterName : bindingParameterNames) {
            final Object parameterValue = parameters.get(bindingParameterName);
            result = bindingParameterValue(result, bindingParameterName, parameterValue);
        }

        return result;
    }

    public Sql(final String value) {
        validateSqlIsNullOrBlank(value);
        this.value = value;
    }

    private void validateSqlIsNullOrBlank(final String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("sql문은 null 혹은 공백이 입력될 수 없습니다. - " + sql);
        }
    }

    private void validateParameterSourceIsNull(final SqlParameterSource parameterSource) {
        if (parameterSource == null) {
            throw new IllegalArgumentException("parameter source는 null이 입력될 수 없습니다.");
        }
    }

    private List<String> parseBindingParameterNames(final String sql) {
        final String regex = ":([a-zA-Z_][a-zA-Z0-9_]*)";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(sql);
        final List<String> bindingParameterNames = new ArrayList<>();
        while (matcher.find()) {
            bindingParameterNames.add(matcher.group(FIRST_GROUP_INDEX));
        }

        return bindingParameterNames;
    }

    private String bindingParameterValue(
            final String sql,
            final String parameterName,
            final Object parameterValue
    ) {
        final String value = convertStringValue(parameterValue);
        if (parameterValue instanceof String) {
            return sql.replace(":" + parameterName, "'" + value + "'");
        }

        if (parameterValue instanceof final LocalDateTime dateTime) {
            final String dateTimeValue = "'" + Timestamp.valueOf(dateTime) + "'";
            return sql.replace(":" + parameterName, dateTimeValue);
        }

        return sql.replace(":" + parameterName, value);
    }

    private String convertStringValue(final Object value) {
        try {
            return String.valueOf(value);
        } catch (final Exception e) {
            throw new IllegalArgumentException("문자열로 변활할 수 없는 파라미터입니다.");
        }
    }

    private void validateParametersIsNull(final Map<String, Object> parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameter map은 null이 입력될 수 없습니다.");
        }
    }

    public String getValue() {
        return value;
    }
}
