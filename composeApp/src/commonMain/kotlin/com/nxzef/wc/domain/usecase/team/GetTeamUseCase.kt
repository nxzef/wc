package com.nxzef.wc.domain.usecase.team

import com.nxzef.wc.domain.repository.UserRepository

class GetTeamUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke() = repository.getTeam()
}