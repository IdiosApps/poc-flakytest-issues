package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationTestsTwo {

    @Test
    void contextLoadsTwo() {
    }

    @Test
    void iFailTwo() {
        assert(0 == 1);
    }


}
