package com.github.jacek99.springbootcucumber.test;

import com.github.jacek99.springbootcucumber.security.PasswordHashingService;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for password hashing functionality
 * @author Jacek Furmankiewicz
 */
public class PasswordHashingServiceTests {

    private PasswordHashingService svc = new PasswordHashingService();

    @Test
    public void basicTest() {
        String password = "ressfdsdfaserea";

        PasswordHashingService.HashInfo result =
                svc.hashPassword(password);

        boolean valid = svc.isHashValid(password, result.getPasswordHash(),
                result.getSalt(), result.getRepetitions());

        Assert.assertEquals(true, valid);
    }

    @Test
    public void negativeTest() {
        String password = "ressfdsdfaserea";
        String basPassword = password + "a";

        PasswordHashingService.HashInfo result =
                svc.hashPassword(password);

        boolean valid = svc.isHashValid(basPassword, result.getPasswordHash(),
                result.getSalt(), result.getRepetitions());

        // should fail
        Assert.assertEquals(false, valid);
    }



}
