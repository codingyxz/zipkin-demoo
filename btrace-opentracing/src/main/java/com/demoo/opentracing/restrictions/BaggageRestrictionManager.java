
package com.demoo.opentracing.restrictions;

/**
 * 用于对baggage是否可访问或长度做限定
 *
 */
public interface BaggageRestrictionManager {

    BaggageRestrictionManager DEFAULT = new DefaultBaggageRestrictionManager();
    BaggageRestrictionManager NO_LIMIT = new NoLimitBaggageRestrictionManager();

    Restriction getRestriction(String key);

    class DefaultBaggageRestrictionManager implements BaggageRestrictionManager {

        private static final int DEFAULT_MAX_VALUE_LENGTH = 2048;

        private final Restriction restriction;

        DefaultBaggageRestrictionManager() {
            this(DEFAULT_MAX_VALUE_LENGTH);
        }

        DefaultBaggageRestrictionManager(int maxValueLength) {
            this.restriction = Restriction.of(true, maxValueLength);
        }

        @Override
        public Restriction getRestriction(String key) {
            return restriction;
        }
    }


    class NoLimitBaggageRestrictionManager implements BaggageRestrictionManager {

        private static final int DEFAULT_MAX_VALUE_LENGTH = Integer.MAX_VALUE;

        private final Restriction restriction;

        NoLimitBaggageRestrictionManager() {
            this(DEFAULT_MAX_VALUE_LENGTH);
        }

        NoLimitBaggageRestrictionManager(int maxValueLength) {
            this.restriction = Restriction.of(true, maxValueLength);
        }

        @Override
        public Restriction getRestriction(String key) {
            return restriction;
        }
    }
}
