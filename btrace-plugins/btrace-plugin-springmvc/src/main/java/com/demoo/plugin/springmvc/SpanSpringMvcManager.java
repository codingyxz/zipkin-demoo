package com.demoo.plugin.springmvc;

import com.demoo.plugin.utils.manager.AbstractSpanManager;
import com.demoo.plugin.utils.manager.TraceSkipManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author zhxy
 * @Date 2021/6/29 8:47 下午
 */
public class SpanSpringMvcManager extends AbstractSpanManager<SpringMvcSpanDecorator> implements TraceSkipManager {

    public static final Pattern DEFAULT_SKIP_PATTERN = Pattern.compile("/api-docs.*|/autoconfig|/configprops|"
            + "/dump|/health|/info|/metrics.*|/mappings|/trace|"
            + "/swagger.*|.*\\.png|.*\\.css|.*\\.js|.*\\.html|.*\\.gif|.*\\.jpg|/favicon.ico|/hystrix.stream");

    private List<Pattern> skipPatterns;

    public SpanSpringMvcManager() {
        this(true,true);
    }

    public SpanSpringMvcManager(boolean useDefaultDecorator, boolean useDefaultSkipPattern) {
        super(useDefaultDecorator);
        if (useDefaultSkipPattern) {
            this.skipPatterns = Collections.singletonList(DEFAULT_SKIP_PATTERN);
        } else {
            this.skipPatterns = Collections.emptyList();
        }
    }

    @Override
    public SpringMvcSpanDecorator getDefaultDecorator() {
        return SpringMvcSpanDecorator.DEFAULT_DECORATOR;
    }

    @Override
    public void addSkipPattern(Pattern pattern) {
        if (pattern == null) {
            return;
        }
        List<Pattern> patterns = new ArrayList<>(skipPatterns);
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
        List<Pattern> patterns = new ArrayList<>(skipPatterns);
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
        if (pattern == null) {
            return;
        }
        List<Pattern> newSkipPatterns = new ArrayList<Pattern>(skipPatterns);
        newSkipPatterns.remove(pattern);
        skipPatterns = Collections.unmodifiableList(newSkipPatterns);
    }

    @Override
    public void clearSkipPatterns() {
        skipPatterns = Collections.emptyList();
    }

}
