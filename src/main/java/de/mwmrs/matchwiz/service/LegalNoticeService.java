package de.mwmrs.matchwiz.service;

import de.mwmrs.matchwiz.dto.LegalNoticeDto;
import de.mwmrs.matchwiz.entity.LegalNotice;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class LegalNoticeService {

    public LegalNotice get() {
        return LegalNotice.get();
    }

    @Transactional
    public LegalNotice update(LegalNoticeDto request) {
        LegalNotice ln = LegalNotice.get();
        ln.name      = request.name();
        ln.address   = request.address();
        ln.telephone = request.telephone();
        ln.email     = request.email();
        return ln;
    }
}
