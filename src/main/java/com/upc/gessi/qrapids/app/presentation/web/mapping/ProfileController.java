package com.upc.gessi.qrapids.app.presentation.web.mapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("/Profiles")
public class ProfileController {

    @RequestMapping("/Profiles/Configuration")
    public String Products(){
        return "Profile/Profiles";
    }

}
