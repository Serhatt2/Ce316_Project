plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.25.0"
}
javafx {
    version = "17.0.6" // veya 23.0.2'yi deniyorsan o da olur
    modules = listOf("javafx.controls", "javafx.fxml")
}


group = "com.MyIAE"
version = "1.0"

application {
    mainModule.set("com.MyIAE") // <--- eklenmesi gereken yer
    mainClass.set("com.MyIAE.Main")
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20230227")
}

jlink {
    imageZip.set(file("$buildDir/image.zip"))

    mergedModule {
        additive = true
        requires("jdk.compiler")
        requires("java.desktop")
    }

    launcher {
        name = "IAE_Launcher"
    }
}
