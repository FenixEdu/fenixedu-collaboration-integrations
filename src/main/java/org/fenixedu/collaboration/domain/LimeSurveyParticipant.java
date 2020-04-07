package org.fenixedu.collaboration.domain;

import org.fenixedu.bennu.core.domain.User;

public class LimeSurveyParticipant extends LimeSurveyParticipant_Base {
    
    public LimeSurveyParticipant(final LimeSurvey limeSurvey, final User user, final String token) {
        setLimeSurvey(limeSurvey);
        setUser(user);
        setParticipationToken(token);
    }

    public void delete() {
        setLimeSurvey(null);
        setUser(null);
        deleteDomainObject();
    }

}
