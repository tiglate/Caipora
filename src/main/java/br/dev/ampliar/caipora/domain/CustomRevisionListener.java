package br.dev.ampliar.caipora.domain;

import br.dev.ampliar.caipora.model.WebFormsUserDetails;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.context.SecurityContextHolder;

public class CustomRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        var customRevisionEntity = (CustomRevisionEntity) revisionEntity;
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof WebFormsUserDetails principal) {
            customRevisionEntity.setUserId(principal.id);
        }
    }
}
