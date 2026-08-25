package nl.templify.iceinsights.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "chip")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chip {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chip_seq")
    @SequenceGenerator(name = "chip_seq", sequenceName = "chip_seq", allocationSize = 1)
    private Long id;

    @Column(name = "chip_code", nullable = false, unique = true, length = 50)
    private String chipCode;

    @Column(name = "chip_label")
    private String chipLabel;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ChipStatus status;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany(mappedBy = "chips")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<User> users = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}