import com.diffplug.spotless.LineEnding
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.external.javadoc.CoreJavadocOptions
import org.gradle.jvm.tasks.Jar

plugins {
    id("dev.prism")
    id("com.diffplug.spotless") version "8.9.0"
}

fun prop(name: String): String = providers.gradleProperty(name).get()

val minecraftVersion = prop("minecraft_version")

group = prop("mod_group_id")
version = prop("mod_version")

repositories {
    mavenCentral()
}

spotless {
    lineEndings = LineEnding.UNIX
    java {
        target(fileTree("src") { include("**/*.java") })
        eclipse("4.26").configProperties(
            """
            org.eclipse.jdt.core.formatter.lineSplit=200
            org.eclipse.jdt.core.formatter.join_wrapped_lines=true
            org.eclipse.jdt.core.formatter.comment.format_javadoc_comments=false
            org.eclipse.jdt.core.formatter.comment.format_block_comments=false
            org.eclipse.jdt.core.formatter.comment.format_line_comments=false
            org.eclipse.jdt.core.formatter.keep_method_body_on_one_line=one_line_if_empty
            org.eclipse.jdt.core.formatter.keep_type_declaration_on_one_line=one_line_if_empty
            org.eclipse.jdt.core.formatter.alignment_for_parameters_in_constructor_declaration=0
            org.eclipse.jdt.core.formatter.alignment_for_parameters_in_method_declaration=0
            org.eclipse.jdt.core.formatter.tabulation.char=space
            org.eclipse.jdt.core.formatter.tabulation.size=4
            org.eclipse.jdt.core.formatter.indentation.size=4
            org.eclipse.jdt.core.formatter.continuation_indentation=2
            org.eclipse.jdt.core.formatter.number_of_empty_lines_to_preserve=1
            """.trimIndent()
        )
        trimTrailingWhitespace()
        endWithNewline()
    }
}

prism {
    curseMaven()
    modrinthMaven()
    maven("BlameJared", "https://maven.blamejared.com/")
    maven("IllusiveSoulworks", "https://maven.theillusivec4.top/")
    maven("FTB", "https://maven.ftb.dev/releases")

    metadata {
        modId = prop("mod_id")
        name = prop("mod_name")
        description = prop("mod_description")
        license = prop("mod_license")
        prop("mod_authors").split(",").forEach { author(it.trim()) }
        expand("minecraft_version_range", prop("minecraft_version_range"))
        expand("neoforge_version_range", prop("neo_version_range"))
    }

    version(minecraftVersion) {
        parchmentMinecraftVersion = prop("parchment_minecraft_version")
        parchmentMappingsVersion = prop("parchment_mappings_version")

        neoforge {
            loaderVersion = prop("neo_version")

            dependencies {
                implementation("maven.modrinth:XaDC71GB:84kuUpr4")

                compileOnly("mezz.jei:jei-$minecraftVersion-common-api:${prop("jei_version")}")
                compileOnly("mezz.jei:jei-$minecraftVersion-neoforge-api:${prop("jei_version")}")
                compileOnly("mezz.jei:jei-$minecraftVersion-neoforge:${prop("jei_version")}")
                runtimeOnly("mezz.jei:jei-$minecraftVersion-neoforge:${prop("jei_version")}")

                compileOnly("top.theillusivec4.curios:curios-neoforge:${prop("curios_version")}+$minecraftVersion:api")
                runtimeOnly("top.theillusivec4.curios:curios-neoforge:${prop("curios_version")}+$minecraftVersion")

                compileOnly("maven.modrinth:jade:${prop("jade_version")}")
                compileOnly("dev.ftb.mods:ftb-library-neoforge:${prop("ftb_library_version")}")
                runtimeOnly("maven.modrinth:jade:${prop("jade_version")}")

                compileOnly("maven.modrinth:distanthorizons:${prop("distant_horizons_version")}")
                compileOnly("maven.modrinth:vdjF5PL5:${prop("dynamic_trees_version")}")
                compileOnly("maven.modrinth:iris:${prop("iris_version")}")
                runtimeOnly("maven.modrinth:iris:${prop("iris_version")}")
                runtimeOnly("maven.modrinth:sodium:${prop("sodium_version")}")
                runtimeOnly("maven.modrinth:spark:${prop("spark_version")}")
            }

            rawProject {
                val sourceSets = extensions.getByType<SourceSetContainer>()
                val main = sourceSets.getByName("main")
                val test = sourceSets.getByName("test")
                test.compileClasspath += main.output + main.compileClasspath
                test.runtimeClasspath += main.output + main.runtimeClasspath
                tasks.named("test") { enabled = false }

                extensions.configure<JavaPluginExtension> { withJavadocJar() }
                tasks.withType<Javadoc>().configureEach {
                    (options as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
                    isFailOnError = false
                }
                tasks.named<Jar>("jar") { exclude("com/leclowndu93150/thaumaturge/debug/**") }

                extensions.configure<NeoForgeExtension> {
                    mods.named(prop("mod_id")) { sourceSet(test) }
                    runs.named("client") { gameDirectory.set(file("run")) }
                    runs.named("server") {
                        gameDirectory.set(file("run"))
                        programArgument("--nogui")
                    }
                    runs.create("gameTestServer") {
                        type.set("gameTestServer")
                        systemProperty("thaumaturge.gametest", "true")
                        gameDirectory.set(file("run-gametest"))
                    }
                }

                val gameTestClasses = files(test.output)
                configurations.matching { it.name == "runGameTestServerAdditionalRuntimeClasspath" }.configureEach {
                    extendsFrom(configurations.getByName("runtimeClasspath"))
                    project.dependencies.add(name, gameTestClasses)
                }
            }
        }
    }

    publishing {
        changelogFile = "changelog.md"

        curseforge {
            accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
            projectId = prop("curseforge_id")
        }

        github {
            repository = prop("github_repo")
            tagName = "${prop("mod_version")}-$minecraftVersion"
        }

        maven {
            name = "Leclown"
            url = prop("leclown_maven_url")
            credentialsFromEnv("MAVEN_USER", "MAVEN_PASS")
        }

        dependencies {
            requires("curios")
            requires("lithostitched")
            optional("jei")
        }
    }
}
