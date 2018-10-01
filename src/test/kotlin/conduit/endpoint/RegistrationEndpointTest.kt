package conduit.endpoint

import conduit.Router
import conduit.handler.UserAlreadyExistsException
import conduit.handler.UserDto
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

class RegistrationEndpointTest {
    lateinit var router: Router

    @BeforeEach
    fun beforeEach() {
        router = getRouterToTest()
    }

    @Test
    fun `should return User on successful registration`() {
        every { router.register(any()) } returns UserDto(
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
                "username": "Jacob",
                "email": "jake@jake.jake",
                "password": "jakejake"
              }
            }
        """.trimIndent()
        val request = Request(Method.POST, "/api/users").body(requestBody)

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
        assertEquals(Status.CREATED, resp.status)
        resp.expectJsonResponse(expectedResponseBody)
    }

    @Test
    fun `should return CONFLICT if user already exist`() {
        every { router.register(any()) } throws UserAlreadyExistsException()

        @Language("JSON")
        val requestBody = """
            {
              "user":{
                "username": "Jacob",
                "email": "jake@jake.jake",
                "password": "jakejake"
              }
            }
        """.trimIndent()
        val request = Request(Method.POST, "/api/users").body(requestBody)

        val resp = router()(request)

        @Language("JSON")
        val expectedResponseBody = """
            {
              "errors": {
                "body": [
                  "The specified user already exists."
                ]
              }
            }
        """.trimIndent()
        resp.expectJsonResponse(expectedResponseBody)
        assertEquals(Status.CONFLICT, resp.status)
    }
}