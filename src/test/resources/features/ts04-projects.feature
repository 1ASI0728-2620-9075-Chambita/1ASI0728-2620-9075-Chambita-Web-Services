@TS04
Feature: TS04 · Projects API
  As a Developer, I want project endpoints to publish and search them.

  Background:
    Given a client "Rosa" is registered and signed in

  Scenario: Publication
    When "Rosa" sends a POST request to "/api/v1/projects" with:
      """
      {"title":"Identidad visual para marca de ropa","description":"Logo, paleta y 6 plantillas para Instagram.",
       "category":"GRAPHIC_DESIGN","requiredSkills":["Illustrator"],"deliverables":["Logo"],
       "budget":450.00,"deadline":"2099-12-31"}
      """
    Then the response status is 201
    And the response field "$.status" is "OPEN"
    And the response field "$.budget" is "450.0"

  Scenario: Missing budget
    When "Rosa" sends a POST request to "/api/v1/projects" with:
      """
      {"title":"Sin presupuesto","description":"Falta el presupuesto","category":"GRAPHIC_DESIGN","deadline":"2099-12-31"}
      """
    Then the response status is 400
    And the response field "$.fields.budget" exists

  Scenario: Only clients publish projects
    Given a verified student "Valeria" with the category "GRAPHIC_DESIGN"
    When "Valeria" sends a POST request to "/api/v1/projects" with:
      """
      {"title":"Proyecto","description":"Descripción","category":"GRAPHIC_DESIGN","budget":100,"deadline":"2099-12-31"}
      """
    Then the response status is 403

  Scenario: Search with filters
    Given "Rosa" published a project "Logo y empaque {RunId}" with budget 320.00
    When "Rosa" sends a GET request to "/api/v1/projects?category=GRAPHIC_DESIGN&status=OPEN&q={RunId}"
    Then the response status is 200
    And the response list "$" has 1 items
    And the response field "$[0].title" is "Logo y empaque {RunId}"
    When "Rosa" sends a GET request to "/api/v1/projects?category=COPYWRITING&q={RunId}"
    Then the response list "$" has 0 items
