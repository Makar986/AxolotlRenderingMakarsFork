// Standalone build: Essential Loom conflicts with Fabric Loom on the root plugin classpath,
// so this project is pulled into the root build via includeBuild instead of include.
pluginManagement {
	repositories {
		maven("https://repo.essential.gg/repository/maven-public/")
		maven("https://maven.architectury.dev/")
		maven("https://maven.fabricmc.net/")
		maven("https://maven.minecraftforge.net/")
		gradlePluginPortal()
	}
}

rootProject.name = "1.12.2.forge"
