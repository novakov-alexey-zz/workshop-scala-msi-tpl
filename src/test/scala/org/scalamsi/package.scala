package org

import org.http4s.Uri

package object scalamsi {

  def serviceUri(endpoint: Option[String] = None): Uri = {
    val str = Module.apiPrefix + endpoint.getOrElse("")
    Uri.fromString(str).toOption.getOrElse(sys.error(s"Wrong uri: $str"))
  }
}
