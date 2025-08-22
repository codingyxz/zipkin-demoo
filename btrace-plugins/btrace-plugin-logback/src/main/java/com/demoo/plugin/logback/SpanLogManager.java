package com.demoo.plugin.logback;

import com.demoo.plugin.utils.manager.AbstractSpanManager;
import com.demoo.plugin.utils.manager.TraceSkipManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Description log span manager管理器
 * @Date 2025-06-27
 * @Created by Yolo
 */
public class SpanLogManager extends AbstractSpanManager<LogFilterSpanDecorator> implements TraceSkipManager {


    public static final Pattern DEFAULT_SKIP_PATTERN = Pattern
            .compile("/api-docs.*|/autoconfig|/configprops|" + "/dump|/health|/info|/metrics.*|/mappings|/trace|"
                    + "/swagger.*|.*\\.png|.*\\.css|.*\\.js|.*\\.html|.*\\.gif|.*\\.jpg|/favicon.ico|/hystrix.stream");


    private List<Pattern> skipPatterns;

    public Pattern getDefaultSkipPattern() {
        return DEFAULT_SKIP_PATTERN;
    }

    public SpanLogManager() {
        this(true, true);
    }

    public SpanLogManager(boolean isUseDefaultDecorator, boolean isUseDefaultSkipPatterns) {
        super(isUseDefaultDecorator);
        if (isUseDefaultSkipPatterns) {
            this.skipPatterns = Collections.singletonList(DEFAULT_SKIP_PATTERN);
        } else {
            this.skipPatterns = Collections.emptyList();
        }
    }

    @Override
    public LogFilterSpanDecorator getDefaultDecorator() {
        return LogFilterSpanDecorator.DEFAULT_DECORATOR;
    }

    @Override
    public List<Pattern> getSkipPatterns() {
        return skipPatterns;
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
