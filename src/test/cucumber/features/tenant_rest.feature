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


