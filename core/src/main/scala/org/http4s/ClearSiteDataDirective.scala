/*
 * Copyright 2013 http4s.org
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

package org.http4s

import cats._
import cats.syntax.all._
import org.http4s.util._

sealed trait ClearSiteDataDirective extends Product with Serializable with Renderable {
  def value: String

  override def render(writer: Writer): writer.type = writer.append(value)

  // Final //

  override final def toString: String =
    this.show
}

object ClearSiteDataDirective {

  case object cache extends ClearSiteDataDirective {
    override val value: String = "cache"
  }

  case object cookies extends ClearSiteDataDirective {
    override val value: String = "cookies"
  }

  case object storage extends ClearSiteDataDirective {
    override val value: String = "storage"
  }

  case object executionContexts extends ClearSiteDataDirective {
    override val value: String = "executionContexts"
  }

  case object wildcard extends ClearSiteDataDirective {
    override val value: String = "*"
  }

  implicit val catsInstancesForClearSiteDataDirective: Hash[ClearSiteDataDirective] with Order[ClearSiteDataDirective] with Show[ClearSiteDataDirective] =
    new Hash[ClearSiteDataDirective] with Order[ClearSiteDataDirective] with Show[ClearSiteDataDirective] {
      override def hash(x: ClearSiteDataDirective): Int = x.hashCode

      override def compare(x: ClearSiteDataDirective, y: ClearSiteDataDirective): Int =
        x.value.compare(y.value)

      override def show(t: ClearSiteDataDirective): String =
        show"ClearSiteDataDirective(value = ${t.value})"
    }
}
