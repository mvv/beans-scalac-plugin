Beans plugin for the Scala compiler
===================================
Scala 2.8 makes it possible to control annotation placement by marking
them with special annotations from the `scala.annotation.target` package.
But the way you are supposed to do it is rather explicit and verbose.
This plugin allows one to define per-class and per-property placement
defaults, for example:

	@(BeanProperty @getter @beanGetter) // Per-class default
	class MyBean {
	  @BeanProperty // Inheriting the class default
	  @Ann1 // Goes to the getter and the bean getter
	  @(Ann2 @field) // Goes to the field *only*
	  var prop1 = 0
	
	  @(BeanProperty @beanSetter) // Per-property default
	  @Ann1 // Goes to the bean setter *only*
	  @(Ann2 @getter) // Goes to the getter *only*
	  var prop2 = 0
	}

Installation
------------
### Automatic
Plugin artifacts are available from the [Maven Central](http://search.maven.com),
just follow the instructions in the Usage section.

### From source
Install [Simple Build Tool](http://www.scala-sbt.org), run

	$ sbt update publish-local publish-local-maven

Usage
-----
### Simple Build Tool
Just add the following lines to your build file:

	autoCompilerPlugins := true
	
	addCompilerPlugin("com.github.mvv.beans-scalac-plugin" %% "beans-scalac-plugin" % "0.2")

### Maven
With the [maven-scala-plugin](http://scala-tools.org/mvnsites/maven-scala-plugin):

	<plugin>
	  <groupId>org.scala-tools</groupId>
	  <artifactId>maven-scala-plugin</artifactId>
	  ...
	  <configuration>
	    ...
	    <compilerPlugins>
	      ...
	      <compilerPlugin>
	        <groupId>com.github.mvv.beans-scalac-plugin</groupId>
	        <artifactId>beans-scalac-plugin</artifactId>
	        <version>0.2</version>
	      </compilerPlugin>
	    </compilerPlugins>
	  </configuration>

### Manual

	$ scalac -Xplugin:<PATH-TO-THE-JAR-FILE> <YOUR-OPTIONS>

