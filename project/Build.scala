import sbt._
import Keys._

object BeansScalacPluginBuild extends Build {
  val localMavenRepo =
    Resolver.file("local-maven", Path.userHome / ".m2" / "repository")(
                  Resolver.mavenStylePatterns)

  val publishLocalMavenConfiguration =
    TaskKey[PublishConfiguration](
      "publish-local-maven-configuration",
      "Configuration for publishing to the local maven repository.")
  val publishLocalMaven =
    TaskKey[Unit]("publish-local-maven",
                  "Publishes artifacts to the local maven repository.")

  val buildSettings = Seq(
    organization := "com.github.mvv.beans-scalac-plugin",
    version := "0.2",
    scalaVersion := "2.9.2",
    crossScalaVersions := Seq("2.8.1", "2.9.0", "2.9.0-1",
                              "2.9.1", "2.9.1-1", "2.9.2"),
    scalaSource in Compile <<= baseDirectory / "src",
    resourceDirectory in Compile <<= baseDirectory / "resources",
    scalaSource in Test <<= baseDirectory / "tests",
    unmanagedSourceDirectories in Compile <<= Seq(scalaSource in Compile).join,
    unmanagedSourceDirectories in Test <<= Seq(scalaSource in Test).join,
    publishArtifact in Test := false,
    resolvers += localMavenRepo,
    publishLocalMavenConfiguration <<=
      (packagedArtifacts, deliverLocal, checksums in publishLocal,
       ivyLoggingLevel) map {
        (artifacts, _, chsums, level) => 
          new PublishConfiguration(
                None, localMavenRepo.name, artifacts, chsums, level)
      },
    publishLocalMaven <<=
      Classpaths.publishTask(publishLocalMavenConfiguration, deliverLocal))

  val publishSettings = Seq(
    publishMavenStyle := true,
    publishTo := Some(
      "releases" at
      "https://oss.sonatype.org/service/local/staging/deploy/maven2"),
    pomIncludeRepository := { _ => false },
    pomExtra := (
      <url>http://github.com/mvv/beans-scalac-plugin</url>
      <licenses>
        <license>
          <name>Apache License, Version 2.0</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:mvv/beans-scalac-plugin.git</url>
        <connection>scm:git:git@github.com:mvv/beans-scalac-plugin.git</connection>
      </scm>
      <developers>
        <developer>
          <id>mvv</id>
          <name>Mikhail Vorozhtsov</name>
          <url>http://github.com/mvv</url>
        </developer>
      </developers>),
    pomPostProcess := { pom =>
      import scala.xml.transform.{RewriteRule, RuleTransformer}
      val dropTestDeps = new RewriteRule {
        override def transform(n: scala.xml.Node) =
          if (n.label == "dependency" && (n \ "scope").text == "test")
            Seq.empty
          else
            Seq(n)
      }
      new RuleTransformer(dropTestDeps)(pom)
    })

  lazy val beansScalacPlugin =
    Project("beans-scalac-plugin", file(".")) 
      .settings(buildSettings: _*)
      .settings(publishSettings: _*)
      .settings(
         resolvers <++= scalaVersion {
           case v if v.startsWith("2.8.") =>
             Seq("sonatype-snapshots" at
                   "http://oss.sonatype.org/content/repositories/snapshots")
           case _ => Seq.empty
         },
         libraryDependencies <+= scalaVersion { v =>
           "org.scala-lang" % "scala-compiler" % v % "provided"
         },
         libraryDependencies <+= scalaVersion { v =>
           val v1 = if (v.startsWith("2.8.") || v.startsWith("2.9.0")) "1.5"
                    else "1.12.2"
           "org.specs2" %% "specs2" % v1 % "test"
         },
         scalacOptions in Test <+= (packageBin in Compile) map { file =>
           "-Xplugin:" + file
         })
}

