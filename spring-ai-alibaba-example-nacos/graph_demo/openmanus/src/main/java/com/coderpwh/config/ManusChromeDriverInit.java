package com.coderpwh.config;

import com.coderpwh.OpenManusSpringBootApplication;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author coderpwh
 */
@Component
public class ManusChromeDriverInit implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {

        String chromedriverPath;

        if (checkOS()) {
            chromedriverPath = getChromedriverPath("data/chromedriver.exe");
        }
        else {
            chromedriverPath = getChromedriverPath("data/chromedriver");
        }

        setChromeDriver(chromedriverPath);
    }

    private String getChromedriverPath(String resourcePath) throws URISyntaxException {

        URL resource = OpenManusSpringBootApplication.class.getClassLoader().getResource(resourcePath);
        if (resource == null) {
            throw new IllegalStateException("Chromedriver not found: " + resourcePath);
        }

        return Paths.get(resource.toURI()).toFile().getAbsolutePath();
    }

    private static Boolean checkOS() {

        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return true;
        }
        else if (os.contains("mac")) {
            return false;
        }
        else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            System.out.println("Operating System: Unix/Linux");
            return false;
        }
        else {
            System.out.println("Operating System: Unknown");
            return false;
        }
    }

    private static void setChromeDriver(String path) {

        System.setProperty("webdriver.chrome.driver", path);
    }

}
