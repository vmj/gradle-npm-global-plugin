package fi.linuxbox.gradle.npm.global;

import org.gradle.api.Named;
import org.gradle.api.provider.Property;

/**
 * Domain object in {@code npmGlobal.packages}.
 */
public interface NpmPackage extends Named {
    Property<String> getFrom();
    Property<String> getScope();
    Property<String> getPkg();
    Property<String> getVersion();
    Property<String> getAlias();

    Property<Boolean> getForce();
}
