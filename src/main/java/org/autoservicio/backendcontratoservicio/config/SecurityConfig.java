package org.autoservicio.backendcontratoservicio.config;

import org.autoservicio.backendcontratoservicio.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuración de seguridad (WebFlux)
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Swagger
                        .pathMatchers(
                                "/contrato_servicio/swagger-ui.html",
                                "/contrato_servicio/swagger-ui/**",
                                "/contrato_servicio/v3/api-docs/**",
                                "/contrato_servicio/webjars/**",
                                "/contrato_servicio/swagger-resources/**"
                        ).permitAll()

                        // Auth y endpoints públicos
                        .pathMatchers(
                                "/contrato_servicio/api/auth/**",
                                "/contrato_servicio/api/facturacion/buscar_facturas_enlinea",
                                "/contrato_servicio/api/facturacion/actualizar_factura",
                                "/contrato_servicio/api/pagos/**",
                                "/contrato_servicio/api/paramae/**",
                                "/contrato_servicio/api/izipay/**"
                        ).permitAll()

                        // Endpoints protegidos
                        .pathMatchers("/contrato_servicio/api/**").authenticated()
                        .anyExchange().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((exchange, e) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        })
                )
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    /**
     * Configuración de CORS (WebFlux).
     * @Order(-101) garantiza que corre ANTES del SecurityWebFilterChain (order = -100),
     * así las cabeceras CORS se agregan incluso cuando Security rechaza la petición.
     */
    @Bean
    @Order(-101)
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Producción: restringir a tus dominios
        corsConfig.setAllowedOrigins(Arrays.asList(
                "https://agoisp.pro",
                "https://www.agoisp.pro",
                "https://dev.agoisp.pro",
                "https://pago.agoisp.pro",
                "http://localhost:4200",
                "http://localhost:4201",
                "http://31.97.133.166"
        ));

        // Alternativa desarrollo: abrir todo
        // corsConfig.addAllowedOriginPattern("*");

        corsConfig.setAllowCredentials(true);
        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedMethod("*");
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
