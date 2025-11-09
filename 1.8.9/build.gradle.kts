import com.google.common.jimfs.Jimfs
import org.kamranzafar.jtar.TarEntry
import org.kamranzafar.jtar.TarHeader
import org.kamranzafar.jtar.TarOutputStream
import org.tukaani.xz.LZMA2Options
import org.tukaani.xz.XZ
import org.tukaani.xz.XZOutputStream
import java.io.BufferedOutputStream
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.io.path.*

plugins {
	id("fabric-loom")
	id("ploceus")
	id("io.github.p03w.machete")
}

val minecraftVersion = "1.8.9"
val feather = "1.8.9+build.27"
val osl = "0.16.3"
val lwjglVersion = "3.3.6"

group = project.property("maven_group")!!
version = "${project.property("version")}+$minecraftVersion"

base.archivesName = project.property("archives_base_name").toString()

loom {
	accessWidenerPath.set(file("src/main/resources/axolotlclient-rendering.accesswidener"))

	mods {
		create("axolotlclient-rendering") {
			sourceSet("main")
		}
		create("axolotlclient-rendering-test") {
			sourceSet("test")
		}
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://jitpack.io") }
}

configurations {
	create("embedCompressed")
}

dependencies {
	minecraft("com.mojang:minecraft:$minecraftVersion")
	mappings("net.ornithemc:feather:$feather")

	modImplementation("net.fabricmc:fabric-loader:${project.property("fabric_loader")}")

	ploceus.dependOsl(osl)

	modImplementation("com.terraformersmc:modmenu:0.3.1+mc1.8.9")

	implementation(include(project(path = ":common", configuration = "shadow"))!!)

	modImplementation("io.github.moehreag:legacy-lwjgl3:${project.property("legacy_lwgjl3")}")

	"embedCompressed"(implementation("org.lwjgl:lwjgl-freetype:$lwjglVersion")!!)

	listOf("linux", "windows", "macos", "windows-arm64", "macos-arm64", "linux-arm64").forEach { platform ->
		"embedCompressed"(runtimeOnly("org.lwjgl:lwjgl-freetype:$lwjglVersion:natives-$platform")!!)
	}

	//implementation("org.apache.xmlgraphics:batik-transcoder:1.19")
	implementation("embedCompressed"("org.apache.xmlgraphics:batik-transcoder:1.19") {
		isTransitive = false
	})
	implementation("embedCompressed"("org.apache.xmlgraphics:batik-anim:1.19")!!)
	implementation("embedCompressed"("org.apache.xmlgraphics:batik-shared-resources:1.19")!!)
	implementation("embedCompressed"("org.apache.xmlgraphics:batik-util:1.19")!!)
	implementation("embedCompressed"("org.apache.xmlgraphics:batik-constants:1.19")!!)
	implementation("embedCompressed"("org.apache.xmlgraphics:batik-i18n:1.19")!!)
	implementation("embedCompressed"("org.apache.xmlgraphics:xmlgraphics-commons:2.11")!!)
	implementation("embedCompressed"("org.apache.xmlgraphics:batik-css:1.19")!!)
	implementation("embedCompressed"("org.apache.xmlgraphics:batik-dom:1.19")!!)
	implementation("embedCompressed"("org.apache.xmlgraphics:batik-ext:1.19")!!)
	implementation("embedCompressed"("org.apache.xmlgraphics:batik-xml:1.19")!!)
	implementation("embedCompressed"("org.apache.xmlgraphics:batik-parser:1.19")!!)
	implementation("embedCompressed"("org.apache.xmlgraphics:batik-svg-dom:1.19")!!)
	implementation("embedCompressed"("org.apache.xmlgraphics:batik-bridge:1.19")!!)
	implementation("embedCompressed"("org.apache.xmlgraphics:batik-gvt:1.19")!!)
	implementation("embedCompressed"("org.apache.xmlgraphics:batik-script:1.19")!!)
	implementation("embedCompressed"("org.apache.xmlgraphics:batik-awt-util:1.19")!!)
	implementation("embedCompressed"("xml-apis:xml-apis-ext:1.3.04")!!)

	include(implementation("org.kamranzafar:jtar:2.3")!!)
	include(implementation("org.tukaani:xz:1.10")!!)
}

buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("org.kamranzafar:jtar:2.3")
		classpath("org.tukaani:xz:1.10")
		classpath("com.google.jimfs:jimfs:1.3.1")
	}
}

configurations.configureEach {
	exclude("org.apache.xmlgraphics:batik-svggen:1.19")
	exclude("xml-apis", "xml-apis")
	exclude("org.lwjgl.lwjgl")
	resolutionStrategy {
		dependencySubstitution {
			substitute(module("io.netty:netty-all:4.0.23.Final")).using(module("io.netty:netty-all:4.1.9.Final"))
		}
		force("io.netty:netty-all:4.1.9.Final")
	}
}

