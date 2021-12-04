package fi.linuxbox.gradle.npm.global

import groovy.transform.AutoFinal
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.ysb33r.gradle.nodejs.plugins.CommandLinePlugin

/**
 * A new instance of this class is created for each project this plugin is
 * applied to.
 */
@AutoFinal
@CompileStatic
class NpmGlobalPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.apply plugin: CommandLinePlugin

        // Following actually instantiates the extension class
        project.extensions.create(NpmGlobalExtension.NAME, NpmGlobalExtension)
    }

}
