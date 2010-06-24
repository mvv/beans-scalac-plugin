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

package com.github.mvv.beans.scalac

import scala.tools.nsc
import nsc.Global
import nsc.transform.Transform
import nsc.plugins.{Plugin, PluginComponent}

class BeansPlugin(val global: Global) extends Plugin {
  import global._ 

  val name = "beans"
  val description = "Flexible property accessors generator"
  val components = List[PluginComponent](AnnotationProcessor)

  object AnnotationProcessor extends PluginComponent with Transform {
    val global: BeansPlugin.this.global.type = BeansPlugin.this.global
    val runsAfter = List("parser")
    override val runsBefore = List("namer")
    val phaseName = BeansPlugin.this.name

    import global._

    protected def newTransformer(unit: CompilationUnit) = new Transformer {
      override def transform(tree: Tree): Tree = tree match {
        case ClassDef(clsMods, clsName, tparams, impl) => atOwner(tree.symbol) {
          def isTargetName(name: Name) = name.toString match {
            case "param" | "field" | "getter" | "setter" |
                 "beanGetter" | "beanSetter" => true
            case _ => false
          }
          def annName(stree: Tree): Name = stree match {
            case Ident(name) => name
            case Select(_, name) => name
            case Annotated(_, sstree) => annName(sstree)
            case Apply(Select(New(sstree), _), _) => annName(sstree)
          }
          def annTree(stree: Tree): Tree = stree match {
            case Ident(_) => stree
            case Select(_, _) => stree
            case Annotated(_, sstree) => annTree(sstree)
          }
          def annotateAnn(ann: Tree, targets: List[Name]): Tree = ann match {
            case app @ Apply(sel @ Select(nw @ New(stree), selector), _) =>
              atOwner(app.symbol) {
                treeCopy.Apply(app, atOwner(sel.symbol) {
                    treeCopy.Select(sel, atOwner(nw.symbol) {
                        treeCopy.New(nw, atOwner(stree.symbol) {
                            def annotateWith(ts: List[Name]): Tree = ts match {
                              case Nil => stree
                              case target :: ts =>
                                treeCopy.Annotated(stree,
                                  treeCopy.Apply(stree,
                                    treeCopy.Select(stree,
                                      treeCopy.New(stree,
                                        treeCopy.Select(stree,
                                          treeCopy.Select(stree,
                                            treeCopy.Select(stree,
                                              treeCopy.Select(stree,
                                                treeCopy.Ident(stree,
                                                  newTermName("_root_")),
                                                newTermName("scala")),
                                              newTermName("annotation")),
                                            newTermName("target")),
                                          newTypeName(target.toString))),
                                      nme.CONSTRUCTOR), Nil), annotateWith(ts))
                            }
                            annotateWith(targets)
                          })
                      }, selector)
                  }, app.args)
              }
          }
          def deannotateAnn(ann: Tree) = ann match {
            case app @ Apply(sel @ Select(nw @ New(stree), selector), _) =>
              atOwner(app.symbol) {
                treeCopy.Apply(app, atOwner(sel.symbol) {
                    treeCopy.Select(sel, atOwner(nw.symbol) {
                        treeCopy.New(nw, atOwner(stree.symbol) {
                            annTree(stree)
                          })
                      }, selector)
                  }, app.args)
              }
          }
          def annAnnNames(stree: Tree): List[Name] = {
            def annAnnNamesRev(stree: Tree): List[Name] = stree match {
              case Ident(name) => List(name)
              case Select(_, name) => List(name)
              case Annotated(ann, sstree) => annName(ann) :: annAnnNamesRev(sstree)
              case Apply(Select(New(sstree), _), _) => annAnnNamesRev(sstree)
            }
            annAnnNamesRev(stree).reverse
          }
          def findDefaults(anns: List[Tree], forCls: Boolean) = {
            val none = None.asInstanceOf[Option[List[Name]]]
            val (dflt, newAnns) = anns.foldLeft((none, List[Tree]())) {
              case ((dflt, newAnns), ann) => annAnnNames(ann) match {
                case name :: rest if name.toString == "BeanProperty" ||
                                     (!forCls && name.toString == "BooleanBeanProperty") =>
                  (Some(rest.view.filter(isTargetName(_)).toSet.toList),
                   if (forCls)
                     newAnns
                   else
                     deannotateAnn(ann) :: newAnns)
                case _ => (dflt, ann :: newAnns)
              }
            }
            (dflt, newAnns.reverse)
          }
          val (clsDflt, newClsMods) = {
            val (dflt, newAnns) = findDefaults(clsMods.annotations, true)
            (dflt.getOrElse(Nil),
             Modifiers(clsMods.flags, clsMods.privateWithin,
                       newAnns, clsMods.positions))
          }
          treeCopy.ClassDef(
            tree, newClsMods, clsName, transformTypeDefs(tparams),
            atOwner(impl.symbol) {
              treeCopy.Template(
                impl, transformTrees(impl.parents), transformValDef(impl.self),
                impl.body mapConserve {
                  case vd @ ValDef(mods, name, tpt, rhs) =>
                    val newMods = {
                      val (dflt, anns) = findDefaults(mods.annotations, false)
                      val newAnns = dflt.map { s =>
                        val targets = if (s.isEmpty) clsDflt else s
                        if (targets.isEmpty)
                          anns
                        else
                          anns.map { ann =>
                            annAnnNames(ann) match {
                              case name :: rest if name.toString != "BeanProperty" && 
                                                   name.toString != "BooleanBeanProperty" &&
                                                   rest.view.filter(isTargetName(_)).isEmpty =>
                                annotateAnn(ann, targets)
                              case _ => ann
                            }
                          }
                      } getOrElse(anns)
                      Modifiers(mods.flags, mods.privateWithin,
                                newAnns, mods.positions)
                    }
                    atOwner(vd.symbol) {
                      treeCopy.ValDef(vd, newMods, name, super.transform(tpt),
                                      super.transform(rhs))
                    }
                  case t => super.transform(t)
                })
            })
        }
        case _ => super.transform(tree)
      }
    } 
  }
}
