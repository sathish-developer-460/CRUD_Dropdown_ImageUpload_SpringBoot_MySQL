package com.acesoftch.crud.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.acesoftch.crud.model.Department;
import com.acesoftch.crud.model.User;
import com.acesoftch.crud.repository.DepartmentRepository;
import com.acesoftch.crud.repository.UserRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    // @Autowired is not needed when there is only one constructor
    public UserController(UserRepository userRepository, DepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    @GetMapping({ "", "/" })
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        return "users/list";
    }

    // @GetMapping("/new")
    // public String showCreateForm(Model model) {
    // model.addAttribute("user", new User());
    // model.addAttribute("departments", departmentRepository.findAll());
    // // Pass departments to the view for the dropdown
    // return "users/create";
    // }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        User user = new User();
        user.setDepartment(new Department());
        model.addAttribute("user", user);
        model.addAttribute("departments", departmentRepository.findAll());
        return "users/create";
    }

    @PostMapping
    public String createUser(@ModelAttribute("user") User user,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {
        // Handle image upload logic here
        String imageUrl = saveImage(imageFile);
        if (imageUrl != null) {
            // Set the image URL in the user object
            // user.setImageUrl("/" + UPLOAD_DIR + fileName);
            user.setImageUrl(imageUrl); // Assuming you serve static files from /uploads/
        }
        userRepository.save(user);
        return "redirect:/users"; // redirect to the list of users after creation
    }

    // Edit form:- Existing users have no department yet, so handle null:
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        if (user.getDepartment() == null) {
            user.setDepartment(new Department());
        }
        model.addAttribute("user", user);
        model.addAttribute("departments", departmentRepository.findAll());
        return "users/edit";
    }

    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable("id") Long id, @ModelAttribute("user") User user,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

        existing.setFirstName(user.getFirstName());
        existing.setLastName(user.getLastName());
        existing.setEmail(user.getEmail());

        // only change the password if the user typed a new one
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existing.setPassword(user.getPassword());
        }

        // save the selected department
        if (user.getDepartment() != null && user.getDepartment().getId() != null) {
            Department dept = departmentRepository.findById(user.getDepartment().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid department"));
            existing.setDepartment(dept);
        }

        // only replace the image if a new file was chosen
        String imageUrl = saveImage(imageFile);
        if (imageUrl != null) {
            existing.setImageUrl(imageUrl);
        }

        userRepository.save(existing);
        return "redirect:/users";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userRepository.deleteById(id);
        return "redirect:/users";
    }

    // saves the uploaded file and returns its URL, or null if no file was chosen
    private String saveImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }
        try {
            // Ensure the upload directory exists
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            // Genereate a unique filename to avoid overwriting and save the file
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.write(filePath, imageFile.getBytes());
            return "/uploads/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error appropriately, maybe set a default image or return an error
            // message
            return null;
        }
    }
}