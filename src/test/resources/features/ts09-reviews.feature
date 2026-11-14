@TS09
Feature: TS09 · Reviews API
  As a Developer, I want review endpoints to register the reputation of clients and students.

  Background:
    Given a client "Rosa" is registered and signed in
    And a verified student "Valeria" with the category "GRAPHIC_DESIGN"
    And "Rosa" published a project "Identidad visual" with budget 450.00
    And "Valeria" sent a proposal for 450.00 in 10 days
    And "Rosa" accepted the proposal

  Scenario: An engagement that is not completed cannot be rated
    When "Rosa" sends a POST request to "/api/v1/engagements/{engagementId}/reviews" with:
      """
      {"score":5,"comment":"Excelente"}
      """
    Then the response status is 409

  Scenario: Valid, out of range and repeated reviews
    Given "Rosa" paid the engagement
    And "Valeria" delivered the work
    And "Rosa" approved the work
    When "Rosa" sends a POST request to "/api/v1/engagements/{engagementId}/reviews" with:
      """
      {"score":6}
      """
    Then the response status is 400
    Given "Rosa" rated "Valeria" with 5 stars
    When "Rosa" sends a POST request to "/api/v1/engagements/{engagementId}/reviews" with:
      """
      {"score":4}
      """
    Then the response status is 409
    When "Rosa" sends a GET request to "/api/v1/users/{Valeria}/reputation"
    Then the response field "$.reviewCount" is "1"
