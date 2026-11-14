package pe.chambitalabs.platform.shared.infrastructure.documentation;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI documentation with the Bearer (JWT) security scheme (TS14).
 */
@Configuration
public class OpenApiConfiguration {

    private static final String BEARER = "bearerAuth";

    @Bean
    public OpenAPI chambitaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Chambita RESTful API")
                        .version("v1")
                        .description("Marketplace that connects verified university students with entrepreneurs "
                                + "and certifies their experience on Ethereum Sepolia.")
                        .license(new License().name("MIT")))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
