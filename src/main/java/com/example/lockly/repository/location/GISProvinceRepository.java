package com.example.lockly.repository.location;

import com.example.lockly.domain.dto.query.ProvinceInfo;
import com.example.lockly.domain.dto.query.WardInfo;
import com.example.lockly.domain.entity.location.GISProvince;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GISProvinceRepository extends JpaRepository<GISProvince, Integer> {
    @Query(value = """
    SELECT
        p.code AS code,
        p.name AS name,
        p.full_name AS fullName
    FROM gis_provinces gp
    JOIN provinces p
      ON p.code = gp.province_code
    WHERE gp.geom && ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)
      AND ST_Covers(gp.geom, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326))
    LIMIT 1
    """, nativeQuery = true)
    Optional<ProvinceInfo> findProvinceByLocation(
            @Param("lat") double lat,
            @Param("lng") double lng
    );

    @Query(value = """
    SELECT
        w.code AS code,
        w.name AS name,
        w.full_name AS fullName
    FROM gis_wards gw
    JOIN wards w
      ON w.code = gw.ward_code
    WHERE gw.geom && ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)
      AND ST_Covers(gw.geom, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326))
    LIMIT 1
    """, nativeQuery = true)
    Optional<WardInfo> findWardByLocation(
            @Param("lat") double lat,
            @Param("lng") double lng
    );
}