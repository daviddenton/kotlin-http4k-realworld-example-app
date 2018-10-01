package conduit.handler

import conduit.model.*
import conduit.repository.ConduitRepository
import conduit.util.HttpException
import conduit.util.generateToken
import conduit.util.hash
import org.http4k.core.Status

class Login(val repository: ConduitRepository) : (LoginUserDto) -> UserDto {
    override operator fun invoke(loginUserDto: LoginUserDto): UserDto {
        val user =
            repository.findUserByEmail(loginUserDto.email) ?: throw UserNotFoundException(loginUserDto.email.value)

        if (loginUserDto.password.hash() != user.password) throw InvalidUserPassException()

        val token = generateToken(user.username, user.email)

        return UserDto(
            user.email,
            token,
            user.username,
            user.bio,
            user.image
        )
    }
}

class UserNotFoundException(usernameOrEmail: String) :
    HttpException(Status.UNAUTHORIZED, "User $usernameOrEmail not found.")

class InvalidUserPassException : HttpException(Status.UNAUTHORIZED, "Invalid username or password.")

data class LoginUserDto(val email: Email, val password: Password)
data class UserDto(val email: Email, val token: Token, val username: Username, val bio: Bio?, val image: Image?)
