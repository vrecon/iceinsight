package nl.templify.iceinsights.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "activity")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activity {
    @Id
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(nullable = false)
    private ZonedDateTime startTime;
    
    private ZonedDateTime endTime;
    
    @Column(nullable = true)
    private Long locationId;
    
    @Column(nullable = false)
    private Long chipId;

    @Column(name = "season_id")
    private Long seasonId;

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
}
