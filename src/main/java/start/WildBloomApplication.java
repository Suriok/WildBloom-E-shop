package start;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class WildBloomApplication {
    public static void main(String[] args) {
        SpringApplication.run(WildBloomApplication.class, args);}
}
