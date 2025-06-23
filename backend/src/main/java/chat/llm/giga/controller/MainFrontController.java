package chat.llm.giga.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class MainFrontController {
    static final Logger log = LoggerFactory.getLogger(MainFrontController.class);

    @RequestMapping({"/"})
    public String loadUI() {
        System.out.println("Forward !!!!");
        log.info("Forward on React index.html...");

        return "forward:/index.html";
    }
}