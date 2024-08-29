package com.testing.product.base;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class BaseFilter<T> {

    public Query buildQuery(FilterDto filterDto,Class<T> entityClass) {

        //create new query object
        Query query = new Query();

        if (filterDto == null || filterDto.getSpecsDto() == null || filterDto.getSpecsDto().isEmpty()) {
            return query;
        }

        List<Criteria> criteriaList = new ArrayList<>();
        for (FilterDto.SpecsDto specs : filterDto.getSpecsDto()) {
                criteriaList.add(createCriteria(specs,entityClass));
        }

        Criteria criteria = new Criteria();
        if (filterDto.getGlobalOperator() == FilterDto.GlobalOperator.AND) {
            criteria.andOperator(criteriaList.toArray(new Criteria[0]));
        } else {
            criteria.orOperator(criteriaList.toArray(new Criteria[0]));
        }

        query.addCriteria(criteria);

        return query;
    }

    private Criteria createCriteria(FilterDto.SpecsDto specs,Class<T> entityClass) {
        switch (specs.getOperation()) {
            case EQUAL:
                return Criteria.where(specs.getColumn()).is(parseValue(entityClass,specs.getColumn(),specs.getValue()));
            case LIKE:
                return Criteria.where(specs.getColumn()).regex(".*" + parseValue(entityClass, specs.getColumn(), specs.getValue()) + ".*", "i");
            case IN:
                return Criteria.where(specs.getColumn()).in(parseValue(entityClass,specs.getColumn(),specs.getValue()));
            case GREATER_THAN:
                return Criteria.where(specs.getColumn()).gt(parseValue(entityClass,specs.getColumn(),specs.getValue()));
            case LESS_THAN:
                return Criteria.where(specs.getColumn()).lt(parseValue(entityClass,specs.getColumn(),specs.getValue()));
            case BETWEEN:
                return Criteria.where(specs.getColumn()).gte(parseValue(entityClass,specs.getColumn(),
                        specs.getValues().get(0))).lte(parseValue(entityClass,specs.getColumn(),
                        specs.getValues().get(1)));
            case EXISTS:
                return Criteria.where(specs.getColumn()).exists(Boolean.parseBoolean(specs.getValue()));
            case NE:
                return Criteria.where(specs.getColumn()).ne(specs.getValue());
            case SIZE:
                return Criteria.where(specs.getColumn()).size(Integer.parseInt(specs.getValue()));
            case ELEMENT_MATCH:
                return Criteria.where(specs.getColumn()).elemMatch(Criteria.where(specs.getSubField()).is(specs.getValue()));
            default:
                throw new IllegalArgumentException("Unsupported operation: " + specs.getOperation());
        }
    }

    private Object parseValue(Class<T> entityClass,String column,String value) {

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


    @Getter
    @Setter
    public static class FilterDto {
        private List<SpecsDto> specsDto;
        private GlobalOperator globalOperator;

        @Getter
        @Setter
        public static class SpecsDto {
            private String column;
            private String value;
            private List<String> values;
            private String subField;
            private Operation operation;

            public enum Operation {
                EQUAL, LIKE, IN, GREATER_THAN, LESS_THAN, BETWEEN, EXISTS, NE, SIZE, ELEMENT_MATCH
            }
        }

        public enum GlobalOperator {
            AND, OR
        }
    }
}