package com.example.chess.routes

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*
import com.example.chess.service.GameService
import com.example.chess.models.MoveRequest
import kotlinx.serialization.Serializable

fun Application.registerGameRoutes() {
    routing {
        post("/game/create") {
            val id = GameService.createGame()
            call.respond(mapOf("gameId" to id))
        }

        post("/game/{id}/join") {
            val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest, "No game id")
            val game = GameService.getGame(id) ?: return@post call.respond(HttpStatusCode.NotFound, "Game not found")
            val payload = call.receive<Map<String, String>>()
            val name = payload["name"] ?: "Player"
            val pl = game.join(name)
            call.respond(pl)
        }

        get("/game/{id}/state") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "No game id")
            val game = GameService.getGame(id) ?: return@get call.respond(HttpStatusCode.NotFound, "Game not found")
            call.respond(game.getStateDTO())
        }

        post("/game/{id}/move") {
            val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest, "No game id")
            val game = GameService.getGame(id) ?: return@post call.respond(HttpStatusCode.NotFound, "Game not found")
            val req = call.receive<MoveRequest>()
            val err = game.makeMove(req.playerId, req.from, req.to)
            if (err != null) call.respond(mapOf("error" to err)) else call.respond(mapOf("ok" to true))
        }
    }
}