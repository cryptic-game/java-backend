package net.cryptic_game.auth.domain.converter;

import de.m4rc3l.nova.core.Converter;
import net.cryptic_game.auth.domain.model.UserModel;
import net.cryptic_game.common.domain.auth.User;
import org.springframework.stereotype.Component;

@Component
public class UserModelConverter implements Converter<UserModel, User> {

  @Override
  public UserModel toModel(final User dto) {
    return new UserModel(dto.name(), dto.created(), dto.last());
  }

  @Override
  public User toDto(final UserModel model) {
    return new User(model.getId(), model.getName(), model.getCreated(), model.getLast());
  }

  @Override
  public void override(final UserModel model, final User dto) {
    model.setName(dto.name());
    model.setLast(model.getLast());
  }

  @Override
  public void merge(final UserModel model, final User dto) {
    if (dto.name() != null) model.setName(dto.name());
    if (dto.last() != null) model.setLast(dto.last());
  }
}
