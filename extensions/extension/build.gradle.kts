plugins {
    id("com.android.library") version "8.2.2"
}

android {
    namespace = "app.diskwala.extension"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
