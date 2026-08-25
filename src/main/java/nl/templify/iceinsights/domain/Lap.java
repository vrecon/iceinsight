package nl.templify.iceinsights.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Table(name = "laps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lap {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lap_seq")
    @SequenceGenerator(name = "lap_seq", sequenceName = "lap_seq", allocationSize = 1)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;
    
    @Column(name = "lap_nr", nullable = false)
    private Integer lapNr;
    
    @Column(name = "datetime_start", nullable = false)
    private ZonedDateTime datetimeStart;
    
    @Column(nullable = false)
    private String duration;
    
    @Column(name = "speed_kph")
    private BigDecimal speedKph;
    
    @Column(name = "speed_mps")
    private BigDecimal speedMps;
    
    @Column(name = "diff_prev_lap")
    private String diffPrevLap;
    
    @Column(name = "session_duration")
    private String sessionDuration;
    
    @Column
    @Enumerated(EnumType.STRING)
    private LapStatus status;
}