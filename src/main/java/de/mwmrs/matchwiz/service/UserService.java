package de.mwmrs.matchwiz.service;

import de.mwmrs.matchwiz.entity.AppUser;
import de.mwmrs.matchwiz.exception.BusinessException;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class UserService {

    public List<AppUser> list() {
        return AppUser.listAll(Sort.ascending("username"));
    }

    @Transactional
    public AppUser approve(Long id) {
        AppUser u = AppUser.findById(id);
        if (u == null) {
            throw BusinessException.notFound("User not found");
        }
        u.active = true;
        return u;
    }
}
