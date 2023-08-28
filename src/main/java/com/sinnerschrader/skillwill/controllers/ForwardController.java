package com.sinnerschrader.skillwill.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;

/**
 * Forward / to /swagger-ui.html
 *
 * @author torree
 */
@Controller
@Scope("prototype")
public class ForwardController {

//	@Operation(summary = "forward to swagger", description = "forward to swagger")
//    @GetMapping("/swagger")
//    public String forwardSwagger() {
//        return "forward:/swagger-ui.html";
//    }
//	
//
//    @Operation(summary = "forward frontend to index", description = "forward routes handled by react-router to index")
//    @GetMapping({"/my-profile/", "/profile/{user}"})
//    public String forwardIndex(@PathVariable(required = false) String user) {
//        return "forward:/";
//    }

}
