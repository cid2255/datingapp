// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
buildscript {
    dependencies {
        classpath 'com.google.gms:google-services:4.4.1'
    }
}
plugins {
    id 'com.android.application'
    id 'com.google.gms.google-services'
}

dependencies {
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-auth'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.10.0'
}
implementation 'com.github.bumptech.glide:glide:4.16.0'
kapt 'com.github.bumptech.glide:compiler:4.16.0'
implementation 'com.google.firebase:firebase-firestore'
implementation 'com.google.firebase:firebase-storage'
implementation 'com.github.bumptech.glide:glide:4.16.0'
