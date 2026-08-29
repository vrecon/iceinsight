package nl.templify.iceinsights.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sessions")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;
    
    @Column(name = "chip_id", nullable = false)
    private Long chipId;
    
    @Column(name = "datetime_start", nullable = false)
    private ZonedDateTime datetimeStart;
    
    @Column(name = "best_lap_nr")
    private Integer bestLapNr;
    
    @Column(name = "best_lap_duration")
    private String bestLapDuration;
    
    @Column(name = "best_lap_speed_kph")
    private BigDecimal bestLapSpeedKph;
    
    @Column(name = "best_lap_speed_mps")
    private BigDecimal bestLapSpeedMps;
    
    @Column(name = "ave_lap_duration")
    private String aveLapDuration;
    
    @Column(name = "median_lap_duration")
    private String medianLapDuration;
    
    @Column(name = "duration")
    private String duration;
    
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lap> laps = new ArrayList<>();
    
    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private SessionStats stats;
}
