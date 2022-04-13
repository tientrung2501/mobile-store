package com.hcmute.mobilestore.controllers;

import com.hcmute.mobilestore.models.Account;
import com.hcmute.mobilestore.models.User;
import com.hcmute.mobilestore.repository.AccountRepository;
import com.hcmute.mobilestore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
@RequestMapping(path = "/Account")
public class AccountController {
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    UserRepository userRepository;

//    Phần đăng nhập
    @GetMapping(value = "/Login")
    public String login(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if ((boolean) session.getAttribute("auth")) {
            return "viewHome/Index";
        } else return "viewAccount/Login";
    }

    @PostMapping(value = "/Login")
    public String login(ModelMap modelMap, HttpServletRequest request) {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        Account account = accountRepository.findByUsername(email);
        if (account != null) {
            if (account.getPassword().equals(password)) {
                HttpSession session = request.getSession();
                session.setAttribute("auth", true);
                session.setAttribute("authUser", userRepository.findById(account.getId()).get());
                session.setAttribute("role", account.getRole());

                return "redirect:/Home";
            } else {
                modelMap.addAttribute("hasError", true);
                modelMap.addAttribute("errorMessage", "Đăng nhập không thành công.");
                return "viewAccount/Login";
            }
        } else {
            modelMap.addAttribute("hasError", true);
            modelMap.addAttribute("errorMessage", "Đăng nhập không thành công.");
            return "viewAccount/Login";
        }
    }

//    Phần đăng ký
    @GetMapping(value = "/Register")
    public String register(HttpServletRequest request) {
         return "viewAccount/Register";
    }

    @PostMapping(value = "/Register")
    public String register(ModelMap modelMap, HttpServletRequest request) {
        String rawpwd = request.getParameter("rawpwd");
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String address = request.getParameter("address");
        String phone_number = request.getParameter("phone");
        int role = 2;
        try {
            User newUser = new User(name, email, address,  phone_number);
            userRepository.save(newUser);
            Account newAccount = new Account(userRepository.findByEmail(email).getId(), email, rawpwd, role);
            accountRepository.save(newAccount);
            modelMap.addAttribute("hasNotify", true);
            modelMap.addAttribute("Message", "Đăng ký thành công!!!");
            return "viewAccount/Register";
        } catch (Exception e) {
            modelMap.addAttribute("hasNotify", true);
            modelMap.addAttribute("Message", e);
            return "viewAccount/Register";
        }
    }

    //    Kiểm tra email đã tồn tại chưa
    @GetMapping(value = "/IsAvailable")
    public void emailChecking(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("email");
        Account account = accountRepository.findByUsername(username);
        boolean isAvailable = (account == null);

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        // Send JSON data. if email exists JSON data will be true.
        out.print(isAvailable);
        out.flush();
    }

    //    Phần đăng xuất
    @PostMapping(value = "/Logout")
    public String logout(ModelMap modelMap, HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("auth", false);
        session.setAttribute("authUser", new User());
        return "redirect:/Home";
    }
}