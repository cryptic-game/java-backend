package net.cryptic_game.auth.model.jpa;

import de.m4rc3l.nova.jpa.model.TableModel;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
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
@Table(name = "user_oauth")
public class UserOAuth extends TableModel {

  @EmbeddedId
  private Id id;

  @Column(name = "provider_user_id", updatable = false, nullable = false)
  private String providerUserId;

  @Column(name = "created", updatable = false, nullable = false)
  private OffsetDateTime created;

  @Column(name = "last", nullable = false)
  private OffsetDateTime last;

  @Embeddable
  public static class Id implements Serializable {

    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    //    @Enumerated(EnumType.STRING)
    @Column(name = "provider_id", updatable = false, nullable = false)
    private String provider;
  }
}
