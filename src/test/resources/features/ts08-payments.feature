@TS08
Feature: TS08 · Simulated payments API
  As a Developer, I want simulated payment endpoints to hold and release payments without a gateway.

  Background:
    Given a client "Rosa" is registered and signed in
    And a verified student "Valeria" with the category "GRAPHIC_DESIGN"
    And "Rosa" published a project "Identidad visual" with budget 450.00
    And "Valeria" sent a proposal for 450.00 in 10 days
    And "Rosa" accepted the proposal

  Scenario: Held payment with the service fee
    Given "Rosa" paid the engagement
    Then the payment of the engagement is "HELD"
    And the response field "$.serviceFee" is "45.0"
    And the response field "$.totalCharged" is "495.0"
    And the response field "$.studentPayout" is "450.0"

  Scenario: Approval releases the payment and a second approval is rejected (QA05)
    Given "Rosa" paid the engagement
    And "Valeria" delivered the work
    And "Rosa" approved the work
    Then the payment of the engagement is "RELEASED"
    When "Rosa" sends a POST request to "/api/v1/engagements/{engagementId}/approval"
    Then the response status is 409

  Scenario: Another user cannot pay
    Given a client "Mario" is registered and signed in
    When "Mario" sends a POST request to "/api/v1/engagements/{engagementId}/payments"
    Then the response status is 403
