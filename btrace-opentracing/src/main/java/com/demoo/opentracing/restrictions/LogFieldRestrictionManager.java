package com.demoo.opentracing.restrictions;

/**
 * 日志字段约束管理者
 *
 */
public interface LogFieldRestrictionManager {

    LogFieldRestrictionManager DEFAULT = new DefaultLogFieldRestrictionManager();
    LogFieldRestrictionManager NO_LIMIT = new NoLimitLogFieldRestrictionManager();

    Restriction getRestriction(String key);

    class DefaultLogFieldRestrictionManager implements LogFieldRestrictionManager {

        private static final int DEFAULT_MAX_VALUE_LENGTH = 1024;

        private final Restriction restriction;

        DefaultLogFieldRestrictionManager() {
            this(DEFAULT_MAX_VALUE_LENGTH);
        }

        DefaultLogFieldRestrictionManager(int maxValueLength) {
            this.restriction = Restriction.of(true, maxValueLength);
        }

        @Override
        public Restriction getRestriction(String key) {
            return restriction;
        }
    }

    class NoLimitLogFieldRestrictionManager implements LogFieldRestrictionManager {

        private static final int DEFAULT_MAX_VALUE_LENGTH = Integer.MAX_VALUE;

        private final Restriction restriction;

        NoLimitLogFieldRestrictionManager() {
            this(DEFAULT_MAX_VALUE_LENGTH);
        }

        NoLimitLogFieldRestrictionManager(int maxValueLength) {
            this.restriction = Restriction.of(true, maxValueLength);
        }

        @Override
        public Restriction getRestriction(String key) {
            return restriction;
        }
    }
}
