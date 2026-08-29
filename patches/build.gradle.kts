group = "app.diskwala"

patches {
    about {
        name = "DiskWala Patches"
        description = "Morphe patches for DiskWala - Disable ads & Remove forced update screen"
        source = "git@github.com:kuntal-devrat/diskwala-patches.git"
        author = "kuntal-devrat"
        contact = "na"
        website = "https://www.diskwala.com"
        license = "GPLv3"
    }
}

val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    implementation(libs.morphePatchesLibrary)
    compileOnly(libs.annotation)
    compileOnly(libs.guava)
    compileOnly(libs.androidxJavascriptengine)
    compileOnly(libs.collections4)
    compileOnly(libs.lang3)
    compileOnly(libs.hiddenapi)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("app.morphe.util.PatchListGeneratorKt")
    }

    publish {
        dependsOn("generatePatchesList")
    }
}
