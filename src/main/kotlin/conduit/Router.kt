package conduit

import conduit.handler.LoginUserDto
import conduit.handler.NewUserDto
import conduit.handler.UserDto
import conduit.model.UpdateUser
import conduit.util.CatchHttpExceptions
import conduit.util.TokenAuth
import conduit.util.createErrorResponse
import org.http4k.core.*
import org.http4k.filter.ServerFilters
import org.http4k.format.Jackson.auto
import org.http4k.lens.Failure
import org.http4k.lens.RequestContextKey
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes


class Router(
    val login: (LoginUserDto) -> UserDto,
    val register: (NewUserDto) -> UserDto,
    val getCurrentUser: (TokenAuth.TokenInfo) -> UserDto,
    val updateCurrentUser: (TokenAuth.TokenInfo, UpdateUser) -> UserDto
) {
    private val contexts = RequestContexts()
    private val tokenInfoKey = RequestContextKey.required<TokenAuth.TokenInfo>(contexts)

    operator fun invoke(): RoutingHttpHandler =
        CatchHttpExceptions()
            .then(ServerFilters.CatchLensFailure {
                createErrorResponse(Status.BAD_REQUEST, it.failures.map(Failure::toString))
            })
            .then(ServerFilters.InitialiseRequestContext(contexts))
            .then(
                routes(
                    "/api/users" bind routes(
                        "/login" bind Method.POST to login(),
                        "/" bind routes(
                            Method.GET to current(),
                            Method.POST to registerUser(),
                            Method.PUT to update()
                        )
                    )
                )
            )

    private fun update(): HttpHandler = TokenAuth(tokenInfoKey).then {
        val tokenInfo = tokenInfoKey(it)
        val updateUser = updateLens(it).user
        val result = updateCurrentUser(tokenInfo, updateUser)
        userLens(UserResponse(result), Response(Status.OK))
    }

    private fun registerUser(): HttpHandler = {
        val result = register(registerLens(it).user)
        userLens(UserResponse(result), Response(Status.CREATED))
    }

    private fun current(): HttpHandler = TokenAuth(tokenInfoKey).then {
        val tokenInfo = tokenInfoKey(it)
        val result = getCurrentUser(tokenInfo)
        userLens(UserResponse(result), Response(Status.OK))
    }

    private fun login(): HttpHandler = {
        val result = login(loginLens(it).user)
        userLens(UserResponse(result), Response(Status.OK))
    }

    private val loginLens = Body.auto<LoginUserRequest>().toLens()
    private val userLens = Body.auto<UserResponse>().toLens()
    private val registerLens = Body.auto<NewUserRequest>().toLens()
    private val updateLens = Body.auto<UpdateUserRequest>().toLens()
}

data class LoginUserRequest(val user: LoginUserDto)

data class UserResponse(val user: UserDto)

data class NewUserRequest(val user: NewUserDto)

data class UpdateUserRequest(val user: UpdateUser)