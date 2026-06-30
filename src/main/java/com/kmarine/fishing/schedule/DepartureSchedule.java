package com.kmarine.fishing.schedule;

import com.kmarine.fishing.vessel.Vessel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "departure_schedules",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"vessel_id", "schedule_date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class DepartureSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vessel_id", nullable = false)
    private Vessel vessel;

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    @Column(nullable = false)
    private boolean available = true;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static DepartureSchedule create(Vessel vessel,
                                           LocalDate scheduleDate,
                                           boolean available) {
        DepartureSchedule schedule = new DepartureSchedule();
        schedule.vessel       = vessel;
        schedule.scheduleDate = scheduleDate;
        schedule.available    = available;
        return schedule;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
