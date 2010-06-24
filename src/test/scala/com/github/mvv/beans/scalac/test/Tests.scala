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
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers

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

@RunWith(classOf[JUnitRunner])
class Tests extends Spec with ShouldMatchers {
  def testIt = execute()

  val simpleField = classOf[SimpleTestBean].getDeclaredField("test")
  val simpleGetter = classOf[SimpleTestBean].getMethod("test")
  val simpleBeanSetter = classOf[SimpleTestBean].getMethod("setTest", classOf[Int])

  it("Property defaults should be respected") {
    simpleGetter.getAnnotation(classOf[PostConstruct]) should not be (null)
    simpleGetter.getAnnotation(classOf[Resource]) should not be (null)
    simpleGetter.getAnnotation(classOf[Resource]).name should be ("test")
    simpleField.getAnnotation(classOf[PostConstruct]) should be (null)
    simpleField.getAnnotation(classOf[Resource]) should be (null)
  }

  it("Annotation overrides should take priority over proprety defaults") {
    simpleBeanSetter.getAnnotation(classOf[PreDestroy]) should not be (null)
    simpleGetter.getAnnotation(classOf[PreDestroy]) should be (null)
    simpleField.getAnnotation(classOf[PreDestroy]) should be (null)
  }

  it("Multitarget property defaults should work") {
    val multiField = classOf[SimpleTestBean].getDeclaredField("testMulti")
    val multiSetter = classOf[SimpleTestBean].getMethod("testMulti_$eq", classOf[Int])
    val multiBeanGetter = classOf[SimpleTestBean].getMethod("getTestMulti")
    multiField.getAnnotation(classOf[PostConstruct]) should be (null)
    multiSetter.getAnnotation(classOf[PostConstruct]) should not be (null)
    multiBeanGetter.getAnnotation(classOf[PostConstruct]) should not be (null)
  }

  val test1Field = classOf[ClassTestBean].getDeclaredField("test1")
  val test1Getter = classOf[ClassTestBean].getMethod("test1")
  val test1Setter = classOf[ClassTestBean].getMethod("test1_$eq", classOf[Int])
  val test1BeanGetter = classOf[ClassTestBean].getMethod("getTest1")
  val test1BeanSetter = classOf[ClassTestBean].getMethod("setTest1", classOf[Int])

  it("Class defaults should be respected") {
    test1BeanGetter.getAnnotation(classOf[PostConstruct]) should not be (null)
    test1BeanSetter.getAnnotation(classOf[PostConstruct]) should not be (null)
    test1Field.getAnnotation(classOf[PostConstruct]) should be (null)
  }

  it("Annotation overrides should take priority over class defaults") {
    test1Getter.getAnnotation(classOf[Resource]) should not be (null)
    test1Field.getAnnotation(classOf[Resource]) should be (null)
    test1BeanGetter.getAnnotation(classOf[Resource]) should be (null)
    test1BeanSetter.getAnnotation(classOf[Resource]) should be (null)
    test1Field.getAnnotation(classOf[PreDestroy]) should not be (null)
    test1Setter.getAnnotation(classOf[PreDestroy]) should not be (null)
    test1BeanGetter.getAnnotation(classOf[Resource]) should be (null)
    test1BeanSetter.getAnnotation(classOf[Resource]) should be (null)
  }

  val test2Field = classOf[ClassTestBean].getDeclaredField("test2")
  val test2Setter = classOf[ClassTestBean].getMethod("test2_$eq", classOf[Int])
  val test2BeanGetter = classOf[ClassTestBean].getMethod("getTest2")
  val test2BeanSetter = classOf[ClassTestBean].getMethod("setTest2", classOf[Int])

  it("Property defaults should take priority over class defaults") {
    test2Setter.getAnnotation(classOf[PostConstruct]) should not be (null)
    test2Field.getAnnotation(classOf[PostConstruct]) should be (null)
    test2BeanGetter.getAnnotation(classOf[PostConstruct]) should be (null)
    test2BeanSetter.getAnnotation(classOf[PostConstruct]) should be (null)
  }

  it("Annotation overrides should take priority over defaults") {
    test2Field.getAnnotation(classOf[PreDestroy]) should not be (null)
    test2Setter.getAnnotation(classOf[PreDestroy]) should be (null)
    test2BeanGetter.getAnnotation(classOf[PreDestroy]) should be (null)
    test2BeanSetter.getAnnotation(classOf[PreDestroy]) should be (null)
  }

  it("Fully qualified targets should be supported") {
    test2BeanGetter.getAnnotation(classOf[Resources]) should not be (null)
    test2Field.getAnnotation(classOf[Resources]) should be (null)
    test2BeanSetter.getAnnotation(classOf[Resources]) should be (null)
  }
}