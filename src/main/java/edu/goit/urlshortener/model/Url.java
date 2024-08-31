package edu.goit.urlshortener.model;

import edu.goit.urlshortener.security.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Builder @Getter @Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "urls")
public class Url {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "urls_seq")
    @SequenceGenerator(name = "urls_seq", sequenceName = "seq_url_id", allocationSize = 1)
    private Long id;

    @Column(name = "short_link")
    private String slug;

    @Column(name = "transactions_count", nullable = false)
    private Long clickCount;

    @Column(name = "native_link", nullable = false, columnDefinition = "TEXT")
    private String destination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "url_fk"))
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
