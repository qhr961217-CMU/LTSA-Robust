/*
 * MIT License
 *
 * Copyright (c) 2020 Changjian Zhang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package edu.cmu.isr.robust.util

import lts.CompositeState
import lts.EventState

typealias Transition = Triple<Int, Int, Int>

interface Transitions {
  fun isEmpty(): Boolean
  fun isNotEmpty(): Boolean

  /**
   * Return a map from states to the transitions that come out from each state.
   */
  fun outTrans(): Map<Int, Iterable<Transition>>

  /**
   * Return a map from states to the transitions that come in each state.
   */
  fun inTrans(): Map<Int, Iterable<Transition>>

  /**
   * Return all the transitions as an iterable.
   */
  fun allTrans(): Iterable<Transition>

  /**
   *
   */
  fun allStates(): Set<Int>

  /**
   *
   */
  fun allEvents(): Set<Int>

  /**
   *
   */
  fun nextStates(s: Int): Set<Int>

  /**
   *
   */
  fun nextStates(s: Int, a: Int): Set<Int>

  /**
   *
   */
  fun prevStates(s: Int): Set<Int>

  /**
   *
   */
  fun prevStates(s: Int, a: Int): Set<Int>

  /**
   *
   */
  fun findEndStates(): Set<Int>

  /**
   *
   */
  fun transFromState(s: Int): Iterable<Transition>
}

class SimpleTransitions : Transitions {

  private val trans: Set<Transition>
  private var outMap: Map<Int, Iterable<Transition>>? = null
  private var inMap: Map<Int, Iterable<Transition>>? = null

  constructor(comp: CompositeState) {
    val m = comp.composition
    val ts = mutableSetOf<Transition>()
    for (s in m.states.indices) {
      for (a in m.alphabet.indices) {
        val nexts: IntArray? = EventState.nextState(m.states[s], a)
        if (nexts != null) {
          for (n in nexts) {
            ts.add(Triple(s, a, n))
          }
        }
      }
    }
    trans = ts
  }

  constructor(ts: Iterable<Transition>) {
    trans = ts.toSet()
  }

  override fun isEmpty(): Boolean {
    return trans.isEmpty()
  }

  override fun isNotEmpty(): Boolean {
    return trans.isNotEmpty()
  }

  override fun outTrans(): Map<Int, Iterable<Transition>> {
    if (outMap == null)
      outMap = trans.groupBy { it.first }
    return outMap!!
  }

  override fun inTrans(): Map<Int, Iterable<Transition>> {
    if (inMap == null)
      inMap = trans.groupBy { it.third }
    return inMap!!
  }

  override fun allTrans(): Iterable<Transition> {
    return trans
  }

  override fun allStates(): Set<Int> {
    return trans.flatMap { listOf(it.first, it.third) }.toSet()
  }

  override fun allEvents(): Set<Int> {
    return trans.map { it.second }.toSet()
  }

  override fun nextStates(s: Int): Set<Int> {
    return outTrans()[s]?.map { it.third }?.toSet() ?: emptySet()
  }

  override fun nextStates(s: Int, a: Int): Set<Int> {
    return outTrans()[s]?.filter { it.second == a }?.map { it.third }?.toSet() ?: emptySet()
  }

  override fun prevStates(s: Int): Set<Int> {
    return inTrans()[s]?.map { it.first }?.toSet() ?: emptySet()
  }

  override fun prevStates(s: Int, a: Int): Set<Int> {
    return inTrans()[s]?.filter { it.second == a }?.map { it.first }?.toSet() ?: emptySet()
  }

  override fun findEndStates(): Set<Int> {
    return inTrans().keys - outTrans().keys
  }

  override fun transFromState(s: Int): Iterable<Transition> {
    val trans = mutableListOf<Transition>()
    val visited = mutableSetOf<Int>()
    var search = setOf(s)
    while (search.isNotEmpty()) {
      visited.addAll(search)
      trans.addAll(search.flatMap { outTrans()[it] ?: emptyList() })
      search = search.flatMap { nextStates(it) }.toSet() - visited
    }
    return trans
  }

}
