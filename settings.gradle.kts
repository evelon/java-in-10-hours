plugins {
    // JDK 21이 없으면 toolchain을 자동으로 내려받는다
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "ledger"
