Feature: NEF API Test Suite2

  Background:
    Given targetApi is /services

  Scenario: Should test header2
    When I GET
    Then response code should be 200
    And response code should not be 401
    And response header Content-Type should exist
    And response header X-CSRF-TOKEN should not exist
    And response body should contain "3"