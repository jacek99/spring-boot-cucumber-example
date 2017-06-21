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
        },
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


      Examples:
        | userId          | tenantId      | password        |
        | test            | mcdonalds     | testpwd         |
        | test2           | harveys       | testpwd2        |

  @tenant_validation_error
  Scenario: Tenant validation
    When "admin@system:adminadmin" sends POST "/myapp/admin/users" with JSON
    """
    {
      "tenantId": "WRONG_TENANT",
      "userId":"testuser",
      "active": true,
      "roles": ["TENANT_ADMIN","TENANT_USER"],
      "password": "testtest",
      "<field>": "<value>"
    }
    """
    Then I expect HTTP code 404
    And I expect JSON equivalent to
    """
      {
        "status": 404,
        "error": "Not Found",
        "message": "Entity Tenant with ID WRONG_TENANT does not exist"
      }
    """

  @tenant_user_add_error
  Scenario Outline: Add error handling
    When "admin@system:adminadmin" sends POST "/myapp/admin/users" with JSON
    """
    {
      "tenantId": "mcdonalds",
      "userId":"testuser",
      "active": true,
      "roles": ["TENANT_ADMIN","TENANT_USER"],
      "password": "testtest",
      "<field>": <value>
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
            "objectName": "tenantUser",
            "field": "<field>",
            "code": "<errorCode>"
          }
        ]
      }
    """

    Examples:
      | field         | value                             | code      | message                                   | errorCode     |
      | userId        | "a"                               | 400       | size must be between 4 and 30             | Size          |
      | roles         | []                                | 400       | size must be between 1 and 3              | Size          |
      # non-existant role
      | roles         | ["WRONG_ROLE"]                    | 400       | invalid security role specified           | SecurityRoles |
      # mix of good and non-existant role
      | roles         | ["WRONG_ROLE","ROLE_TENANT_USER"] | 400       | invalid security role specified           | SecurityRoles |
      | password      | "a"                               | 400       | size must be between 5 and 100            | Size          |

  @tenant_user_dup
  Scenario Outline: Disallow duplicates (from either system or tenant account)
    # add an admin user within a tenant
    When "admin@system:adminadmin" sends POST "/myapp/admin/users" with JSON
    """
    {
      "tenantId": "mcdonalds",
      "userId":"admin",
      "active": true,
      "roles": ["TENANT_ADMIN","TENANT_USER"],
      "password": "testpassword"
    }
    """

    # add a regular user
    When "<user>@<tenant>:<password>" sends POST "/myapp/admin/users" with JSON
    """
    {
      "tenantId": "mcdonalds",
      "userId":"ronald",
      "active": true,
      "roles": ["TENANT_USER"],
      "password": "testpassword"
    }
    """
    Then I expect HTTP code 201

    # try add a second one with same ID - should throw a 409 Conflict
    When "<user>@<tenant>:<password>" sends POST "/myapp/admin/users" with JSON
    """
    {
      "tenantId": "mcdonalds",
      "userId":"ronald",
      "active": false,
      "roles": ["TENANT_USER"],
      "password": "differentpassword"
    }
    """
    Then I expect HTTP code 409
    And I expect JSON equivalent to
    """
      {
        "status": 409,
        "error": "Conflict",
        "message": "TenantUser identified by ID ronald already exists"
      }
    """
    # verify only 1 user called "ronald" exists
    # the other one never made it
    When "admin@system:adminadmin" sends GET "/myapp/admin/users"
    Then I expect HTTP code 200
    And I expect JSON equivalent to
    """
      [
        {
          "tenantId": "mcdonalds",
          "userId": "admin",
          "roles": [
            "TENANT_ADMIN",
            "TENANT_USER"
          ],
          "active": true,
          "password": "**********"
        },
        {
          "tenantId": "mcdonalds",
          "userId": "ronald",
          "roles": [
            "TENANT_USER"
          ],
          "active": true,
          "password": "**********"
        },
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

    Examples:
      | user        | tenant      | password      |
      | admin       | system      | adminadmin    |
      | admin       | mcdonalds   | testpassword  |


