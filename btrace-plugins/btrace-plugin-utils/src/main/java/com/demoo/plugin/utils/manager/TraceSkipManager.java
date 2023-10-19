package com.demoo.plugin.utils.manager;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 管理匹配规则
 */
public interface TraceSkipManager {

    List<Pattern> getSkipPatterns();

    void addSkipPattern(Pattern pattern);

    void addSkipPattern(int index, Pattern pattern);

    void addSkipPatterns(List<Pattern> patterns);

    void removeSkipPattern(Pattern pattern);

    void clearSkipPatterns();

}
