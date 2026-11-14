@TS05
Feature: TS05 · Proposals API
  As a Developer, I want proposal endpoints so students apply and clients compare them.

  Background:
    Given a client "Rosa" is registered and signed in
    And a verified student "Valeria" with the category "GRAPHIC_DESIGN"
    And "Rosa" published a project "Identidad visual" with budget 450.00

  Scenario: Submission and duplicated proposal
    When "Valeria" sends a POST request to "/api/v1/projects/{projectId}/proposals" with:
      """
      {"price":450.00,"deliveryDays":10,"message":"Te propongo tres bocetos de logo en la primera semana."}
      """
    Then the response status is 201
    And the response field "$.status" is "PENDING"
    When "Valeria" sends a POST request to "/api/v1/projects/{projectId}/proposals" with:
      """
      {"price":400.00,"deliveryDays":8,"message":"Otra propuesta"}
      """
    Then the response status is 409

  Scenario: A student without a complete profile cannot apply (US13)
    Given a student "Andres" registered with an institutional email
    And "Andres" confirms the verification code
    And "Andres" signs in
    When "Andres" sends a POST request to "/api/v1/projects/{projectId}/proposals" with:
      """
      {"price":300.00,"deliveryDays":5,"message":"Quiero postular"}
      """
    Then the response status is 422

  Scenario: Order by rating and acceptance
    Given "Valeria" sent a proposal for 450.00 in 10 days
    When "Rosa" sends a GET request to "/api/v1/projects/{projectId}/proposals?sort=rating"
    Then the response status is 200
    And the response list "$" has 1 items
    And the response field "$[0].student.fullName" is "Valeria Test"
    When "Rosa" sends a POST request to "/api/v1/projects/{projectId}/proposals/{proposalId}/acceptance"
    Then the response status is 200
    And the response field "$.status" is "ACCEPTED"

  Scenario: Only the owner sees the proposals
    Given "Valeria" sent a proposal for 450.00 in 10 days
    When "Valeria" sends a GET request to "/api/v1/projects/{projectId}/proposals"
    Then the response status is 403
