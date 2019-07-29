package com.nex.test.demo;

import org.junit.runner.JUnitCore;

import java.io.IOException;

public class NexTest {

    public static void main(String[] args) throws IOException {

        JUnitCore.main("com.nex.test.demo.CucumberTest");

/*        JUnitCore junit = new JUnitCore();
        Result result = junit.run(CucumberTest.class);;*/

    }

}
