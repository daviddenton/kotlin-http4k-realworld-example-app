package conduit.endpoint

import conduit.Router
import conduit.handler.InvalidUserPassException
import conduit.handler.UserDto
import conduit.handler.UserNotFoundException
import conduit.model.Bio
import conduit.model.Email
import conduit.model.Token
import conduit.model.Username
import io.mockk.every
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AuthenticationEndpointTest {
    lateinit var router: Router

    @BeforeEach
    fun beforeEach() {
        router = getRouterToTest()
    }

    @Test
    fun `should return a User on successful login`() {
        every { router.login.invoke(any()) } returns UserDto(
            Email("jake@jake.jake"),
            Token("jwt.token.here"),
            Username("jake"),
            Bio("I work at statefarm"),
            null
        )

        @Language("JSON")
        val requestBody = """
            {
              "user":{
                "email": "jake@jake.jake",
                "password": "jakejake"
              }
            }
        """.trimIndent()
        val request = Request(Method.POST, "/api/users/login").body(requestBody)

        val resp = router()(request)

        @Language("JSON")
        val expectedResponseBody = """
            {
              "user": {
                "email": "jake@jake.jake",
                "token": "jwt.token.here",
                "username": "jake",
                "bio": "I work at statefarm",
                "image": null
              }
            }
        """.trimIndent()
        assertEquals(Status.OK, resp.status)
        resp.expectJsonResponse(expectedResponseBody)
    }

    @Test
    fun `should return 400 if email or password are not available in request body`() {
        @Language("JSON")
        val requestBody = """
            {
              "user":{
              }
            }
        """.trimIndent()
        val request = Request(Method.POST, "/api/users/login").body(requestBody)

        val resp = router()(request)


        assertEquals(Status.BAD_REQUEST, resp.status)
        resp.expectJsonResponse()
    }

    @Test
    fun `should return error when the user not found`() {
        every { router.login(any()) } throws UserNotFoundException("xxx")

        @Language("JSON")
        val requestBody = """
            {
              "user":{
                "email": "jake@jake.jake",
                "password": "jakejake"
              }
            }
        """.trimIndent()
        val request = Request(Method.POST, "/api/users/login").body(requestBody)

        val resp = router()(request)

        @Language("JSON")
        val expectedResponseBody = """
            {
              "errors": {
                "body": [
                  "User xxx not found."
                ]
              }
            }
        """.trimIndent()
        resp.expectJsonResponse(expectedResponseBody)
        assertEquals(Status.UNAUTHORIZED, resp.status)
    }

    @Test
    fun `should return unauthorized when the user or password is invalid`() {
        every { router.login(any()) } throws InvalidUserPassException()

        @Language("JSON")
        val requestBody = """
            {
              "user":{
                "email": "jake@jake.jake",
                "password": "jakejake"
              }
            }
        """.trimIndent()
        val request = Request(Method.POST, "/api/users/login").body(requestBody)

        val resp = router()(request)

        @Language("JSON")
        val expectedResponseBody = """
            {
              "errors": {
                "body": [
                  "Invalid username or password."
                ]
              }
            }
        """.trimIndent()
        resp.expectJsonResponse(expectedResponseBody)
        assertEquals(Status.UNAUTHORIZED, resp.status)
    }
}