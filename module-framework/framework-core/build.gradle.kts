plugins {
    `java-library`
}
val javafxVersion = libs.versions.javafx.get()
val platform: String = run {
    val osName = System.getProperty("os.name").lowercase()
    when {
        osName.contains("win") -> "win"
        osName.contains("mac") -> "mac"
        osName.contains("linux") -> "linux"
        osName.contains("nix") -> "linux"
        else -> throw GradleException("Unsupported OS: $osName")
    }
}
dependencies {
    api(libs.bundles.spring)

    api(libs.hikaricp)
    api(libs.bundles.mybatis.plus)
    api(libs.mybatis.spring)
    api(libs.log4j.core)
    api(libs.slf4j.api)
    api(libs.logback.classic)

    api(libs.bundles.jackson)
    api(libs.guava)
    api(libs.commons.beanutils){ exclude(group = "commons-logging", module = "commons-logging") }
    api(libs.commons.lang3)
    api(libs.bundles.okhttp)
    api(libs.cron.utils)
    api(libs.bundles.twelvemonkeys)
    api(libs.bundles.google.zxing)
    api(libs.jnativehook)
    api(libs.fastexcel)

    api(libs.atlantafx.base)
    api(libs.controlsfx)
    api(libs.bundles.ikonli)
    api(libs.animatefx)
    api(libs.richtextfx)
    api(libs.bundles.byte.buddy)

    api(libs.ngdbc)


    compileOnly("org.openjfx:javafx-base:$javafxVersion:$platform")
    compileOnly("org.openjfx:javafx-graphics:$javafxVersion:$platform")
    compileOnly("org.openjfx:javafx-controls:$javafxVersion:$platform")
    compileOnly("org.openjfx:javafx-fxml:$javafxVersion:$platform")
    compileOnly("org.openjfx:javafx-swing:$javafxVersion:$platform")

}
