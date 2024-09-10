package edu.goit.urlshortener.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.goit.urlshortener.security.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Setter
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@BatchSize(size = 10)
@Table(name = "links")
public class Url implements Serializable {
    @Serial
    private static final long serialVersionUID = 6527855645691638321L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "link_seq")
    @SequenceGenerator(name = "link_seq", sequenceName = "seq_link_id", allocationSize = 1)
    private Long id;

    @Column(name = "short_link")
    private String shortLink;

    @Column(name = "click_count", nullable = false)
    private Long clickCount;

    @Column(name = "native_link", nullable = false, columnDefinition = "TEXT")
    private String nativeLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "url_fk"))
    @JsonIgnore
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expired_time")
    private LocalDateTime expiredTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Url url)) return false;
        return id != null && id.equals(url.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
