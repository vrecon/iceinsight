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

    @Column(name = "best_1_duration")
    private String best1Duration;

    @Column(name = "best_2_duration")
    private String best2Duration;

    @Column(name = "best_4_duration")
    private String best4Duration;

    @Column(name = "best_5_duration")
    private String best5Duration;

    @Column(name = "best_8_duration")
    private String best8Duration;

    @Column(name = "best_13_duration")
    private String best13Duration;

    @Column(name = "best_25_duration")
    private String best25Duration;

    @Column(name = "best_50_duration")
    private String best50Duration;

    @Column(name = "best_100_duration")
    private String best100Duration;

    @Column(name = "moving_avg_window")
    private Integer movingAvgWindow;
}
