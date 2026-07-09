package com.kmarine.fishing.reservation;

import com.kmarine.fishing.common.EncryptionConverter;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reservation_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    private String name;        // 탑승자 이름
    private String phone;       // 탑승자 연락처
    
    @Convert(converter = EncryptionConverter.class)
    private String idNumber;    // 생년월일 암호화 저장

    public static ReservationMember create(Reservation reservation,
                                           String name, String phone,
                                           String idNumber) {
        ReservationMember m = new ReservationMember();
        m.reservation = reservation;
        m.name        = name;
        m.phone       = phone;
        m.idNumber    = idNumber;
        return m;
    }
}