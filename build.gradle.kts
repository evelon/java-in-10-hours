plugins {
    java
    application
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // Gradle 9부터는 launcher를 직접 선언해야 한다
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.27.7")
}

application {
    mainClass = "ledger.Main"
}

tasks.test {
    useJUnitPlatform()
}

// run은 기본으로 stdin을 넘기지 않는다. REPL에 키보드 입력을 연결한다
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
