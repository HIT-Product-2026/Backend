package com.example.lockly.domain.entity.location;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.MultiPolygon;

@Entity
@Table(name = "gis_wards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GISWard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ward_code", length = 20, nullable = false)
    private String wardCode;

    @Column(name = "geom")
    private MultiPolygon geom;
}