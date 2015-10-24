package com.filler.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by xonda on 22/10/2015.
 */
@Controller
public class HomeController {
    @RequestMapping("/home")
    public String home() {
        return "index";
    }
    @RequestMapping("/")
    public String root() {
        return "redirect:home";
    }
}
