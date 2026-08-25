package nl.templify.iceinsights.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "session_stats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionStats {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "session_stats_seq")
    @SequenceGenerator(name = "session_stats_seq", sequenceName = "session_stats_seq", allocationSize = 1)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private Session session;
    
    @Column(name = "lap_count")
    private Integer lapCount;
    
    @Column(name = "fastest_time")
    private String fastestTime;
    
    @Column(name = "average_time")
    private String averageTime;
    
    @Column(name = "median_time")
    private String medianTime;
    
    @Column(name = "total_training_time")
    private String totalTrainingTime;
    
    @Column(name = "active_training_time")
    private String activeTrainingTime;
    
    @Column(name = "average_speed_kph")
    private BigDecimal averageSpeedKph;
    
    @Column(name = "average_speed_mps")
    private BigDecimal averageSpeedMps;
    
    @Column(name = "fastest_speed_kph")
    private BigDecimal fastestSpeedKph;
    
    @Column(name = "fastest_speed_mps")
    private BigDecimal fastestSpeedMps;
}
