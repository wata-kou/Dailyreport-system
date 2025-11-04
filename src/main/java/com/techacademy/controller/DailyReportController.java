package com.techacademy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Report;
import com.techacademy.service.DailyReportService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("/reports")
public class DailyReportController {

    private final DailyReportService dailyReportService;

    @Autowired
    public DailyReportController(DailyReportService dailyReportService) {
        this.dailyReportService = dailyReportService;
    }

    // 日報一覧画面
    @GetMapping
    public String list(Model model) {

        model.addAttribute("listSize", dailyReportService.findAll().size());
        model.addAttribute("reportList", dailyReportService.findAll());

        return "reports/list";
    }

    // 日報詳細画面
    @GetMapping(value = "/{id}/")
    public String detail(@PathVariable("id") int id, Model model) {

        model.addAttribute("report", dailyReportService.findById(id));
        return "reports/detail";
    }
    
    // 日報更新画面
    @GetMapping(value = "/{id}/update")
    public String edit(@PathVariable("id") int id, Model model) {
        
        model.addAttribute("report", dailyReportService.findById(id));
        return "reports/update";
    }
    
    // 日報更新処理
    @PostMapping(value = "/{id}/update")
    public String update(@PathVariable("id") int id,@Validated @ModelAttribute("report") Report report, BindingResult res, @AuthenticationPrincipal UserDetail userDetail,Model model) {

        // 入力チェック
        if (res.hasErrors()) {
            Report existing = dailyReportService.findById(id);
            if (existing != null) {
                report.setEmployee(existing.getEmployee());
            }
            model.addAttribute("loginUser", userDetail.getEmployee());
            return "reports/update";
        }
        
        report.setId(id);
        
        // ★ 同じ日付の日報が既に存在するかチェック
        if (dailyReportService.existsSameDateExceptId(report.getEmployee(), report.getReportDate(), id)) {
            Report existing = dailyReportService.findById(id);
            if (existing != null) {
                report.setEmployee(existing.getEmployee());
            }
            res.rejectValue("reportDate", "report.date.duplicate", "既に登録されている日付です");
            model.addAttribute("loginUser", userDetail.getEmployee());
            return "reports/update";
        }
        
        // DBに保存
        dailyReportService.update(report);

        return "redirect:/reports";
    }

    // 日報新規登録画面
    @GetMapping(value = "/add")
    public String create(@AuthenticationPrincipal UserDetail userDetail, Model model, @ModelAttribute Report report) {

        model.addAttribute("report", report);
        model.addAttribute("loginUser", userDetail.getEmployee());
        return "reports/new";
    }

    // 日報新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Report report, BindingResult res, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        
        // 入力チェック
        if (res.hasErrors()) {
            model.addAttribute("loginUser", userDetail.getEmployee());
            return "reports/new";
        }
        
        report.setEmployee(userDetail.getEmployee());
        
        // ★ 同じ日付の日報が既に存在するかチェック
        if (dailyReportService.existsSameDate(report.getEmployee(), report.getReportDate())) {
            res.rejectValue("reportDate", "duplicate", "既に登録されている日付です");
            model.addAttribute("loginUser", userDetail.getEmployee());
            return "reports/new";
        }
        
        // DBに保存
        dailyReportService.save(report);

        return "redirect:/reports";
    }

    // 日報削除処理
    @PostMapping(value = "/{id}/delete")
    public String delete(@PathVariable("id") int id, @AuthenticationPrincipal UserDetail userDetail, Model model) {

        ErrorKinds result = dailyReportService.delete(id, userDetail);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("report", dailyReportService.findById(id));
            return detail(id, model);
        }

        return "redirect:/reports";
    }

}
