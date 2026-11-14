@TS02
Feature: TS02 · Student verification API
  As a Developer, I want endpoints to verify institutional emails to identify real students.

  Scenario: The code is sent at sign-up and the student is verified
    Given a student "Valeria" registered with an institutional email
    Then a verification code was sent to "Valeria"
    When "Valeria" confirms the verification code
    Then the response status is 200
    And the response field "$.status" is "VERIFIED"

  Scenario: Requesting a new code for a non-university domain
    When an anonymous user sends a POST request to "/api/v1/student-verifications" with:
      """
      {"email":"valeria.rojas@gmail.com"}
      """
    Then the response status is 422

  Scenario: A wrong code is rejected with a message in Spanish
    Given a student "Valeria" registered with an institutional email
    Then a verification code was sent to "Valeria"
    When an anonymous user sends a POST request to "/api/v1/student-verifications/confirmations" with the language "es-419" and:
      """
      {"email":"{ValeriaEmail}","code":"000000"}
      """
    Then the response status is 400
    And the response field "$.detail" is "El código de verificación es incorrecto."
