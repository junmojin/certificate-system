package com.junmo.certificatesystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.junmo.certificatesystem.entity.CarrerCertificate;

public interface CarrerCertificateRepository extends JpaRepository<CarrerCertificate, Long> {

    @Query("""
            SELECT c FROM CarrerCertificate c
            JOIN c.applicant a
            WHERE a.userId = :userId
            ORDER BY c.createdAt DESC
            """)
    List<CarrerCertificate> findMyApplications(@Param("userId") String userId);

    @Query("""
            SELECT c FROM CarrerCertificate c
            JOIN c.applicant a
            WHERE c.id = :id AND a.userId = :userId
            """)
    Optional<CarrerCertificate> findByIdAndApplicantUserId(
            @Param("id") Long id,
            @Param("userId") String userId);

    @Query("""
            SELECT c FROM CarrerCertificate c
            JOIN FETCH c.applicant a
            ORDER BY a.userId ASC, c.createdAt DESC
            """)
    List<CarrerCertificate> findAllWithApplicant();

    @Query("""
            SELECT c FROM CarrerCertificate c
            JOIN FETCH c.applicant a
            WHERE a.userId = :userId
            ORDER BY c.createdAt DESC
            """)
    List<CarrerCertificate> findByApplicantUserId(@Param("userId") String userId);

    @Query("""
            SELECT c FROM CarrerCertificate c
            JOIN FETCH c.applicant a
            WHERE c.id = :id
            """)
    Optional<CarrerCertificate> findByIdWithApplicant(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT a.userId FROM CarrerCertificate c
            JOIN c.applicant a
            ORDER BY a.userId ASC
            """)
    List<String> findDistinctApplicantUserIds();
}
