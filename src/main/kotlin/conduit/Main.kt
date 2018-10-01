package conduit

import conduit.handler.GetCurrentUser
import conduit.handler.Login
import conduit.handler.RegisterUser
import conduit.handler.UpdateCurrentUser
import conduit.repository.ConduitRepositoryImpl
import org.apache.logging.log4j.core.config.Configurator
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    Configurator.initialize(null, "log4j2-local.yaml")

    val logger = LoggerFactory.getLogger("main")

    val database = Database.connect("jdbc:h2:~/conduit", driver = "org.h2.Driver")
    val repository = ConduitRepositoryImpl(database)

    val app = Router(
        Login(repository),
        RegisterUser(repository),
        GetCurrentUser(repository),
        UpdateCurrentUser(repository)
    )()

    logger.info("Starting server...")

    app.asServer(Jetty(9000)).start().apply {
        logger.info("Server started on port 9000")
    }
}