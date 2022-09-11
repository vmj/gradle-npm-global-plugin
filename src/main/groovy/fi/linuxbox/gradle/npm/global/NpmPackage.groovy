package fi.linuxbox.gradle.npm.global

import org.gradle.api.Named
import org.gradle.api.provider.Property

/**
 * Domain object in {@code npmGlobal.packages}.
 * <p>
 *     If using the convenience {@link NpmGlobalExtension#install install}
 *     function, the name of the object is derived from the first argument
 *     (the {@link #getPkg NPM package name}).  It is derived by removing
 *     any spaces and punctuation and capitalizing the medial words.
 *     For example, {@code lower-camel-case} becomes {@code lowerCamelCase}.
 * </p>
 * <p>
 *     If using the {@code packages} block, the name is used as given.
 *     The user will likely specify the {@code pkg} property in this case.
 * </p>
 * <p>
 *     The name is used as part of the install task name.
 * </p>
 *
 */
interface NpmPackage extends Named {

    /**
     * The verbatim location from where to install the NPM package.
     * <p>
     *     Anything that {@code npm install -g} accepts is accepted here.
     * </p>
     * <p>
     *     If this is specified, then {@link #getAlias() alias},
     *     {@link #getScope() scope}, {@link #getPkg() package}, and
     *     {@link #getVersion() version} are not used in determining the source
     *     location of the installation.
     * </p>
     */
    Property<String> getFrom()

    /**
     * Local alias for the installed package.
     * <p>
     *     This is either the optional named argument of the
     *     {@link NpmGlobalExtension#install install} function,
     *     or the {@code alias} property from the {@code packages} block.
     * </p>
     * <p>
     *     If this is not specified, users should fall back to using the
     *     {@link #getPkg()} package name} property as the local installation
     *     name.
     * </p>
     */
    Property<String> getAlias()

    /**
     * Namespace of the NPM package.
     * <p>
     *     Do not add the leading {@code @} or the trailing {@code /}.
     *     Those separators are added automatically.
     * </p>
     * <p>
     *     This is either the optional named argument of the
     *     {@link NpmGlobalExtension#install install} function,
     *     or the {@code scope} property from the {@code packages} block.
     * </p>
     */
    Property<String> getScope()

    /**
     * The NPM package name.
     * <p>
     *     This is either the first argument of the
     *     {@link NpmGlobalExtension#install install} function,
     *     or the {@code pkg} property from the {@code packages} block.
     * </p>
     * <p>
     *     If the {@code pkg} property in the {@code packages} block is not
     *     specified, users should fall back to using the
     *     {@link #getName()} name} property.
     * </p>
     */
    Property<String> getPkg()

    /**
     * Version of the NPM package.
     * <p>
     *     Do not add the leading {@code @}.
     *     That separator is added automatically.
     * </p>
     * <p>
     *     This is either the optional named argument of the
     *     {@link NpmGlobalExtension#install install} function,
     *     or the {@code version} property from the {@code packages} block.
     * </p>
     */
    Property<String> getVersion()

}
