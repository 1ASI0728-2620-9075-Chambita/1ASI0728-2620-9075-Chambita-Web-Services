@TS03
Feature: TS03 · Profiles and portfolio API
  As a Developer, I want profile and portfolio endpoints so the applications show the talent of students.

  Background:
    Given a verified student "Valeria" with the category "GRAPHIC_DESIGN"
    And a client "Rosa" is registered and signed in

  Scenario: The client reads the profile of a student
    When "Rosa" sends a GET request to "/api/v1/students/{Valeria}"
    Then the response status is 200
    And the response field "$.complete" is "true"
    And the response field "$.newInPlatform" is "true"
    And the response field "$.serviceCategories[0]" is "GRAPHIC_DESIGN"

  Scenario: The student adds a work to the portfolio
    When "Valeria" sends a POST request to "/api/v1/students/{Valeria}/portfolio-items" with:
      """
      {"title":"Logo para cafetería","description":"Identidad visual","externalUrl":"https://behance.net/valeria"}
      """
    Then the response status is 201
    And the response field "$.title" is "Logo para cafetería"

  Scenario: Another user cannot edit the portfolio
    When "Rosa" sends a POST request to "/api/v1/students/{Valeria}/portfolio-items" with:
      """
      {"title":"Intruso"}
      """
    Then the response status is 403

  Scenario: Unknown student
    When "Rosa" sends a GET request to "/api/v1/students/00000000-0000-0000-0000-000000000000"
    Then the response status is 404
