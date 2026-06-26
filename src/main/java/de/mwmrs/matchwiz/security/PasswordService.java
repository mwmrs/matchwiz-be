package de.mwmrs.matchwiz.security;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PasswordService {

    public String hash(String plain) {
        return BcryptUtil.bcryptHash(plain);
    }

    public boolean matches(String plain, String hash) {
        return BcryptUtil.matches(plain, hash);
    }
}
