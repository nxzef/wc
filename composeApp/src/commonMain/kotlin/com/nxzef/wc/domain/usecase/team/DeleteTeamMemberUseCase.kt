package com.nxzef.wc.domain.usecase.team

import com.nxzef.wc.domain.repository.UserRepository

class DeleteTeamMemberUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(id: String) = repository.removeMember(id)
}