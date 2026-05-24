package com.dbp.democarpultec.config.swagger;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerRedirectController {

    @GetMapping("/api/docs/")
    public String redirectSwaggerDocs() {
        return "redirect:/api/docs";
    }
}