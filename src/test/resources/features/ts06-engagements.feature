@TS06
Feature: TS06 · Engagements and deliverables API
  As a Developer, I want engagement and deliverable endpoints to manage the life cycle of the project.

  Background:
    Given a client "Rosa" is registered and signed in
    And a verified student "Valeria" with the category "GRAPHIC_DESIGN"
    And "Rosa" published a project "Identidad visual" with budget 450.00
    And "Valeria" sent a proposal for 450.00 in 10 days
    And "Rosa" accepted the proposal

  Scenario: The accepted proposal creates the engagement pending payment
    Then the engagement is "PENDING_PAYMENT"
    When "Valeria" sends a GET request to "/api/v1/engagements/{engagementId}"
    Then the response status is 200
    And the response field "$.agreedPrice" is "450.0"

  Scenario: Delivery moves the engagement to review
    Given "Rosa" paid the engagement
    When "Valeria" sends a POST request to "/api/v1/engagements/{engagementId}/deliverables" with:
      """
      {"linkUrl":"https://drive.google.com/entrega-final","note":"Logo, paleta y plantillas"}
      """
    Then the response status is 201
    And the response field "$.status" is "IN_REVIEW"

  Scenario: An empty delivery is rejected (US25)
    Given "Rosa" paid the engagement
    When "Valeria" sends a POST request to "/api/v1/engagements/{engagementId}/deliverables" with:
      """
      {"note":"Sin archivo"}
      """
    Then the response status is 400

  Scenario: Cancellation before payment (US27)
    When "Rosa" sends a POST request to "/api/v1/engagements/{engagementId}/cancellation"
    Then the response status is 200
    And the response field "$.status" is "CANCELLED"

  Scenario: Users outside the engagement cannot see it
    Given a client "Mario" is registered and signed in
    When "Mario" sends a GET request to "/api/v1/engagements/{engagementId}"
    Then the response status is 403
