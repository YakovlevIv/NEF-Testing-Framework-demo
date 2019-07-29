package com.nex.test.demo;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"pretty", "html:target/cucumber"},
        features = "src/main/resources/features",
        glue = {"com.nex.test.demo.core.service"})
public class CucumberTest {}
