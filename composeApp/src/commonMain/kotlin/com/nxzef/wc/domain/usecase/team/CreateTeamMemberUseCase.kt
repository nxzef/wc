package com.nxzef.wc.domain.usecase.team

import com.nxzef.wc.domain.repository.UserRepository

class CreateTeamMemberUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        role: String
    ) = repository.createMember(name, email, role)
}