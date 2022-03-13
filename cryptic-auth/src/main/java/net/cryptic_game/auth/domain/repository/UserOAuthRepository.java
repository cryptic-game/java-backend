package net.cryptic_game.auth.domain.repository;

import net.cryptic_game.auth.domain.model.UserOAuthModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOAuthRepository extends JpaRepository<UserOAuthModel, UserOAuthModel.Id> {
}