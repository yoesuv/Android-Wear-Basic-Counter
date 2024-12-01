import org.gradle.api.JavaVersion

object AndroidDependencies {

    val compileSource = JavaVersion.VERSION_11
    val compileTarget = JavaVersion.VERSION_11
    const val jvmTarget = "11"

    const val core = "androidx.core:core-ktx:1.12.0"
    const val compose = "androidx.activity:activity-compose:1.8.2"
    const val lifecycle = "androidx.lifecycle:lifecycle-runtime-ktx:2.7.0"
    const val lifeData = "androidx.compose.runtime:runtime-livedata:1.6.0"
    const val iconExtended = "androidx.compose.material:material-icons-extended:1.6.0"

}

object WearableDependencies {

    const val wearable = "com.google.android.gms:play-services-wearable:18.1.0"

}

object CoroutineDependencies {

    const val playService = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.1"

}

object KotlinDependencies {

    const val kotlinCompiler = "1.5.14"

}