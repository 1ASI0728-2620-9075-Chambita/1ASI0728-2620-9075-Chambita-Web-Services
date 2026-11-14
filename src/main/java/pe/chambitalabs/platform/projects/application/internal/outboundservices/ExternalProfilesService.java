package pe.chambitalabs.platform.projects.application.internal.outboundservices;

import org.springframework.stereotype.Service;
import pe.chambitalabs.platform.profiles.interfaces.acl.ProfilesContextFacade;
import pe.chambitalabs.platform.profiles.interfaces.acl.ProfilesContextFacade.StudentSnapshot;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/** ACL towards Profiles: eligibility and public data of students. */
@Service
public class ExternalProfilesService {

    private final ProfilesContextFacade profilesContextFacade;

    public ExternalProfilesService(ProfilesContextFacade profilesContextFacade) {
        this.profilesContextFacade = profilesContextFacade;
    }

    public boolean isStudentEligible(UUID studentId) {
        return profilesContextFacade.isStudentEligible(studentId);
    }

    public Map<UUID, StudentSnapshot> fetchStudents(Collection<UUID> studentIds) {
        return profilesContextFacade.fetchStudentSnapshots(studentIds);
    }

    public String fetchClientName(UUID clientId) {
        return profilesContextFacade.fetchClientDisplayName(clientId).orElse("");
    }
}
