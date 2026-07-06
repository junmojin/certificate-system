package com.junmo.certificatesystem.service.certificate;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.junmo.certificatesystem.common.enums.CertificateStatus;
import com.junmo.certificatesystem.dto.certificate.CertificateAdminDetailResponse;
import com.junmo.certificatesystem.dto.certificate.CertificateAdminSummaryResponse;
import com.junmo.certificatesystem.dto.certificate.CertificateApplyRequest;
import com.junmo.certificatesystem.dto.certificate.CertificateSummaryResponse;
import com.junmo.certificatesystem.dto.certificate.CertificateViewData;
import com.junmo.certificatesystem.entity.CarrerCertificate;
import com.junmo.certificatesystem.entity.User;
import com.junmo.certificatesystem.repository.CarrerCertificateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CarrerCertificateService {

    private static final DateTimeFormatter TABLE_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final CarrerCertificateRepository carrerCertificateRepository;
    private final CareerCertificatePdfService careerCertificatePdfService;

    @Transactional
    public CarrerCertificate apply(User applicant, CertificateApplyRequest request) {
        CarrerCertificate certificate = new CarrerCertificate();
        certificate.setApplicant(applicant);
        certificate.setCompanyName(request.getCompanyName());
        certificate.setPosition(request.getPosition());
        certificate.setPurpose(request.getPurpose());
        certificate.setStatus(CertificateStatus.PENDING);
        return carrerCertificateRepository.save(certificate);
    }

    @Transactional(readOnly = true)
    public CertificateViewData getOwnCertificateView(Long certificateId, String userId) {
        CarrerCertificate certificate = carrerCertificateRepository
                .findByIdAndApplicantUserId(certificateId, userId)
                .orElseThrow(() -> new IllegalArgumentException("증명서를 찾을 수 없습니다."));

        return toViewData(certificate);
    }

    @Transactional(readOnly = true)
    public byte[] generateOwnCertificatePdf(Long certificateId, String userId) throws IOException {
        CertificateViewData viewData = getOwnCertificateView(certificateId, userId);
        return careerCertificatePdfService.generate(viewData);
    }

    @Transactional(readOnly = true)
    public List<CertificateSummaryResponse> getMyApplicationSummaries(String userId) {
        return carrerCertificateRepository.findMyApplications(userId).stream()
                .map(c -> CertificateSummaryResponse.builder()
                        .id(c.getId())
                        .companyName(c.getCompanyName())
                        .position(c.getPosition())
                        .status(c.getStatus())
                        .statusLabel(c.getStatus().getLabel())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CarrerCertificate> getMyApplications(String userId) {
        return carrerCertificateRepository.findMyApplications(userId);
    }

    @Transactional(readOnly = true)
    public List<CertificateAdminSummaryResponse> getAdminApplicationSummaries(String userIdFilter) {
        List<CarrerCertificate> certificates = userIdFilter == null || userIdFilter.isBlank()
                ? carrerCertificateRepository.findAllWithApplicant()
                : carrerCertificateRepository.findByApplicantUserId(userIdFilter.trim());

        return certificates.stream()
                .map(this::toAdminSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getApplicantUserIds() {
        return carrerCertificateRepository.findDistinctApplicantUserIds();
    }

    @Transactional(readOnly = true)
    public CertificateAdminDetailResponse getAdminApplicationDetail(Long certificateId) {
        CarrerCertificate certificate = carrerCertificateRepository.findByIdWithApplicant(certificateId)
                .orElseThrow(() -> new IllegalArgumentException("증명서 신청을 찾을 수 없습니다."));
        return toAdminDetail(certificate);
    }

    @Transactional
    public void approveApplication(Long certificateId) {
        CarrerCertificate certificate = getCertificateForReview(certificateId);
        certificate.setStatus(CertificateStatus.APPROVED);
    }

    @Transactional
    public void rejectApplication(Long certificateId) {
        CarrerCertificate certificate = getCertificateForReview(certificateId);
        certificate.setStatus(CertificateStatus.REJECTED);
    }

    private CarrerCertificate getCertificateForReview(Long certificateId) {
        CarrerCertificate certificate = carrerCertificateRepository.findByIdWithApplicant(certificateId)
                .orElseThrow(() -> new IllegalArgumentException("증명서 신청을 찾을 수 없습니다."));

        if (certificate.getStatus() != CertificateStatus.PENDING) {
            throw new IllegalArgumentException("승인 대기 상태의 신청만 처리할 수 있습니다.");
        }
        return certificate;
    }

    private CertificateAdminSummaryResponse toAdminSummary(CarrerCertificate certificate) {
        User applicant = certificate.getApplicant();
        return CertificateAdminSummaryResponse.builder()
                .id(certificate.getId())
                .applicantUserId(applicant.getUserId())
                .applicantName(applicant.getName())
                .companyName(certificate.getCompanyName())
                .position(certificate.getPosition())
                .purpose(certificate.getPurpose())
                .status(certificate.getStatus())
                .statusLabel(certificate.getStatus().getLabel())
                .appliedAt(certificate.getCreatedAt())
                .build();
    }

    private CertificateAdminDetailResponse toAdminDetail(CarrerCertificate certificate) {
        User applicant = certificate.getApplicant();
        CertificateStatus status = certificate.getStatus();

        return CertificateAdminDetailResponse.builder()
                .id(certificate.getId())
                .applicantUserId(applicant.getUserId())
                .applicantName(applicant.getName())
                .hireDate(applicant.getHireDate())
                .companyName(certificate.getCompanyName())
                .position(certificate.getPosition())
                .purpose(certificate.getPurpose())
                .status(status)
                .statusLabel(status.getLabel())
                .appliedAt(certificate.getCreatedAt())
                .updatedAt(certificate.getUpdatedAt())
                .canReview(status == CertificateStatus.PENDING)
                .build();
    }

    private CertificateViewData toViewData(CarrerCertificate certificate) {
        User applicant = certificate.getApplicant();
        String documentNo = "CERT-" + certificate.getId() + "-"
                + applicant.getUserId().toUpperCase();

        return CertificateViewData.builder()
                .certificateId(certificate.getId())
                .documentNo(documentNo)
                .userId(applicant.getUserId())
                .name(applicant.getName())
                .birthDateDisplay("-")
                .department("-")
                .companyName(certificate.getCompanyName())
                .position(certificate.getPosition())
                .hireDate(applicant.getHireDate())
                .formattedWorkStartDate(formatTableDate(applicant.getHireDate()))
                .workEndDateDisplay("재직중")
                .workPeriod(formatWorkPeriod(applicant.getHireDate()))
                .employmentStatus("재직중")
                .purpose(certificate.getPurpose())
                .assignedTask("")
                .remarks("")
                .status(certificate.getStatus())
                .issueDate(LocalDate.now())
                .appliedAt(certificate.getCreatedAt())
                .issuerName(certificate.getCompanyName())
                .build();
    }

    public String formatDocumentDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
    }

    public String formatTableDate(LocalDate date) {
        return date.format(TABLE_DATE);
    }

    private String formatWorkPeriod(LocalDate hireDate) {
        Period period = Period.between(hireDate, LocalDate.now());
        return String.format("%d년 %d개월 %d일", period.getYears(), period.getMonths(), period.getDays());
    }
}
