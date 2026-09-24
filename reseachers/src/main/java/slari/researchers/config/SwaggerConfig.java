package slari.researchers.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SwaggerConfig — OpenAPI / Swagger UI Configuration
 *
 * Configures:
 *   - API title, description, version shown at top of Swagger UI
 *   - JWT Bearer authentication button in Swagger UI
 *     (the 🔒 Authorize button at the top of the page)
 *
 * Once configured, login via POST /api/auth/login,
 * copy the token, click Authorize, paste "Bearer <token>",
 * and all subsequent requests will include your JWT.
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI slariOpenAPI() {
        return new OpenAPI()
                /* ── API metadata shown at top of Swagger UI ── */
                .info(new Info()
                        .title("SLARI Research Hub API")
                        .description("""
                                Sierra Leone Agricultural Research Institute —
                                Crop Variety Dissemination Platform.
                                
                                To test protected endpoints:
                                1. Use POST /api/auth/login to get a JWT token
                                2. Click the Authorize 🔒 button above
                                3. Enter: Bearer <your_token>
                                4. All secured endpoints will include your token
                                
                                Demo credentials:
                                  Admin:      admin@slari.gov.sl / demo1234
                                  Researcher: amara@slari.gov.sl / demo1234
                                  Extension:  joseph@extension.sl / demo1234
                                  Farmer:     sorie@farmer.sl / demo1234
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SLARI")
                                .email("admin@slari.gov.sl")))

                /* ── JWT Bearer security scheme ── */
                /* This adds the 🔒 Authorize button to Swagger UI */
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your JWT token here (without 'Bearer ' prefix — Swagger adds it automatically)")));
    }
}
