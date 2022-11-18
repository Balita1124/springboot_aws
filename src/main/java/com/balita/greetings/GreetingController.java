package com.balita.greetings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class GreetingController {

    @Autowired
    private DynamoDBEnhanced dde;

    @Autowired
    private PublishTextSMS msg;

    @GetMapping("/")
    public String greetingForm(Model model) {
        model.addAttribute("greeting", new Greeting());
        return "greeting";
    }

    @PostMapping("/greeting")
    public String greetingSubmit(@ModelAttribute Greeting greeting) {

        // Stores data in an Amazon DynamoDB table.
        dde.injectDynamoItem(greeting);

        // Sends a text notification.
        msg.sendMessage(greeting.getId());

        return "result";
    }
}
