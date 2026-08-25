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
}