package com.demoo.plugin.utils.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 整合SpanManager和SkipManager
 *
 * @author zhxy
 * @Date 2021/6/30 1:49 下午
 */
public abstract class AbstractSpanWithSkipManager<T> extends AbstractSpanManager<T> implements TraceSkipManager{


    //---------------------------------------------------span---------------------------------------------------

    public AbstractSpanWithSkipManager(boolean useDefaultDecorator) {
        super(useDefaultDecorator);
    }


    //--------------------------------------------------skip----------------------------------------------------

    public static final Pattern DEFAULT_SKIP_PATTERN = Pattern.compile("/api-docs.*|/autoconfig|/configprops|"
            + "/dump|/health|/info|/metrics.*|/mappings|/trace|"
            + "/swagger.*|.*\\.png|.*\\.css|.*\\.js|.*\\.html|.*\\.gif|.*\\.jpg|/favicon.ico|/hystrix.stream");

    protected List<Pattern> skipPatterns;

    public Pattern getDefaultSkipPattern() {
        return DEFAULT_SKIP_PATTERN;
    }

    @Override
    public void addSkipPattern(Pattern pattern) {
        if (pattern == null) {
            return;
        }
        List<Pattern> patterns = new ArrayList<Pattern>(skipPatterns);
        patterns.add(pattern);
        skipPatterns = Collections.unmodifiableList(patterns);
    }

    @Override
    public List<Pattern> getSkipPatterns() {
        return skipPatterns;
    }

    @Override
    public void addSkipPattern(int index, Pattern pattern) {
        if (pattern == null) {
            return;
        }
        List<Pattern> patterns = new ArrayList<Pattern>(skipPatterns);
        patterns.add(index, pattern);
        skipPatterns = Collections.unmodifiableList(patterns);
    }

    @Override
    public void addSkipPatterns(List<Pattern> patterns) {
        if (patterns == null || patterns.size() == 0) {
            return;
        }
        List<Pattern> newSkipPatterns = new ArrayList<Pattern>(skipPatterns);
        newSkipPatterns.addAll(patterns);
        skipPatterns = Collections.unmodifiableList(newSkipPatterns);
    }

    @Override
    public void removeSkipPattern(Pattern pattern) {
        List<Pattern> newSkipPatterns = new ArrayList<Pattern>(skipPatterns);
        newSkipPatterns.remove(pattern);
        skipPatterns = Collections.unmodifiableList(newSkipPatterns);
    }

    @Override
    public void clearSkipPatterns() {
        skipPatterns = Collections.emptyList();
    }

}
