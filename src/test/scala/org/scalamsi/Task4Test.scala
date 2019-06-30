package org.scalamsi

import org.scalatest.FlatSpec

class Task4Test extends FlatSpec {

  it should "not load invalid jdbc config" in {
    AppConfig.load("src/test/resources/invalid-application.conf") match {
      case Left(_) => () // test passed
      case Right(_) => fail("wrong validation should not be loaded")
    }
  }

  it should "load valid jdbc config" in {
    AppConfig.load("src/test/resources/valid-application.conf") match {
      case Right(_) => () // test passed
      case Left(_) => fail("valid validation should be loaded")
    }
  }
}
