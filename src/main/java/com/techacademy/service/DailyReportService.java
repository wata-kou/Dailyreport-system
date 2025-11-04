package com.techacademy.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.DailyReportRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DailyReportService {

    private final DailyReportRepository dailyReportRepository;

    public DailyReportService(DailyReportRepository dailyReportRepository, PasswordEncoder passwordEncoder) {
        this.dailyReportRepository = dailyReportRepository;
    }

    // 日報保存
    @Transactional
    public ErrorKinds save(Report report) {

        report.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        dailyReportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    // 日報削除
    @Transactional
    public ErrorKinds delete(int id, UserDetail userDetail) {

        Report report = findById(id);
        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);
        report.setDeleteFlg(true);

        return ErrorKinds.SUCCESS;
    }

    // 日報一覧表示処理
    public List<Report> findAll() {
        return dailyReportRepository.findAll();
    }

    // 1件を検索
    public Report findById(int id) {
        // findByIdで検索
        Optional<Report> option = dailyReportRepository.findById(id);
        // 取得できなかった場合はnullを返す
        Report report = option.orElse(null);
        return report;
    }

    
 // 従業員更新
    @Transactional
    public ErrorKinds update(Report report) {

        // DB上の既存データを取得
        Report dbReport = findById(report.getId());

        // 変更項目を上書き
        dbReport.setReportDate(report.getReportDate());
        dbReport.setTitle(report.getTitle());
        dbReport.setContent(report.getContent());
        dbReport.setUpdatedAt(LocalDateTime.now());

        // 論理削除フラグはそのまま維持
        dbReport.setDeleteFlg(dbReport.isDeleteFlg());

        dailyReportRepository.save(dbReport);
        return ErrorKinds.SUCCESS;
    }
    
    // ★ 従業員に紐づく日報一覧を取得（for ループ用）
    public List<Report> findByEmployee(Employee employee) {
        return dailyReportRepository.findByEmployeeAndDeleteFlgFalse(employee);
    }
    // 新規作成・更新時に日付の重複がないか確認
    public boolean existsSameDate(Employee employee, LocalDate date) {
        return dailyReportRepository.existsByEmployeeAndReportDateAndDeleteFlgFalse(employee, date);
    }
    public boolean existsSameDateExceptId(Employee employee, LocalDate date, int id) {
        return dailyReportRepository.existsByEmployeeAndReportDateAndIdNotAndDeleteFlgFalse(employee, date, id);
    }


}
