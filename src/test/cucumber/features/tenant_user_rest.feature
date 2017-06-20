@tenant_user
Feature: Tenant User REST API

    Background:
      # create a few tenants as part of data set up
      Given "admin@system:adminadmin" sends POST "/myapp/system/tenants" using JSON:
        | tenantId    | name        |
        | mcdonalds   | McDonalds   |
        | harveys     | Harveys     |
        | greenolive  | Green Olive |


  @tenant_user_get
  Scenario: Query all
    # should get no users in newly created tenant
    # and password should be masked
    When "admin@system:adminadmin" sends GET "/myapp/admin/users"
    Then I expect HTTP code 200
    And I expect JSON equivalent to
    """
      [
        {
          "tenantId": "system",
          "userId": "admin",
          "roles": [
            "SYSTEM_ADMIN",
            "TENANT_ADMIN",
            "TENANT_USER"
          ],
          "active": true,
          "password": "**********"
        }
      ]
    """

  @tenant_user_get_error
  Scenario Outline: Query error handling
    When "admin@system:adminadmin" sends <method> "/myapp/admin/user/WRONG_ID"
    Then I expect HTTP code 404
    And I expect JSON equivalent to
    """
      {
        "status": 404,
        "error": "Not Found",
        "path": "/myapp/admin/user/WRONG_ID"
      }
    """

    Examples:
      | method  | user  |
      | GET     | read  |
      | PATCH   | admin |
      | DELETE  | admin |


  @tenant_user_add
  Scenario Outline: Add one
    # have the system admin add a new account within each tenant
    When "admin@system:adminadmin" sends POST "/myapp/admin/users" with JSON
    """
    {
      "tenantId": "<tenantId>",
      "userId":"<userId>",
      "active": true,
      "roles": ["TENANT_ADMIN","TENANT_USER"],
      "password": "<password>"
    }
    """
    Then I expect HTTP code 201
    And I expect JSON equivalent to
    """
    {
      "tenantId": "<tenantId>",
      "userId":"<userId>",
      "active": true,
      "roles": ["TENANT_ADMIN","TENANT_USER"],
      "password": "<password>"
    }
    """
    And I expect HTTP header "location" contains "/myapp/admin/users/<userId>"

    # now validate that this newly added user can add a new user within its own tenant
    When "<userId>@<tenantId>:<password>" sends POST "/myapp/admin/users" with JSON
    """
    {
      "tenantId": "<tenantId>",
      "userId":"<userId>_extra",
      "active": true,
      "roles": ["TENANT_USER"],
      "password": "<password>_extra"
    }
    """
    Then I expect HTTP code 201
    And I expect JSON equivalent to
    """
    {
      "tenantId": "<tenantId>",
      "userId":"<userId>_extra",
      "active": true,
      "roles": ["TENANT_USER"],
      "password": "<password>_extra"
    }
    """
    And I expect HTTP header "location" contains "/myapp/admin/users/<userId>_extra"

    # verify it got added and the new tenant user can only see users
    # within their own tenant
    When "<userId>@<tenantId>:<password>" sends GET "/myapp/admin/users"
    Then I expect HTTP code 200
    And I expect JSON equivalent to
    """
      [
        {
          "tenantId": "<tenantId>",
          "userId": "<userId>",
          "roles": [
            "TENANT_ADMIN",
            "TENANT_USER"
          ],
          "active": true,
          "password": "**********"
        },
        {
          "tenantId": "<tenantId>",
          "userId": "<userId>_extra",
          "roles": [
            "TENANT_USER"
          ],
          "active": true,
          "password": "**********"
        }
      ]
    """

    # validate that system tenant can see across ALL tenants
    When "admin@system:adminadmin" sends GET "/myapp/admin/users"
    Then I expect HTTP code 200
    And I expect JSON equivalent to
    """
      [
        {
          "tenantId": "system",
          "userId": "admin",
          "roles": [
            "SYSTEM_ADMIN",
            "TENANT_ADMIN",
            "TENANT_USER"
          ],
          "active": true,
          "password": "**********"
        },
        {
          "tenantId": "<tenantId>",
          "userId": "<userId>",
          "roles": [
            "TENANT_ADMIN",
            "TENANT_USER"
          ],
          "active": true,
          "password": "**********"
        },
        {
          "tenantId": "<tenantId>",
          "userId": "<userId>_extra",
          "roles": [
            "TENANT_USER"
          ],
          "active": true,
          "password": "**********"
        }
      ]
    """


      Examples:
        | userId          | tenantId      | password        |
        | test            | mcdonalds     | testpwd         |
        | test2           | harveys       | testpwd2        |
