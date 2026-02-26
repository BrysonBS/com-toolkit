plugins {
    application
    alias(libs.plugins.javafxplugin)
    alias(libs.plugins.beryx.runtime)
    alias(libs.plugins.shadow.plugin)
}
application {
    mainClass.set("cn.com.toolkit.app.application.Launcher")
    applicationDefaultJvmArgs = listOf(
        "-Dfile.encoding=UTF-8",
        "-Dsun.stdout.encoding=UTF-8",
        "-Dsun.stderr.encoding=UTF-8",
        "-Dsun.jnu.encoding=UTF-8",
        "-Dconsole.encoding=UTF-8"
    )
}

javafx {
    version = libs.versions.javafx.get()
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies{
    implementation(project(":module-framework:framework-core"))
}


runtime {
    options.set(listOf("--strip-debug", "--compress", "1", "--no-header-files", "--no-man-pages"))
    launcher {
        noConsole = true
    }
    jpackage {

        val currentOs = org.gradle.internal.os.OperatingSystem.current()
        val imgType = when {
            currentOs.isWindows -> "ico"
            currentOs.isMacOsX -> "icns"
            else -> "png"
        }
        imageOptions.addAll(listOf("--icon", "src/main/resources/assets/icons/icon.$imgType"))
        installerOptions.addAll(listOf(
            "--resource-dir", "src/main/resources",
            "--app-version", "${project.version}",
            //"--vendor", "",
            "--description", "Description",
            "--copyright", "Copyright 2025"
        ))

        when {
            currentOs.isWindows -> {
                installerOptions.addAll(listOf(
                    //"--win-per-user-install",
                    "--win-dir-chooser",
                    "--win-menu",
                    "--win-shortcut",
                    "--verbose"
                ))
            }
            currentOs.isLinux -> {
                installerOptions.addAll(listOf(
                    "--linux-package-name", "toolkit",
                    "--linux-shortcut"
                ))
            }
            currentOs.isMacOsX -> {
                installerOptions.addAll(listOf("--mac-package-name", "toolkit"))
            }
        }
    }
}
tasks.shadowJar {
    group = "build" //添加到build分组
    archiveBaseName.set(project.name)
    archiveClassifier.set("all")
    archiveVersion.set(project.version.toString())
    duplicatesStrategy = DuplicatesStrategy.WARN
    //重命名
    //archiveFileName.set("${project.name}-${project.version}-all.jar")


    // 关键：自动合并服务文件
    mergeServiceFiles()

    manifest {
        attributes(
            "Main-Class" to application.mainClass.get(),
            "Implementation-Version" to project.version,
            "Created-By" to "Gradle ${gradle.gradleVersion}",
            "Build-Jdk" to JavaVersion.current(),
        )
    }

    exclude(
        "META-INF/*.SF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
        "module-info.class",
        "**/.*",  // 排除隐藏文件
        "**/*.java",
        "**/*.groovy",
        "**/*.scala",
        "**/*.kt",
        "plugins/**",
    )

}

tasks.register<Copy>("jarMerge") {
    dependsOn(tasks.shadowJar)
    group = "build"
    val modules = listOf(
        ":module-tools:cpbio-tool",
        ":module-tools:json-tool",
        ":module-tools:cron-tool",
        ":module-tools:image-tool"
    )
    destinationDir = layout.buildDirectory.get().asFile
    //额外插件
    from(modules.map { modulePath ->
        project(modulePath).tasks.jar.get().archiveFile
    }){
        //相对于destinationDir
        into("libs/plugins")
    }

    if(destinationDir.resolve("jpackage/${project.name}/app").exists()){
        from(modules.map { modulePath ->
            project(modulePath).tasks.jar.get().archiveFile
        }){
            //相对于destinationDir
            into("jpackage/${project.name}/app/plugins")
        }
    }
}

tasks.jar{
    enabled = false
}
tasks.jpackageImage{
    finalizedBy("jarMerge");
}