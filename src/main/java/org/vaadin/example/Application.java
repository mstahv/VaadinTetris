
package org.vaadin.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
// Enable additional servlet filters for wscdn and cloud hosted fontawesome
@ServletComponentScan({"com.vaadin.wscdn", "org.peimari.dawn"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
}
