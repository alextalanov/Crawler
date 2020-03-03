package com.gmail.wristylotus.jobs

import java.net.URL

package object configuration {

  object implicits {

    implicit class ResourceOps(path: String) {

      def asResource: Option[URL] = Option(getClass.getClassLoader.getResource(path))
    }

  }

}
