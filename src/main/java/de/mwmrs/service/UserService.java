package de.mwmrs.service;

import de.mwmrs.entity.AppUser;
import de.mwmrs.exception.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class UserService {

    public List<AppUser> list() {
        return AppUser.listAll();
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
