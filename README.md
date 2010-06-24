Beans plugin for the Scala compiler
===================================
Scala 2.8 allows us to control annotation placement for properties by
marking them with special annotations from the scala.annotation.target
package. But the way you are supposed to do it is rather explicit and
verbose. This plugin allows you to define per-class and per-property
placement defaults, for example:
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
The usual:
	$ mvn install

Usage
-----
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
	        <version>0.1.0-SNAPSHOT</version>
	      </compilerPlugin>
	    </compilerPlugins>
	  </configuration>
Manual:
	$ scalac -Xplugin:<PATH-TO-THE-JAR-FILE> <YOUR-OPTIONS>

