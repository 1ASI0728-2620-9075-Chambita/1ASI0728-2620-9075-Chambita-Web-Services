package pe.chambitalabs.platform.certification.application.internal.outboundservices;

import org.springframework.stereotype.Service;
import pe.chambitalabs.platform.engagements.interfaces.acl.EngagementsContextFacade;

import java.util.UUID;

/** ACL towards Engagements: whether the engagement has an open dispute. */
@Service
public class ExternalEngagementsService {

    private final EngagementsContextFacade engagementsContextFacade;

    public ExternalEngagementsService(EngagementsContextFacade engagementsContextFacade) {
        this.engagementsContextFacade = engagementsContextFacade;
    }

    public boolean hasOpenDispute(UUID engagementId) {
        return engagementsContextFacade.hasOpenDispute(engagementId);
    }
}
