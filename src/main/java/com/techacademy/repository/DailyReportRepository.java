package com.techacademy.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;

public interface DailyReportRepository extends JpaRepository<Report, Integer> {
    List<Report> findByEmployeeAndDeleteFlgFalse(Employee employee);
    
    boolean existsByEmployeeAndReportDateAndDeleteFlgFalse(Employee employee, LocalDate reportDate);
    
    boolean existsByEmployeeAndReportDateAndIdNotAndDeleteFlgFalse(Employee employee, LocalDate reportDate, int id);
    
}