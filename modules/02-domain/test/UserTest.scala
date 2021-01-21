package services

import org.scalatestplus.play.PlaySpec
import domain._
import utils.TimeUtils

class UserTest extends PlaySpec {

  "User" should {

    "test sample on user" in {

      val u1 = new User(
        email    = new Email("jean@gmail.com"),
        password = "1234",
        birthday = Some(TimeUtils.now().minusYears(10))
      )

      u1.birthday.map(_ isBefore TimeUtils.now()).getOrElse(false) mustBe true
    }
  }
}
