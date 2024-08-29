package com.testing.product.base;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class BaseParamFilter<T> {

    public Query buildQuery(WebRequest request, Class<T> entityClass) {

        Query query = new Query();

        List<Criteria> criteriaList = new ArrayList<>();

        Map<String, String[]> parameterMap = request.getParameterMap();

        String gop = request.getParameter("gop");

        GlobalOperator globalOperator;

        if (gop == null || gop.trim().isEmpty()) {

            globalOperator = GlobalOperator.AND;
        } else {
            globalOperator = GlobalOperator.fromString(gop);
        }

        // Iterate through parameters
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {

            String paramName = entry.getKey();

            if (paramName.equals("gop") || paramName.equals("pageNumber") || paramName.equals("pageSize") || paramName.equals("monthYear")) {
                continue;
            }

            String paramValue = entry.getValue().toString();

            String columnName = extractColumnName(paramName);

            Operation operation = detectOperation(paramName);

            criteriaList.add(createCriteria(entityClass,columnName,operation,paramValue));
        }


        Criteria criteria = new Criteria();

        if (globalOperator == GlobalOperator.AND) {
            criteria.andOperator(criteriaList.toArray(new Criteria[0]));
        } else {
            criteria.orOperator(criteriaList.toArray(new Criteria[0]));
        }

        query.addCriteria(criteria);

        return  query;

    }

    private static String extractColumnName(String paramName) {

        if (paramName.endsWith("_like")) {

            return paramName.substring(0, paramName.length() - 5);

        } else if (paramName.endsWith("_in")) {

            return paramName.substring(0, paramName.length() - 3);

        } else if (paramName.endsWith("_gt")) {

            return paramName.substring(0, paramName.length() - 3);

        } else if (paramName.endsWith("_lt")) {

            return paramName.substring(0, paramName.length() - 3);

        } else if (paramName.endsWith("_between")) {

            return paramName.substring(0, paramName.length() - 8);

        } else {
            return paramName;
        }
    }

    private static Operation detectOperation(String paramName) {
        if (paramName.endsWith("_like")) {

            return Operation.LIKE;

        } else if (paramName.endsWith("_in")) {

            return Operation.IN;

        } else if (paramName.endsWith("_gt")) {

            return Operation.GREATER_THAN;

        } else if (paramName.endsWith("_lt")) {

            return Operation.LESS_THAN;

        } else if (paramName.endsWith("_between")) {

            return Operation.BETWEEN;

        } else {
            return Operation.EQUAL;
        }
    }

    private Criteria createCriteria(Class<T> entityClass, String columnName, Operation operation, String value) {

        switch (operation) {

            case IN -> {
                return Criteria.where(columnName).in(parseValue(entityClass,columnName,value));
            }

            case LIKE -> {
                return Criteria.where(columnName).regex(".*" + parseValue(entityClass, columnName, value) + ".*", "i");
            }

            case EQUAL -> {
                return Criteria.where(columnName).is(parseValue(entityClass,columnName,value));
            }
            case BETWEEN -> {
                return Criteria.where(columnName).in(parseValue(entityClass,columnName,value));
            }

            case LESS_THAN -> {
                return Criteria.where(columnName).lt(parseValue(entityClass,columnName,value));
            }

            case GREATER_THAN -> {
                return Criteria.where(columnName).gt(parseValue(entityClass,columnName,value));
            }
            default -> {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,String.format("Invalid operator"));
            }

        }
    }

    private  Object parseValue(Class<T> entityClass,String column,String value) {

        Field field;
        try {
            field = entityClass.getDeclaredField(column);
        }catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Invalid column: " + column, e);
        }

        Class<?> type =field.getType();

        if (type == String.class) {
            return value;
        } else if (type == Integer.class || type == int.class) {
            return Integer.parseInt(value);
        } else if (type == Double.class || type == double.class) {
            return Double.parseDouble(value);
        } else if (type == Boolean.class || type == boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == LocalDate.class) {
            return LocalDate.parse(value);
        } else if (type == LocalTime.class) {
            return LocalTime.parse(value);
        } else if (type == LocalDateTime.class) {
            return LocalDateTime.parse(value);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type.getSimpleName());
        }
    }


    public enum Operation {
        EQUAL, LIKE, IN, GREATER_THAN, LESS_THAN, BETWEEN;

        public static Operation fromString(String operation) {
            try {
                return Operation.valueOf(operation.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid operation: " + operation);
            }
        }
    }

    public enum GlobalOperator {
        AND, OR;

        public static GlobalOperator fromString(String operator) {
            try {
                return GlobalOperator.valueOf(operator.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid global operator: " + operator);
            }
        }
    }

}
