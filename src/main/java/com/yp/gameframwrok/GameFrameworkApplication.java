package com.yp.gameframwrok;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Log4j2
@SpringBootApplication
public class GameFrameworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameFrameworkApplication.class, args);
        log.info("   **      *   ****      *****    *    *    ****     ");
		log.info("   * *     *  *    *     *    *   *    *   *         ");
		log.info("   *  *    *  *    *     *    *   *    *  *          ");
		log.info("   *   *   *  *    *     * * *    *    *  *   ***    ");
		log.info("   *    *  *  *    *     *    *   *    *  *     *    ");
		log.info("   *     * *  *    *     *    *   *    *   *    *    ");
		log.info("   *      **   ****      *****     ****     ****     ");
    }

}
