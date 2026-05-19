/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.controllers;

import com.evercare.pojo.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author cadic
 */
@Controller
@ControllerAdvice
public class HomeController {
  
    @RequestMapping("/")
    public String index() {
        return "index";
    }
    

}
