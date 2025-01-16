import org.gradle.api.tasks.PathSensitivity.NONE
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.Companion.kotlinNodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn

plugins {
    kotlin("multiplatform") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    kotlin("plugin.js-plain-objects") version "2.1.0"
    id("io.github.turansky.seskar") version "3.81.0"
    id("io.kotest.multiplatform") version "5.9.1"
    distribution
}

val collectGoodTypings by tasks.registering(Sync::class) {
    from("../github-actions-typing-catalog/typings")
    include("**/action-types.yml")
    into(layout.buildDirectory.dir("good-typings"))
    eachFile {
        relativePath = RelativePath(
            relativePath.isFile,
            relativePath
                .segments
                .joinToString("_")
                .replace("_action-types", "")
        )
    }
    includeEmptyDirs = false
}

val schemaFile = file("src/main/schema/github-actions-typing.schema.json")
val badDir = file("src/test/resources/bad-typings")

kotlin {
    jvmToolchain(21)
    jvm {
        mainRun {
            mainClass = "it.krzeminski.githubactionstyping.MainKt"
        }

        val test by testRuns.existing {
            executionTask {
                useJUnitPlatform()
                configureInputsForSchemaTest()
                systemProperty("schemaFile", schemaFile.absolutePath)
                systemProperty("goodDir", collectGoodTypings.get().destinationDir.absolutePath)
                systemProperty("badDir", badDir.absolutePath)
            }
        }
    }

    js {
        useEsModules()
        nodejs {
            testTask {
                configureInputsForSchemaTest()
                environment("schemaFile", schemaFile.absolutePath)
                environment("goodDir", collectGoodTypings.get().destinationDir.absolutePath)
                environment("badDir", badDir.absolutePath)
            }
        }
    }

    sourceSets {
        jvmMain {
            dependencies {
                implementation("com.charleskorn.kaml:kaml:0.63.0")
            }
        }

        jvmTest {
            dependencies {
                runtimeOnly("org.junit.platform:junit-platform-launcher")
                implementation(dependencies.platform("io.kotest:kotest-bom:5.9.1"))
                runtimeOnly("io.kotest:kotest-runner-junit5")
                implementation("io.kotest:kotest-framework-api")
                implementation("io.kotest:kotest-framework-datatest")
                implementation("io.kotest:kotest-assertions-core")

                implementation("it.krzeminski:snakeyaml-engine-kmp:3.1.0")
                implementation("io.github.optimumcode:json-schema-validator:0.3.1")
            }
        }

        jsTest {
            dependencies {
                implementation(dependencies.platform("io.kotest:kotest-bom:5.9.1"))
                implementation("io.kotest:kotest-framework-engine")
                implementation("io.kotest:kotest-framework-api")
                implementation("io.kotest:kotest-framework-datatest")
                implementation("io.kotest:kotest-assertions-core")

                implementation(kotlinWrappers.js)
                implementation(kotlinWrappers.node)
                implementation(npm("ajv", "8.17.1"))
                implementation(npm("yaml", "2.7.0"))
                implementation(npm("@prantlf/jsonlint", "16.0.0"))
            }
        }
    }
}

kotlinNodeJsRootExtension.version = "20.18.1"
kotlinNodeJsRootExtension.downloadBaseUrl = null
yarn.downloadBaseUrl = null

distributions {
    main {
        contents {
            into("lib") {
                val jvmJar by tasks.existing
                from(jvmJar)
                val jvmRuntimeClasspath by configurations
                from(jvmRuntimeClasspath)
            }
        }
    }
}

fun AbstractTestTask.configureInputsForSchemaTest() {
    inputs
        .file(schemaFile)
        .withPropertyName("schemaFile")
        .normalizeLineEndings()
        .withPathSensitivity(NONE)

    inputs
        .files(collectGoodTypings)
        .withPropertyName("goodDir")
        .normalizeLineEndings()
        .withPathSensitivity(NONE)

    inputs
        .dir(badDir)
        .withPropertyName("badDir")
        .normalizeLineEndings()
        .withPathSensitivity(NONE)
}
