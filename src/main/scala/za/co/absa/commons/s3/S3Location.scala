/*
 * Copyright 2018 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.commons.s3

import scala.util.matching.Regex

trait S3Location {
  def protocol: String
  def bucketName: String
  def path: String

  /**
   * Returns formatted S3 string, e.g. `s3a://myBucket/path/to/somewhere` that
   * [[za.co.absa.commons.s3.SimpleS3Location.SimpleS3LocationExt.toSimpleS3Location]] parses from
   *
   * @return formatted s3 string
   */
  def asSimpleS3LocationString: String = s"$protocol://$bucketName/$path"

}

object SimpleS3Location {

  /**
   * Generally usable regex for validating S3 path, e.g. `s3://my-cool-bucket1/path/to/file/on/s3.txt`
   * Protocols `s3`, `s3n`, and `s3a` are allowed.
   * Bucket naming rules defined at [[https://docs.aws.amazon.com/AmazonS3/latest/dev/BucketRestrictions.html#bucketnamingrules]] are instilled.
   */
  private val S3LocationRx: Regex = "^(s3[an]?)://([-a-z0-9.]{3,63})/(.*)$".r

  def apply(path: String): SimpleS3Location = {
    path.toSimpleS3Location.getOrElse(throw new IllegalArgumentException(s"Could not parse S3 location from $path!"))
  }

  implicit class SimpleS3LocationExt(val path: String) extends AnyVal {

    def toSimpleS3Location: Option[SimpleS3Location] = PartialFunction.condOpt(path) {
      case S3LocationRx(protocol, bucketName, relativePath) =>
        SimpleS3Location(protocol, bucketName, relativePath)
    }

    def isValidS3Path: Boolean = S3LocationRx.pattern.matcher(path).matches

    def withoutTrailSlash: SimpleS3Location = {
      val parsedS3Location = SimpleS3Location(path)

      if (parsedS3Location.path.endsWith("/")) parsedS3Location.copy(path=parsedS3Location.path.dropRight(1))
      else parsedS3Location
    }

    def withTrailSlash: SimpleS3Location = {
      val parsedS3Location = SimpleS3Location(path)

      if (parsedS3Location.path.endsWith("/")) parsedS3Location
      else {
        val lastPartOfS3Path = parsedS3Location.path.split("/").last
        if (lastPartOfS3Path.contains("."))
          throw new IllegalArgumentException(
            s"Could not add '/' into S3 location because it contains file location: ${parsedS3Location.path}"
          )

        parsedS3Location.copy(path=parsedS3Location.path + "/")
      }
    }
  }
}

case class SimpleS3Location(protocol: String, bucketName: String, path: String) extends S3Location
