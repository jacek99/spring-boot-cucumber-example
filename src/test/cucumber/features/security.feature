@security
Feature: Tests for the Basic Auth security layer

  @security
  Scenario Outline: Basic auth security
    When "<user>:<password>" sends GET "/myapp/system/tenants"
    Then I expect HTTP code <code>
    And I expect JSON equivalent to
    """
      {
        "status": <code>,
        "error": "Unauthorized",
        "message": "<message>",
        "path": "/myapp/system/tenants"
      }
    """

    Examples:
      | user              | password          | code        | message                                 |
      # wrong user name format
      | null              | null              | 401         | Unknown user                            |
      # non-existant tenant
      | ronald@mcdonalds  | null              | 401         | Unknown user                            |
      # non existant user in an existing tenant
      | ronald@system     | null              | 401         | Unknown user                            |
      # right user/tenant, wrong password
      | admin@system      | wrongiswrong      | 401         | Wrong password                          |
