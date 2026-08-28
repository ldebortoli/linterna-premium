import java.util.Properties
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    jacoco
}

val versionProperties = Properties().apply {
    rootProject.file("../../../version.properties").inputStream().use(::load)
}
val canonicalVersionName = versionProperties.getProperty("versionName")
val canonicalVersionCode = versionProperties.getProperty("versionCode").toInt()
val playAdMobAppId = providers.gradleProperty("LINTERNAPREMIUM_ADMOB_APP_ID").orNull.orEmpty()
val playBannerId = providers.gradleProperty("LINTERNAPREMIUM_ADMOB_BANNER_ID").orNull.orEmpty()

android {
    namespace = "com.linternapremium.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.linternapremium.app"
        minSdk = 24
        targetSdk = 36
        versionCode = canonicalVersionCode
        versionName = canonicalVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        buildConfigField("String", "PREMIUM_PRODUCT_ID", "\"premium_blackout_pack\"")
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("demo") {
            dimension = "distribution"
            applicationIdSuffix = ".demo"
            versionNameSuffix = "-demo"
            buildConfigField("boolean", "DEMO_BILLING", "true")
            buildConfigField(
                "String",
                "ADMOB_BANNER_ID",
                "\"ca-app-pub-3940256099942544/6300978111\"",
            )
        }
        create("play") {
            dimension = "distribution"
            buildConfigField("boolean", "DEMO_BILLING", "false")
            buildConfigField("String", "ADMOB_BANNER_ID", "\"$playBannerId\"")
            manifestPlaceholders["ADMOB_APP_ID"] = playAdMobAppId
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
        warningsAsErrors = true
        disable += setOf("GradleDependency", "AndroidGradlePluginVersion")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

jacoco {
    toolVersion = "0.8.13"
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.06.01")

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("com.android.billingclient:billing-ktx:9.1.0")
    implementation("com.google.android.gms:play-services-ads:25.4.0")
    implementation("com.google.android.ump:user-messaging-platform:4.0.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
}

val coverageClasses = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/demoDebug")) {
    include("**/domain/**")
}
val coverageSources = files("src/main/java/com/linternapremium/app/domain")
val coverageData = fileTree(layout.buildDirectory) {
    include("jacoco/testDemoDebugUnitTest.exec")
    include("outputs/unit_test_code_coverage/demoDebugUnitTest/testDemoDebugUnitTest.exec")
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDemoDebugUnitTest")
    classDirectories.setFrom(coverageClasses)
    sourceDirectories.setFrom(coverageSources)
    executionData.setFrom(coverageData)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn("testDemoDebugUnitTest")
    classDirectories.setFrom(coverageClasses)
    sourceDirectories.setFrom(coverageSources)
    executionData.setFrom(coverageData)
    violationRules {
        listOf("INSTRUCTION", "BRANCH", "LINE", "METHOD").forEach { metric ->
            rule {
                limit {
                    counter = metric
                    value = "COVEREDRATIO"
                    minimum = "1.00".toBigDecimal()
                }
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    finalizedBy("jacocoTestReport")
}

val playTaskRequested = gradle.startParameter.taskNames.any { taskName ->
    taskName.contains("play", ignoreCase = true)
}
if (playTaskRequested && (playAdMobAppId.isBlank() || playBannerId.isBlank())) {
    throw GradleException(
        "La variante Play exige LINTERNAPREMIUM_ADMOB_APP_ID y LINTERNAPREMIUM_ADMOB_BANNER_ID en ~/.gradle/gradle.properties.",
    )
}
