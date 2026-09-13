import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.openapiGenerator)
}

val generatedBuildConfigDir = layout.buildDirectory.dir("generated/buildconfig")
val generatedOpenApiDir = layout.buildDirectory.dir("generated/openapi")

abstract class GenerateBuildConfigTask : DefaultTask() {
    @get:Input
    abstract val flavor: Property<String>

    @get:Input
    abstract val prodBaseUrl: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val flavorValue = flavor.get()
        val prodUrl = prodBaseUrl.get()
        val file = outputDirectory.get().asFile.resolve(
            "de/poljansek/budgetbook/core/config/BuildConfig.kt"
        )
        file.parentFile.mkdirs()
        file.writeText(
            """
            package de.poljansek.budgetbook.core.config

            object BuildConfig {
                const val FLAVOR: String = "$flavorValue"
                const val PROD_BASE_URL: String = "$prodUrl"
            }

            """.trimIndent()
        )
    }
}

val generateBuildConfig = tasks.register<GenerateBuildConfigTask>("generateBuildConfig") {
    flavor.set(providers.gradleProperty("budgetbook.flavor").orElse("dev"))
    prodBaseUrl.set(
        providers.gradleProperty("budgetbook.prodBaseUrl").orElse("https://budgetbook.example.com")
    )
    outputDirectory.set(generatedBuildConfigDir)
}

val copyOpenApiSpec = tasks.register<Copy>("copyOpenApiSpec") {
    from(rootProject.layout.projectDirectory.file("openapi/openapi.yaml"))
    into(layout.buildDirectory.dir("openapi"))
}

tasks.named<GenerateTask>("openApiGenerate") {
    dependsOn(copyOpenApiSpec)
    generatorName.set("kotlin")
    library.set("multiplatform")
    inputSpec.set(
        layout.buildDirectory.file("openapi/openapi.yaml").map { it.asFile.absolutePath }
    )
    outputDir.set(generatedOpenApiDir.map { it.asFile.absolutePath })
    packageName.set("de.poljansek.budgetbook.generated")
    apiPackage.set("de.poljansek.budgetbook.generated.api")
    modelPackage.set("de.poljansek.budgetbook.generated.model")
    configOptions.set(
        mapOf(
            "serializationLibrary" to "kotlinx_serialization",
            "dateLibrary" to "kotlinx-datetime",
            "useCoroutines" to "true",
            "enumPropertyNaming" to "original",
        )
    )
    skipValidateSpec.set(true)
    generateApiTests.set(false)
    generateModelTests.set(false)
    generateApiDocumentation.set(false)
    generateModelDocumentation.set(false)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    jvm()

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    android {
        namespace = "de.poljansek.budgetbook.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generatedBuildConfigDir)
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.compose.material.icons.core)

                implementation(libs.navigation.compose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.viewmodel.compose)

                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)

                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)

                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.no.arg)

                implementation(libs.filekit.core)
                implementation(libs.filekit.dialogs)
                implementation(libs.filekit.dialogs.compose)
            }
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.ktor.client.mock)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        webMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "de.poljansek.budgetbook.resources"
}

tasks.matching {
    it.name.contains("compile", ignoreCase = true) && it.name.contains("Kotlin")
}.configureEach {
    dependsOn(generateBuildConfig)
}

tasks.matching { it.name.startsWith("generateResourceAccessors") }.configureEach {
    dependsOn(generateBuildConfig)
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
