package net.cryptic_game.auth.domain.repository;

import java.util.Optional;
import java.util.UUID;
import net.cryptic_game.auth.domain.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserModel, UUID> {

  @Query("select uOAuth.id.user from UserOAuthModel uOAuth where uOAuth.id.provider = ?1 and uOAuth.providerUserId = ?2")
  Optional<UserModel> findByProviderIdAndProviderUserId(String providerId, String ProviderUserId);

  @Query("select count(user) = 0 from UserModel user where lower(user.name) = lower(?1)")
  boolean isUsernameAvailable(String name);
}
