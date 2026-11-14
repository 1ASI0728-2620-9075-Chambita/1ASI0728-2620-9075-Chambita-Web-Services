@TS11
Feature: TS11 · Certificate issuance from the backend
  As a Developer, I want the backend to issue the certificate on the network when a reviewed project
  is completed, so the process is automatic.

  Background:
    Given a client "Rosa" is registered and signed in
    And a verified student "Valeria" with the category "GRAPHIC_DESIGN"
    And "Rosa" published a project "Identidad visual" with budget 450.00
    And "Valeria" sent a proposal for 450.00 in 10 days
    And "Rosa" accepted the proposal
    And "Rosa" paid the engagement
    And "Valeria" delivered the work
    And "Rosa" approved the work

  Scenario: The client review issues the certificate on Sepolia
    Given "Rosa" rated "Valeria" with 5 stars
    Then the certificate of "Valeria" is "ISSUED"
    And the response field "$[0].transactionHash" exists
    And the response field "$[0].score" is "5"
    And the profile of "Valeria" shows 1 certificate

  Scenario: Public verification data has no personal data (US33, US36)
    Given "Rosa" rated "Valeria" with 5 stars
    And the certificate of "Valeria" is "ISSUED"
    When an anonymous user sends a GET request to "/api/v1/public/certificates/{certificateHash}"
    Then the response status is 200
    And the response field "$.projectTitle" is "Identidad visual"
    And the response field "$.status" is "ISSUED"
    And the response does not contain "Valeria"
    And the response does not contain "{ValeriaEmail}"

  Scenario: The client sees the issued certificates of the student (US15)
    Given "Rosa" rated "Valeria" with 5 stars
    And the certificate of "Valeria" is "ISSUED"
    When "Rosa" sends a GET request to "/api/v1/students/{Valeria}/certificates"
    Then the response status is 200
    And the response list "$" has 1 items
    And the response field "$[0].status" is "ISSUED"
