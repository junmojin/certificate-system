package com.junmo.certificatesystem.service.certificate;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.junmo.certificatesystem.common.enums.CertificateStatus;
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

    private static final String DEFAULT_COMPANY_ADDRESS = "경기도 성남시 분당구 판교로 OOO번길 OO";
    private static final String DEFAULT_REPRESENTATIVE = "대표이사 O O O";
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
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CarrerCertificate> getMyApplications(String userId) {
        return carrerCertificateRepository.findMyApplications(userId);
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
                .companyAddress(DEFAULT_COMPANY_ADDRESS)
                .issuerName(certificate.getCompanyName())
                .representativeName(DEFAULT_REPRESENTATIVE)
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
