package net.cryptic_game.auth.model.jpa;

import de.m4rc3l.nova.jpa.model.TableModelAutoId;
import java.time.OffsetDateTime;
import javax.persistence.Column;
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
@Table(name = "user")
public class User extends TableModelAutoId {

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "created", updatable = false, nullable = false)
  private OffsetDateTime created;

  @Column(name = "last", nullable = false)
  private OffsetDateTime last;
}
