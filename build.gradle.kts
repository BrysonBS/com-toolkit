
allprojects {
    apply<JavaPlugin>()

    group = "cn.com.toolkit"
    version = "0.0.1"

    configure<JavaPluginExtension> {
        toolchain {
            afterEvaluate {
                languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
            }
        }
    }

    repositories {
        mavenLocal()
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        mavenCentral()
    }
}
subprojects {

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    tasks.withType<JavaExec> {
        systemProperty("file.encoding", "utf-8")
        systemProperty("sun.stdout.encoding", "utf-8")
        systemProperty("sun.stderr.encoding", "utf-8")
        systemProperty("console.encoding", "utf-8")
    }
    tasks.withType<Test> {
        useJUnitPlatform()
    }
    project.dependencies {
        afterEvaluate {
            add("implementation", libs.lombok)
            add("annotationProcessor", libs.lombok)
            add("testImplementation", libs.jupiter.api)
            add("testRuntimeOnly", libs.jupiter.engine)
        }
    }
}
