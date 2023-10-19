package com.demoo.opentracing.propagation;

public final class PrefixedKeys {
  public String prefixedKey(String key, String prefix) {
    return prefix + key;
  }

  public String unPrefixedKey(String key, String prefix) {
    return key.substring(prefix.length());
  }
}
