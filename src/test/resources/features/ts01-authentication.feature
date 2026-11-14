@TS01
Feature: TS01 · Sign-up and sign-in API
  As a Developer, I want sign-up and sign-in endpoints with tokens to protect access to the API.

  Scenario: Sign-up of a client
    When an anonymous user sends a POST request to "/api/v1/authentication/sign-up" with:
      """
      {"email":"rosa.huaman.ts01.{RunId}@gmail.com","password":"Chambita2026!","fullName":"Rosa Huamán","role":"CLIENT"}
      """
    Then the response status is 201
    And the response field "$.role" is "CLIENT"
    And the response field "$.status" is "ACTIVE"

  Scenario: Duplicated email
    Given a client "Rosa" is registered and signed in
    When an anonymous user sends a POST request to "/api/v1/authentication/sign-up" with:
      """
      {"email":"{RosaEmail}","password":"Chambita2026!","fullName":"Rosa","role":"CLIENT"}
      """
    Then the response status is 409

  Scenario: Sign-in with valid and invalid credentials
    Given a client "Rosa" is registered and signed in
    When "Rosa" sends a GET request to "/api/v1/users/me"
    Then the response status is 200
    And the response field "$.id" is "{Rosa}"
    When an anonymous user sends a POST request to "/api/v1/authentication/sign-in" with:
      """
      {"email":"nobody@gmail.com","password":"wrong-password"}
      """
    Then the response status is 401

  Scenario: A student must use an institutional email (US09)
    When an anonymous user sends a POST request to "/api/v1/authentication/sign-up" with:
      """
      {"email":"valeria.ts01@gmail.com","password":"Chambita2026!","fullName":"Valeria Rojas","role":"STUDENT"}
      """
    Then the response status is 422

  Scenario: Requests without a token are rejected
    When an anonymous user sends a GET request to "/api/v1/projects"
    Then the response status is 401
