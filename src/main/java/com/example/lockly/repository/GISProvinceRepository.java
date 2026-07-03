package com.example.lockly.repository;

import com.example.lockly.domain.entity.locationEntity.GISProvince;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GISProvinceRepository extends JpaRepository<GISProvince, Integer> {

    @Query(value = """
    SELECT province_code
    FROM gis_provinces
    WHERE ST_Contains(
        geom,
        ST_SRID(POINT(:lng, :lat), 4326)
    )
    LIMIT 1
    """, nativeQuery = true)
    Optional<String> findProvinceCodeByLocation(
            @Param("lat") double lat,
            @Param("lng") double lng
    );

    @Query(value = """
        SELECT p.name
        FROM provinces p
        WHERE p.code = :provinceCode
        """, nativeQuery = true)
    Optional<String> findProvinceNameByCode(@Param("provinceCode") String provinceCode);

    @Query(value = """
        SELECT p.full_name
        FROM provinces p
        WHERE p.code = :provinceCode
        """, nativeQuery = true)
    Optional<String> findProvinceFullNameByCode(@Param("provinceCode") String provinceCode);
}