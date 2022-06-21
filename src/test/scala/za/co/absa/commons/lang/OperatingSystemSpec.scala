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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class OperatingSystemSuite extends AnyFlatSpec with Matchers {

  "OperatingSystem util" should "correctly determine OS" in {
    OperatingSystem.getOsByOsName("Windows 10") shouldBe OperatingSystem.WINDOWS
    OperatingSystem.getOsByOsName("Linux") shouldBe OperatingSystem.LINUX
    OperatingSystem.getOsByOsName("Mac OS X") shouldBe OperatingSystem.MAC
    OperatingSystem.getOsByOsName("SunOs") shouldBe OperatingSystem.SOLARIS

    OperatingSystem.getOsByOsName("my own special os") shouldBe OperatingSystem.OTHER
  }
}
