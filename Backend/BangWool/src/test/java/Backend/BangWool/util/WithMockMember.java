package Backend.BangWool.util;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = MockMemberFactory.class)
public @interface WithMockMember {

    int memberID() default 1;
    String username() default "test@test.com";
    String role() default "ROLE_USER";

}
