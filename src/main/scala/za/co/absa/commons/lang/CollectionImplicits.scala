/*
 * Copyright 2020 ABSA Group Limited
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

package za.co.absa.commons.lang

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import scala.language.higherKinds
import scala.reflect.ClassTag

@deprecated("Use type-specific ...Extension instead", "1.1.0")
object CollectionImplicits {

  implicit class IteratorOps[A](val iter: Iterator[A]) extends AnyVal {
    /**
      * A better version of <code>Iterator.copyToArray</code>,
      * that returns the number of actually read elements.
      *
      * @param xs    the array to fill.
      * @param start the starting index.
      * @param len   the maximal number of elements to copy.
      * @tparam B the type of the elements of the array.
      * @note Reuse: $consumesIterator
      */
    def fetchToArray[B >: A](xs: Array[B], start: Int, len: Int): Int = {
      require(start >= 0 && (start < xs.length || xs.length == 0), s"start $start out of range ${xs.length}")
      var i = start
      val end = start + math.min(len, xs.length - start)
      while (i < end && iter.hasNext) {
        xs(i) = iter.next()
        i += 1
      }
      i - start
    }
  }

  implicit class TraversableOnceOps[A, M[X] <: TraversableOnce[X]](val xs: M[A]) extends AnyVal {

    /**
      * Almost like `distinct`, but instead of comparing elements itself it compares their projections,
      * returned by a given function `f`.
      *
      * It's logically equivalent to doing stable grouping by `f` followed by selecting the first value of each key.
      *
      * @param f   projection function
      * @param cbf collection builder factory
      * @return a new sequence of elements which projection (obtained by applying the function `f`)
      *         are the first occurrence of every other elements' projection of this sequence.
      */
    def distinctBy[B](f: A => B)(implicit cbf: CanBuildFrom[M[A], A, M[A]]): M[A] = {
      val seen = mutable.Set.empty[B]
      val b = cbf(xs)
      for (x <- xs) {
        val y = f(x)
        if (!seen(y)) {
          b += x
          seen += y
        }
      }
      b.result()
    }
  }

  implicit class ArrayOps[A](val xs: Array[A]) extends AnyVal {
    /**
      * @see [[TraversableOnceOps.distinctBy]]
      */
    def distinctBy[B](f: A => B)(implicit ct: ClassTag[A]): Array[A] = (xs: Seq[A]).distinctBy(f).toArray
  }

}
