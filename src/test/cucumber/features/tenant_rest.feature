@tenant
Feature: Tenant REST service (system admin only)

  @tenant_get
  Scenario: Query all
    # should get default system tenant only
    When "admin@system:adminadmin" sends GET "/myapp/system/tenants"
    Then I expect HTTP code 200
    And I expect JSON equivalent to
    """
      [
        {
          "tenantId": "system",
          "name": "system",
          "url": "system"
        }
      ]
    """

  @tenant_get_error
  Scenario Outline: Query error handling
    When "admin@system:adminadmin" sends <method> "/myapp/system/tenant/WRONG_ID"
    Then I expect HTTP code 404
    And I expect JSON equivalent to
    """
      {
        "status": 404,
        "error": "Not Found",
        "path": "/myapp/system/tenant/WRONG_ID"
      }
    """

    Examples:
      | method  | user  |
      | GET     | read  |
      | PATCH   | admin |
      | DELETE  | admin |

  @tenant_add
  Scenario: Add one
    # add and return JSON as part of the payload
    When "admin@system:adminadmin" sends POST "/myapp/system/tenants" with JSON
    """
    {
      "tenantId": "mcdonalds",
      "name": "McDonalds"
    }
    """
    Then I expect HTTP code 201
    And I expect JSON equivalent to
    """
    {
      "tenantId": "mcdonalds",
      "name": "McDonalds"
    }
    """
    And I expect HTTP header "location" contains "/myapp/system/tenants/mcdonalds"

    # verify it got added
    When "admin@system:adminadmin" sends GET "/myapp/system/tenants"
    Then I expect HTTP code 200
    # result set should always be sorted alphabeticall by ID for consistent JSON docs in BDD testing
    And I expect JSON equivalent to
    """
      [
        {
          "tenantId": "mcdonalds",
          "name": "McDonalds",
          "url": null
        },
        {
          "tenantId": "system",
          "name": "system",
          "url": "system"
        }
      ]
    """


  @tenant_add_error
  Scenario Outline: Add error handling
    When "admin@system:adminadmin" sends POST "/myapp/system/tenants" with JSON
    """
     {
      "tenantId": "test",
      "name": "Test Tenant",
      "<field>": "<value>"
    }
    """
    Then I expect HTTP code <code>
    And I expect JSON equivalent to
    """
      {
        "status": <code>,
        "errors": [
          {
            "defaultMessage": "<message>",
            "objectName": "tenant",
            "field": "<field>",
            "rejectedValue": "<value>",
            "code": "<errorCode>"
          }
        ]
      }
    """

    Examples:
      | field         | value           | code      | message                               | errorCode |
      | tenantId      | a               | 400       | size must be between 2 and 10         | Size      |
      | tenantId      | asdfdfsdfsafas  | 400       | size must be between 2 and 10         | Size      |
      | url           | a               | 400       | must be a valid URL                   | URL       |
      | name          | a               | 400       | size must be between 2 and 30         | Size      |
      | name          | asdqsdqsdljsljdlqsldsqjdlqjsdjqsjlqjsldqlsdqsjdlsqsdqxacxaca     | 400       | size must be between 2 and 30         | Size      |

  @tenant_dup
  Scenario: Disallow duplicates
    # add
    When "admin@system:adminadmin" sends POST "/myapp/system/tenants" with JSON
    """
    {
      "tenantId": "mcdonalds",
      "name": "McDonalds"
    }
    """
    Then I expect HTTP code 201
    # try add a second one - should throw a 409 Conflict
    When "admin@system:adminadmin" sends POST "/myapp/system/tenants" with JSON
    """
    {
      "tenantId": "mcdonalds",
      "name": "The Real McDonalds"
    }
    """
    Then I expect HTTP code 409
    And I expect JSON equivalent to
    """
      {
        "status": 409,
        "error": "Conflict",
        "message": "Tenant identified by ID mcdonalds already exists"
      }
    """
    # verify only 2 exist (original one + system)
    When "admin@system:adminadmin" sends GET "/myapp/system/tenants"
    Then I expect HTTP code 200
    And I expect JSON equivalent to
    """
      [
        {
          "tenantId": "mcdonalds",
          "name": "McDonalds",
          "url": null
        },
        {
          "tenantId": "system",
          "name": "system",
          "url": "system"
        }
      ]
    """

  @tenant_update
  Scenario Outline: Update
    # add
    When "admin@system:adminadmin" sends POST "/myapp/system/tenants" with JSON
    """
    {
      "tenantId": "mcdonalds",
      "name": "McDonalds"
    }
    """
    Then I expect HTTP code 201
    # update it
    When "admin@system:adminadmin" sends PATCH "/myapp/system/tenants/mcdonalds" with JSON
    """
    {
      "tenantId": "mcdonalds",
      "name": "McDonalds",
      "<field>": <value>
    }
    """
    Then I expect HTTP code 204
    # verify
    When "admin@system:adminadmin" sends GET "/myapp/system/tenants/mcdonalds"
    Then I expect HTTP code 200
    And I expect JSON equivalent to
    """
    <content>
    """

    Examples:
      | field          | value                  | content                                                                             |
      | name           | "Real McDonalds"       | {"tenantId": "mcdonalds","name": "Real McDonalds","url": null}                      |
      | url            | "http://mcdonalds.com" | {"tenantId": "mcdonalds","name": "McDonalds","url": "http://mcdonalds.com"}         |

  @tenant_update_error
  Scenario Outline: Update error handling
    # add
    When "admin@system:adminadmin" sends POST "/myapp/system/tenants" with JSON
    """
    {
      "tenantId": "mcdonalds",
      "name": "McDonalds"
    }
    """
    Then I expect HTTP code 201
    # update
    When "admin@system:adminadmin" sends PATCH "/myapp/system/tenants/mcdonalds" with JSON
    """
    {
      "tenantId": "mcdonalds",
      "name": "McDonalds",
      "<field>":"<value>"
    }
    """
    Then I expect HTTP code <code>
    And I expect JSON equivalent to
    """
      {
        "status": <code>,
        "errors": [
          {
            "defaultMessage": "<message>",
            "objectName": "tenant",
            "field": "<field>",
            "rejectedValue": "<value>",
            "code": "<errorCode>"
          }
        ]
      }
    """

    Examples:
      | field       | value             | code    | message                       | errorCode       |
      | name        | a                 | 400     | size must be between 2 and 30 | Size            |
      | url         | a.com             | 400     | must be a valid URL           | URL             |

  @tenant_delete
  Scenario: Delete single
    # add 2
    When "admin@system:adminadmin" sends POST "/myapp/system/tenants" with JSON
    """
    {
      "tenantId": "mcdonalds",
      "name": "McDonalds"
    }
    """
    Then I expect HTTP code 201
    When "admin@system:adminadmin" sends POST "/myapp/system/tenants" with JSON
    """
    {
      "tenantId": "wholefoods",
      "name": "Whole Foods"
    }
    """
    Then I expect HTTP code 201
    # delete & validate
    When "admin@system:adminadmin" sends DELETE "/myapp/system/tenants/wholefoods"
    Then I expect HTTP code 204
    When "admin@system:adminadmin" sends GET "/myapp/system/tenants"
    Then I expect HTTP code 200
    And I expect JSON equivalent to
    """
      [
        {
          "tenantId": "mcdonalds",
          "name": "McDonalds",
          "url": null
        },
        {
          "tenantId": "system",
          "name": "system",
          "url": "system"
        }
      ]
    """
