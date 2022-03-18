package net.cryptic_game.auth.domain.model;

import de.m4rc3l.nova.jpa.model.TableModel;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "auth_user_oauth")
public class UserOAuthModel extends TableModel {

  @EmbeddedId
  private Id id;

  @Column(name = "provider_user_id", updatable = false, nullable = false)
  private String providerUserId;

  @Column(name = "created", updatable = false, nullable = false)
  private OffsetDateTime created;

  @Column(name = "last", nullable = false)
  private OffsetDateTime last;

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof UserOAuthModel that)) {
      return false;
    }

    return Objects.equals(this.id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.id);
  }

  @Setter
  @Getter
  @Embeddable
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Id implements Serializable {

    @ManyToOne
    @JoinColumn(name = "user_id", updatable = false, nullable = false)
    private UserModel user;

    @Column(name = "provider_id", updatable = false, nullable = false)
    private String provider;

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }

      if (!(o instanceof Id id)) {
        return false;
      }

      return Objects.equals(this.user, id.user)
          && Objects.equals(this.provider, id.provider);
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.user, this.provider);
    }
  }
}
