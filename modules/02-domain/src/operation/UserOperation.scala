package operation

import domain.User

import security.HashUtils

object UserOperation {

  def update_password(user: User, new_password: String): User = {
    user.copy(
      password = HashUtils.createPassword(user.password)
    )
  }

  def check_password(user: User, candidate: String): Boolean = {
    HashUtils.checkPassword(candidate, user.password)
  }
}
