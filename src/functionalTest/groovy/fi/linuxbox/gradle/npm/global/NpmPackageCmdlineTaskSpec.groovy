package fi.linuxbox.gradle.npm.global

import fi.linuxbox.gradle.GradleSpecification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class NpmPackageCmdlineTaskSpec extends GradleSpecification {

    void 'it should work in a single project build'() {
        given:
        buildScript << """
            // tag::single-project-example[]
            plugins {
                id 'fi.linuxbox.npm-global'
            }

            npmGlobal {
                install 'cdk', version: '2.41.0'
            }

            import fi.linuxbox.gradle.npm.global.NpmPackage
            import fi.linuxbox.gradle.npm.global.NpmPackageCmdlineTask
            import java.nio.file.Path

            abstract class CdkCmdlineTask extends NpmPackageCmdlineTask {
                CdkCmdlineTask() {
                    super()
                    group = 'CDK tasks'
                    description = 'Runs cdk with --args'
                }

                @Override
                protected boolean providesEntrypoint(NpmPackage pkg) {
                    pkg.pkg.get() == 'cdk'
                }

                @Override
                protected Path resolveEntrypoint(Path nodeModuleDirectory) {
                    nodeModuleDirectory.resolve('bin').resolve('cdk')
                }
            }

            tasks.register('cdkVersion', CdkCmdlineTask) {
                args = ['--version']
            }
            // end::single-project-example[]
        """

        when:
        final build = gradle(latestGradleVersion, 'cdkVersion').build()

        then:
        build.task(':installCdk').outcome == SUCCESS
        build.task(':cdkVersion').outcome == SUCCESS
        build.output.contains '2.41.0'
    }

    void 'it should work in a subproject'() {
        given:
        settingsScript << """
            include 'subproject'
        """

        and:
        buildScript << """
            plugins {
                id 'fi.linuxbox.npm-global'
            }

            nodejs {
                // Installation of Yarn requires sh
                useSystemPath()
            }

            npmGlobal {
                install 'yarn', version: '1.22.19'
            }
        """

        and:
        projectFile('subproject/build.gradle') << """
            plugins {
                id 'fi.linuxbox.npm-global'
            }

            import fi.linuxbox.gradle.npm.global.NpmPackage
            import fi.linuxbox.gradle.npm.global.NpmPackageCmdlineTask
            import java.nio.file.Path
            
            abstract class YarnCmdlineTask extends NpmPackageCmdlineTask {
                YarnCmdlineTask() {
                    super()
                    group = 'Yarn tasks'
                    description = 'Runs yarn with --args'
                }

                @Override
                protected boolean providesEntrypoint(NpmPackage pkg) {
                    pkg.pkg.get() == 'yarn'
                }

                @Override
                protected Path resolveEntrypoint(Path nodeModuleDirectory) {
                    nodeModuleDirectory.resolve('bin').resolve('yarn.js')
                }
            }

            tasks.register('yarnVersion', YarnCmdlineTask) {
                args = ['--version']
            }
        """

        when:
        final build = gradle(latestGradleVersion, ':subproject:yarnVersion').build()

        then:
        build.task(':subproject:yarnVersion').outcome == SUCCESS
        build.output.contains '1.22.19'
    }
}
