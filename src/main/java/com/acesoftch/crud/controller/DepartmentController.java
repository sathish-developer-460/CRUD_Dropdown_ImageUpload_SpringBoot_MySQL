package com.acesoftch.crud.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.acesoftch.crud.model.Department;
import com.acesoftch.crud.repository.DepartmentRepository;

@Controller
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @GetMapping
    public String listDepartments(Model model) {
        model.addAttribute("departments", departmentRepository.findAll());
        return "departments/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("department", new Department());
        return "departments/create";
    }

    @PostMapping
    public String createDepartment(@ModelAttribute("department") Department department) {
        departmentRepository.save(department);
        return "redirect:/departments";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid department Id:" + id));
        model.addAttribute("department", department);
        return "departments/edit";
    }

    @PostMapping("/update/{id}")
    public String updateDepartment(@PathVariable("id") Long id, @ModelAttribute("department") Department department) {
        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid department Id:" + id));

        existing.setName(department.getName());

        departmentRepository.save(existing);
        return "redirect:/departments";
    }

    @GetMapping("/delete/{id}")
    public String deleteDepartment(@PathVariable("id") Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid department Id:" + id));
        departmentRepository.delete(department);
        return "redirect:/departments";
    }
}