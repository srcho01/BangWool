package Backend.BangWool.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "방울 API 명세서", description = "token header 필요 없는 API : /auth/**, /login/**",
                version = "v1"))
@RequiredArgsConstructor
@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi chatOpenApi() {
        String[] paths = {"/**"}; // 경로에 매칭되는 API를 그룹화하여 문서화

        return GroupedOpenApi.builder()
                .group("BangWool")  // 그룹 이름을 설정한다.
                .pathsToMatch(paths)     // 그룹에 속하는 경로 패턴을 지정한다.
                .build();
    }

    @Bean
    public OpenAPI api() {
        SecurityScheme apiKey = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("Description: 앞에 \"Bearer \"와 함께 /login을 실행하여 발급받은 access token을 넣어주세요");

        SecurityRequirement requirement = new SecurityRequirement().addList("Bearer");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("Bearer", apiKey))
                .addSecurityItem(requirement);
    }
}