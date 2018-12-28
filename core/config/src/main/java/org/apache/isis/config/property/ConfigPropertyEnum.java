package org.apache.isis.config.property;

import java.util.AbstractMap;
import java.util.Map;

import org.apache.isis.config.IsisConfiguration;

public class ConfigPropertyEnum<E extends Enum<E>> extends
        ConfigPropertyAbstract<E> {
    public ConfigPropertyEnum(final String key, final E defaultValue) {
        super(key, defaultValue);
    }
    public E from(final IsisConfiguration configuration) {
        return Enum.valueOf(defaultValue.getDeclaringClass(), configuration.getString(key, defaultValue.name()).toUpperCase());
    }

    @Override
    public Map.Entry<String, String> of(final E value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value.name().toUpperCase());
    }

}
