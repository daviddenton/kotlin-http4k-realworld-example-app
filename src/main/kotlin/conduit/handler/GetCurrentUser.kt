package conduit.handler

import conduit.model.extractEmail
import conduit.repository.ConduitRepository
import conduit.util.TokenAuth

class GetCurrentUser(val repository: ConduitRepository) : (TokenAuth.TokenInfo) -> UserDto {
    override fun invoke(tokenInfo: TokenAuth.TokenInfo): UserDto {
        val email = tokenInfo.extractEmail()
        return repository.findUserByEmail(email)
            ?.let {
                UserDto(
                    it.email,
                    tokenInfo.token,
                    it.username,
                    it.bio,
                    it.image
                )
            }
            ?: throw UserNotFoundException(email.value)
    }
}