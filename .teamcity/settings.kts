import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dotnetPublish
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dotnetRestore
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2020.2"

project {

    buildType(Build)
}

object Build : BuildType({
    name = "Build"

    artifactRules = "+:./build => ci-cd-artifact.zip"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        dotnetRestore {
            name = "restoring dependencies"
            projects = "CICDDemo/CICDDemo.sln"
            param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.CommandLineTools.DEFAULT%")
        }
        dotnetPublish {
            name = "Publish"
            projects = "CICDDemo/CICDDemo.csproj"
            outputDir = "./build"
            param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.CommandLineTools.DEFAULT%")
        }
        step {
            name = "Push to Octopus"
            type = "octopus.push.package"
            param("octopus_space_name", "Default")
            param("octopus_host", "http://18.205.176.81:8080")
            param("octopus_packagepaths", "+:./build => %repository%.%build.number%.zip")
            param("octopus_publishartifacts", "true")
            param("octopus_forcepush", "false")
            param("secure:octopus_apikey", "credentialsJSON:4b9d6efe-8fff-4d80-b4f6-5b2f5699ae9b")
        }
    }

    triggers {
        vcs {
        }
    }
})
