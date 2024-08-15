package Backend.BangWool.util;

import Backend.BangWool.member.dto.Session;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class MockMemberFactory implements WithSecurityContextFactory<WithMockMember> {

    @Override
    public SecurityContext createSecurityContext(WithMockMember annotation) {

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Session session = new Session(annotation.id(), annotation.username(), annotation.role());
        Authentication authToken = new UsernamePasswordAuthenticationToken(session, null, session.getAuthorities());
        context.setAuthentication(authToken);

        return context;
    }
}
