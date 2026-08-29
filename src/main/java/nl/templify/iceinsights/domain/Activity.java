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

    @Column(name = "best_1_duration")
    private String best1Duration;

    @Column(name = "best_2_duration")
    private String best2Duration;

    @Column(name = "best_5_duration")
    private String best5Duration;

    @Column(name = "best_13_duration")
    private String best13Duration;

    @Column(name = "best_25_duration")
    private String best25Duration;
}
