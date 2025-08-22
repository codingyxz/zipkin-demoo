package com.demoo.plugin.mybatis.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.session.defaults.DefaultSqlSession.StrictMap;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public class SqlSpanUtils {

    private static final String EMPTY = "";

    // ********************拦截MappedStatement*****************
    public static MappedStatement getMappedStatement(Invocation invocation) {
        Object[] target = invocation.getArgs();
        if (target != null && target.length > 0 && target[0] instanceof MappedStatement) {
            return (MappedStatement) target[0];
        }
        return null;
    }

    public static Connection getConnection(MappedStatement mappedStatement) {
        if (null == mappedStatement) {
            return null;
        }
        try {
            return mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
        } catch (Exception e) {

        }
        return null;
    }

    public static String getSql(Invocation invocation) {
        try {
            Object[] target = invocation.getArgs();
            MappedStatement mappedStatement = getMappedStatement(invocation);
            if (target != null && target.length > 1 && mappedStatement != null) {
                Object parameterObject = target[1];
                BoundSql boundSql = mappedStatement.getBoundSql(parameterObject);
                return boundSql.getSql();
            }
        } catch (Exception e) {

        }
        return EMPTY;
    }

    // ********************拦截StatementHandler*****************
    public static Connection getConnection(Invocation invocation) {
        try {
            Object[] target = invocation.getArgs();
            if (target != null && target.length > 0 && target[0] instanceof Connection) {
                return (Connection) target[0];
            }
        } catch (Exception e) {

        }
        return null;
    }

    public static RoutingStatementHandler getStatementHandler(Invocation invocation) {
        if (invocation.getTarget() instanceof RoutingStatementHandler) {
            return (RoutingStatementHandler) invocation.getTarget();
        }
        return null;
    }

    public static String getDaoMethod(Invocation invocation) {
        try {
            RoutingStatementHandler statementHandler = getStatementHandler(invocation);
            Object ob = ReflectUtil.getFieldValue(statementHandler, "delegate");
            if (ob instanceof StatementHandler) {
                StatementHandler delegate = (StatementHandler) ob;
                Object ob1 = ReflectUtil.getFieldValue(delegate, "mappedStatement");
                if (ob1 instanceof MappedStatement) {
                    MappedStatement mappedStatement = (MappedStatement) ob1;
                    String method = mappedStatement.getId();
                    String[] str = method.split("\\.");
                    if (str.length >= 2) {
                        return str[str.length - 2] + "." + str[str.length - 1];
                    }
                }
            }
        } catch (Exception e) {

        }
        return EMPTY;
    }

    private static Object getFiledValue(Object ob, String name) {
        try {
            if (ob != null) {
                Field field = ob.getClass().getDeclaredField(name);
                if (field != null) {
                    field.setAccessible(true);
                    return field.get(ob);
                }
            }
        } catch (Exception e) {

        }
        return null;
    }

    public static String getSql(RoutingStatementHandler statementHandler) {
        try {
            BoundSql boundSql = statementHandler.getBoundSql();
            String sql = boundSql.getSql();
            // 格式化Sql语句，去除换行符，替换参数
            sql = formatSql(sql, boundSql.getParameterObject(), boundSql.getParameterMappings());
            return sql;
        } catch (Exception e) {

        }
        return EMPTY;
    }

    private static String beautifySql(String sql) {
        sql = sql.replaceAll("[\\s\n ]+", " ");
        return sql;
    }

    private static String formatSql(String sql, Object parameterObject, List<ParameterMapping> parameterMappingList) {
        // 输入判断是否为空
        if (StringUtils.isEmpty(sql)) {
            return "";
        }
        // 美化sql
        sql = beautifySql(sql);

        // 不传参数的场景，直接把Sql美化一下返回出去
        if (parameterObject == null || CollectionUtils.isEmpty(parameterMappingList)) {
            return sql;
        }

        // 定义一个没有替换过占位符的sql，用于出异常时返回
        String sqlWithoutReplacePlaceholder = sql;
        try {
            Class<?> parameterObjectClass = parameterObject.getClass();

            // 如果参数是StrictMap且Value类型为Collection，获取key="list"的属性，这里主要是为了处理<foreach>循环时传入List这种参数的占位符替换
            // 例如select * from xxx where id in <foreach
            // collection="list">...</foreach>
            if (isStrictMap(parameterObjectClass)) {
                StrictMap<Collection<?>> strictMap = (StrictMap<Collection<?>>) parameterObject;

                if (isList(strictMap.get("list").getClass())) {
                    sql = handleListParameter(sql, strictMap.get("list"));
                }
            } else if (isMap(parameterObjectClass)) {
                // 如果参数是Map则直接强转，通过map.get(key)方法获取真正的属性值
                // 这里主要是为了处理<insert>、<delete>、<update>、<select>时传入parameterType为map的场景
                Map<?, ?> paramMap = (Map<?, ?>) parameterObject;
                sql = handleMapParameter(sql, paramMap, parameterMappingList);
            } else {
                // 通用场景，比如传的是一个自定义的对象或者八种基本数据类型之一或者String
                sql = handleCommonParameter(sql, parameterMappingList, parameterObjectClass, parameterObject);
            }
        } catch (Exception e) {
            // 占位符替换过程中出现异常，则返回没有替换过占位符但是格式美化过的sql，这样至少保证sql语句比BoundSql中的sql更好看
            return sqlWithoutReplacePlaceholder;
        }

        return sql;
    }

    /**
     * 处理通用场景
     *
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private static String handleCommonParameter(String sql, List<ParameterMapping> parameterMappingList,
                                                Class<?> parameterObjectClass, Object parameterObject) throws Exception {
        for (ParameterMapping parameterMapping : parameterMappingList) {
            String propertyValue = null;
            // 基本数据类型或者基本数据类型的包装类，直接toString即可获取其真正的参数值，其余直接取paramterMapping中的property属性即可
            if (isPrimitiveOrPrimitiveWrapper(parameterObjectClass)) {
                propertyValue = parameterObject.toString();
            } else {
                String propertyName = parameterMapping.getProperty();
                if (parameterObjectClass.isAssignableFrom(MapperMethod.ParamMap.class)) {
                    propertyValue = String.valueOf(parameterObjectClass.getDeclaredMethod("get", Object.class).invoke(parameterObject, propertyName));
                } else {
                    Field field = parameterObjectClass.getDeclaredField(propertyName);
                    // 要获取Field中的属性值，这里必须将私有属性的accessible设置为true
                    field.setAccessible(true);
                    propertyValue = String.valueOf(field.get(parameterObject));
                }

                if (parameterMapping.getJavaType().isAssignableFrom(String.class)) {
                    propertyValue = "\"" + propertyValue + "\"";
                }
            }

            sql = sql.replaceFirst("\\?", propertyValue);
        }

        return sql;
    }

    /**
     * 处理Map场景
     */
    private static String handleMapParameter(String sql, Map<?, ?> paramMap, List<ParameterMapping> parameterMappingList) {
        for (ParameterMapping parameterMapping : parameterMappingList) {
            Object propertyName = parameterMapping.getProperty();
            Object propertyValue = paramMap.get(propertyName);
            if (propertyValue != null) {
                if (propertyValue.getClass().isAssignableFrom(String.class)) {
                    propertyValue = "\"" + propertyValue + "\"";
                }

                sql = sql.replaceFirst("\\?", propertyValue.toString());
            }
        }
        return sql;
    }

    /**
     * @param sql
     * @param col
     * @Description: 处理List场景
     */
    private static String handleListParameter(String sql, Collection<?> col) {
        if (col != null && col.size() != 0) {
            for (Object obj : col) {
                String value = null;
                Class<?> objClass = obj.getClass();

                // 只处理基本数据类型、基本数据类型的包装类、String这三种
                // 如果是复合类型也是可以的，不过复杂点且这种场景较少，写代码的时候要判断一下要拿到的是复合类型中的哪个属性
                if (isPrimitiveOrPrimitiveWrapper(objClass)) {
                    value = obj.toString();
                } else if (objClass.isAssignableFrom(String.class)) {
                    value = "\"" + obj.toString() + "\"";
                }

                sql = sql.replaceFirst("\\?", value);
            }
        }

        return sql;
    }

    /**
     * 是否基本数据类型或者基本数据类型的包装类
     */
    private static boolean isPrimitiveOrPrimitiveWrapper(Class<?> parameterObjectClass) {
        return parameterObjectClass.isPrimitive() || (parameterObjectClass.isAssignableFrom(Byte.class)
                || parameterObjectClass.isAssignableFrom(Short.class)
                || parameterObjectClass.isAssignableFrom(Integer.class)
                || parameterObjectClass.isAssignableFrom(Long.class)
                || parameterObjectClass.isAssignableFrom(Double.class)
                || parameterObjectClass.isAssignableFrom(Float.class)
                || parameterObjectClass.isAssignableFrom(Character.class)
                || parameterObjectClass.isAssignableFrom(Boolean.class));
    }

    /**
     * 是否DefaultSqlSession的内部类StrictMap
     */
    private static boolean isStrictMap(Class<?> parameterObjectClass) {
        return StrictMap.class.isAssignableFrom(parameterObjectClass);
    }

    /**
     * 是否List的实现类
     */
    private static boolean isList(Class<?> clazz) {
        Class<?>[] interfaceClasses = clazz.getInterfaces();
        for (Class<?> interfaceClass : interfaceClasses) {
            if (interfaceClass.isAssignableFrom(List.class)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 是否Map的实现类
     */
    private static boolean isMap(Class<?> parameterObjectClass) {
        Class<?>[] interfaceClasses = parameterObjectClass.getInterfaces();
        for (Class<?> interfaceClass : interfaceClasses) {
            if (interfaceClass.isAssignableFrom(Map.class)) {
                return true;
            }
        }

        return false;
    }

    // ********************通用*****************
    public static String getInstant(Connection connection) {
        if (null == connection) {
            return EMPTY;
        }
        try {
            return connection.getMetaData().getURL();
        } catch (Exception e) {

        }
        return EMPTY;
    }

    public static String databaseType(Connection connection) {
        if (null == connection) {
            return EMPTY;
        }
        try {
            return connection.getMetaData().getDatabaseProductName();
        } catch (Exception e) {

        }
        return EMPTY;
    }

    public static String user(Connection connection) {
        if (null == connection) {
            return EMPTY;
        }
        try {
            return connection.getMetaData().getUserName();
        } catch (Exception e) {

        }
        return EMPTY;
    }

    public static String getDbIp(String instance) {
        if (null == instance || "".equals(instance)) {
            return EMPTY;
        }
        try {
            int start = instance.indexOf("//") + 2;
            String startStr = instance.substring(start);
            int ipEnd = startStr.indexOf("/");
            String ipStr = startStr.substring(0, ipEnd);
            return ipStr;
        } catch (Exception e) {

        }
        return EMPTY;
    }

    public static String getDbName(String instance) {
        if (null == instance || "".equals(instance)) {
            return EMPTY;
        }
        try {
            int start = instance.indexOf("//") + 2;
            String startStr = instance.substring(start);
            int ipEnd = startStr.indexOf("/");
            int nameEnd = startStr.indexOf("?");
            String nameStr = startStr.substring(ipEnd + 1, nameEnd);
            return nameStr;
        } catch (Exception e) {

        }
        return EMPTY;
    }

}