tasks.processResources {
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}

	val out = layout.buildDirectory.dir("resources").get().asFile.toPath().resolve("main")
		.resolve("libraries.tar.xz")
	actions.addLast {
		val jijs = configurations.getByName("include").dependencies.toSet()
		val includes = configurations.getByName("embedCompressed").resolvedConfiguration
			.resolvedArtifacts
			.filter { jijs.none { jij -> it.moduleVersion.toString() + (if (it.classifier != null) ":" + it.classifier else "") == jij.toString() } }
			.map { it.file.toPath() }


		out.parent.createDirectories()
		out.deleteIfExists()
		Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix()).use { memFs ->

			val memPaths = ConcurrentLinkedDeque<java.nio.file.Path>()
			val futs = mutableListOf<CompletableFuture<*>>()
			includes.forEach { jar ->
				futs.add(CompletableFuture.runAsync {
					val memPath = memFs.getPath(jar.fileName.toString())
					FileSystems.newFileSystem(
						memPath,
						mapOf("create" to "true", "compressionMethod" to "stored")
					).use { fs ->
						FileSystems.newFileSystem(jar).use { file ->
							Files.walkFileTree(
								file.getPath("/"),
								object : SimpleFileVisitor<java.nio.file.Path>() {
									override fun visitFile(
										file: java.nio.file.Path,
										attrs: BasicFileAttributes
									): FileVisitResult {
										val f = fs.getPath(file.toString())
										f.parent.createDirectories()
										file.copyTo(f)
										return super.visitFile(file, attrs)
									}
								})
						}
					}
					memPaths.add(memPath)
				})
			}
			CompletableFuture.allOf(*futs.toTypedArray()).join()
			out.outputStream().use { outputStream ->
				BufferedOutputStream(outputStream).use { buf ->
					XZOutputStream(buf, LZMA2Options(6), XZ.CHECK_SHA256).use { xz ->
						TarOutputStream(xz).use { tar ->
							memPaths.forEach { memPath ->
								val e = TarEntry(
									TarHeader.createHeader(
										memPath.fileName.toString(),
										memPath.fileSize(),
										0,
										false,
										444
									)
								)
								tar.putNextEntry(e)
								Files.copy(memPath, tar)
								tar.flush()
							}
						}
					}
				}
			}
		}
	}
	outputs.file(out)
}

tasks.processIncludeJars.configure {
	fun recompressNestedJar(jar: File) {
		val out = jar.resolveSibling(jar.name + ".stored")
		out.toPath().deleteIfExists()
		jar.inputStream().use { stream ->
			ZipInputStream(stream).use { zipIn ->
				out.outputStream().use { outStream ->
					ZipOutputStream(outStream).use { zipOut ->
						zipOut.setMethod(ZipOutputStream.STORED)
						var entry: ZipEntry? = zipIn.nextEntry
						while (entry != null) {
							val entryBytes = zipIn.readAllBytes()
							if (entry.method == ZipEntry.DEFLATED) {
								entry.method = ZipEntry.STORED
								entry.size = entryBytes.size.toLong()
								entry.compressedSize = entry.size
							}
							zipOut.putNextEntry(entry)
							zipOut.write(entryBytes)
							entry = zipIn.nextEntry
						}
						zipOut.closeEntry()
					}
				}
			}
		}
		out.toPath().moveTo(jar.toPath(), StandardCopyOption.REPLACE_EXISTING)
	}

	outputs.upToDateWhen { false }
	actions.addLast {
		outputs.files.asFileTree.files.forEach { recompressNestedJar(it) }
	}
}

tasks.runClient {
	if (project.property("native_glfw") == "true") {
		val glfwPath = project.properties.getOrDefault("native_glfw_path", "/usr/lib/libglfw.so")
		jvmArgs("-Dorg.lwjgl.glfw.libname=$glfwPath")
	}
	classpath(sourceSets.getByName("test").runtimeClasspath)
	jvmArgs("-XX:+AllowEnhancedClassRedefinition", "-XX:+IgnoreUnrecognizedVMOptions")
}

tasks.withType(JavaCompile::class).configureEach {
	options.encoding = "UTF-8"

	if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_18)) {
		options.release = 17
	}
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

// Configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = base.archivesName.get()
			from(components["java"])
		}
	}

	repositories {
		maven {
			name = "owlMaven"
			val repository = if (project.version.toString().contains("beta") || project.version.toString()
					.contains("alpha")
			) "snapshots" else "releases"
			url = uri("https://moehreag.duckdns.org/maven/$repository")
			credentials(PasswordCredentials::class)
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
	}
}

tasks.modrinth {
	dependsOn(tasks.getByName("optimizeOutputsOfRemapJar"))
}

modrinth {
	token = System.getenv("MODRINTH_TOKEN")
	projectId = ""
	versionNumber = "${project.version}"
	versionType = if (project.version.toString().contains("beta")) {
		"beta"
	} else if (project.version.toString().contains("alpha")) {
		"alpha"
	} else {
		"release"
	}
	uploadFile = tasks.remapJar.get()
	gameVersions.set(listOf(minecraftVersion))
	loaders.set(listOf("ornithe"))
	additionalFiles.set(listOf(tasks.remapSourcesJar))
	dependencies {
		required.project("osl")
		required.project("moehreag-legacy-lwjgl3")
	}

	// Changelog fetching: Credit LambdAurora.
	// https://github.com/LambdAurora/LambDynamicLights/blob/1ef85f486084873b5d97b8a08df72f57859a3295/build.gradle#L145
	// License: MIT
	val changelogText = file("../CHANGELOG.md").readText()
	val regexVersion =
		((project.version) as String).split("+")[0].replace("\\.".toRegex(), "\\.").replace("\\+".toRegex(), "+")
	val changelogRegex = "###? ${regexVersion}\\n\\n(( *- .+\\n)+)".toRegex()
	val matcher = changelogRegex.find(changelogText)

	if (matcher != null) {
		var changelogContent = matcher.groups[1]?.value

		val changelogLines = changelogText.split("\n")
		val linkRefRegex = "^\\[([A-z0-9 _\\-/+.]+)]: ".toRegex()
		for (line in changelogLines.reversed()) {
			if ((linkRefRegex.matches(line)))
				changelogContent += "\n" + line
			else break
		}
		changelog = changelogContent
	} else {
		afterEvaluate {
			tasks.modrinth.configure { isEnabled = false }
		}
	}
}
