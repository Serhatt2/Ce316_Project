plugins {
    id("buildlogic.java-application-conventions")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-text")
    implementation(project(":utilities"))
}

application {
    // Ana sınıf buraya:
    mainClass.set("com.MyEde.Main")
}

javafx {
    version = "23.0.2"
    modules = listOf("javafx.controls", "javafx.fxml")
}

tasks.named<JavaExec>("run") {
    jvmArgs = listOf(
        "--module-path", "/Users/serhataydin/Desktop/javafx-sdk-23.0.2/lib",
        "--add-modules", "javafx.controls,javafx.fxml"
    )
}
