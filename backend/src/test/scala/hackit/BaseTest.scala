package hackit

class BaseTest extends org.specs2.mutable.Specification {
  "Test" >> {
    "check something" >> {
      1 + 1 should_== 2
    }
  }
}
