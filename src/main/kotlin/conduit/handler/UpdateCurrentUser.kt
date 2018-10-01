package conduit.handler

import conduit.model.UpdateUser
import conduit.model.extractEmail
import conduit.repository.ConduitRepository
import conduit.util.TokenAuth

class UpdateCurrentUser(val repository: ConduitRepository) : (TokenAuth.TokenInfo, UpdateUser) -> UserDto {
    override fun invoke(tokenInfo: TokenAuth.TokenInfo, updateUser: UpdateUser): UserDto {
        val email = tokenInfo.extractEmail()

        val user = repository.updateUser(email, updateUser)

        return UserDto(
            user.email,
            tokenInfo.token,
            user.username,
            user.bio,
            user.image
        )
    }
}