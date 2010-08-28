/*
 * Copyright (C) 2010 Mikhail Vorozhtsov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.mvv.beans.scalac.test

import javax.annotation._
import scala.annotation.target._
import scala.reflect.BeanProperty
import org.specs._

class SimpleTestBean {
  @(BeanProperty @getter)
  @PostConstruct
  @Resource(name="test", shareable=true)
  @(PreDestroy @beanSetter)
  @(Resources @field)(Array(new Resource))
  var test = 0

  @(BeanProperty @setter @beanGetter)
  @PostConstruct
  var testMulti = 0
}

@(BeanProperty @beanGetter @beanSetter)
class ClassTestBean {
  @BeanProperty
  @PostConstruct
  @(Resource @getter)(name="test1")
  @(PreDestroy @field @setter)
  var test1 = 0

  @(BeanProperty @setter)
  @PostConstruct
  @(PreDestroy @field)
  @(Resources @scala.annotation.target.beanGetter)(Array(new Resource))
  var test2 = 0
}

class Tests extends SpecificationWithJUnit {
  val simpleField = classOf[SimpleTestBean].getDeclaredField("test")
  val simpleGetter = classOf[SimpleTestBean].getMethod("test")
  val simpleBeanSetter = classOf[SimpleTestBean].getMethod("setTest", classOf[Int])

  "Property defaults should be respected" in {
    simpleGetter.getAnnotation(classOf[PostConstruct]) must not be (null)
    simpleGetter.getAnnotation(classOf[Resource]) must not be (null)
    simpleGetter.getAnnotation(classOf[Resource]).name must_== "test"
    simpleField.getAnnotation(classOf[PostConstruct]) must be (null)
    simpleField.getAnnotation(classOf[Resource]) must be (null)
  }

  "Annotation overrides should take priority over proprety defaults" in {
    simpleBeanSetter.getAnnotation(classOf[PreDestroy]) must not be (null)
    simpleGetter.getAnnotation(classOf[PreDestroy]) must be (null)
    simpleField.getAnnotation(classOf[PreDestroy]) must be (null)
  }

  "Multitarget property defaults should work" in {
    val multiField = classOf[SimpleTestBean].getDeclaredField("testMulti")
    val multiSetter = classOf[SimpleTestBean].getMethod("testMulti_$eq", classOf[Int])
    val multiBeanGetter = classOf[SimpleTestBean].getMethod("getTestMulti")
    multiField.getAnnotation(classOf[PostConstruct]) must be (null)
    multiSetter.getAnnotation(classOf[PostConstruct]) must not be (null)
    multiBeanGetter.getAnnotation(classOf[PostConstruct]) must not be (null)
  }

  val test1Field = classOf[ClassTestBean].getDeclaredField("test1")
  val test1Getter = classOf[ClassTestBean].getMethod("test1")
  val test1Setter = classOf[ClassTestBean].getMethod("test1_$eq", classOf[Int])
  val test1BeanGetter = classOf[ClassTestBean].getMethod("getTest1")
  val test1BeanSetter = classOf[ClassTestBean].getMethod("setTest1", classOf[Int])

  "Class defaults should be respected" in {
    test1BeanGetter.getAnnotation(classOf[PostConstruct]) must not be (null)
    test1BeanSetter.getAnnotation(classOf[PostConstruct]) must not be (null)
    test1Field.getAnnotation(classOf[PostConstruct]) must be (null)
  }

  "Annotation overrides should take priority over class defaults" in {
    test1Getter.getAnnotation(classOf[Resource]) must not be (null)
    test1Field.getAnnotation(classOf[Resource]) must be (null)
    test1BeanGetter.getAnnotation(classOf[Resource]) must be (null)
    test1BeanSetter.getAnnotation(classOf[Resource]) must be (null)
    test1Field.getAnnotation(classOf[PreDestroy]) must not be (null)
    test1Setter.getAnnotation(classOf[PreDestroy]) must not be (null)
    test1BeanGetter.getAnnotation(classOf[Resource]) must be (null)
    test1BeanSetter.getAnnotation(classOf[Resource]) must be (null)
  }

  val test2Field = classOf[ClassTestBean].getDeclaredField("test2")
  val test2Setter = classOf[ClassTestBean].getMethod("test2_$eq", classOf[Int])
  val test2BeanGetter = classOf[ClassTestBean].getMethod("getTest2")
  val test2BeanSetter = classOf[ClassTestBean].getMethod("setTest2", classOf[Int])

  "Property defaults should take priority over class defaults" in {
    test2Setter.getAnnotation(classOf[PostConstruct]) must not be (null)
    test2Field.getAnnotation(classOf[PostConstruct]) must be (null)
    test2BeanGetter.getAnnotation(classOf[PostConstruct]) must be (null)
    test2BeanSetter.getAnnotation(classOf[PostConstruct]) must be (null)
  }

  "Annotation overrides should take priority over defaults" in {
    test2Field.getAnnotation(classOf[PreDestroy]) must not be (null)
    test2Setter.getAnnotation(classOf[PreDestroy]) must be (null)
    test2BeanGetter.getAnnotation(classOf[PreDestroy]) must be (null)
    test2BeanSetter.getAnnotation(classOf[PreDestroy]) must be (null)
  }

  "Fully qualified targets should be supported" in {
    test2BeanGetter.getAnnotation(classOf[Resources]) must not be (null)
    test2Field.getAnnotation(classOf[Resources]) must be (null)
    test2BeanSetter.getAnnotation(classOf[Resources]) must be (null)
  }
}
