package pe.chambitalabs.platform.shared.infrastructure.i18n;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * Resolves the language from Accept-Language: en_US by default, es_419 when requested (TS15).
 */
@Configuration
public class LocaleConfiguration {

    public static final Locale ENGLISH = Locale.forLanguageTag("en-US");
    public static final Locale LATIN_AMERICAN_SPANISH = Locale.forLanguageTag("es-419");

    @Bean
    public LocaleResolver localeResolver() {
        var resolver = new AcceptHeaderLocaleResolver();
        resolver.setSupportedLocales(List.of(ENGLISH, LATIN_AMERICAN_SPANISH, Locale.forLanguageTag("es")));
        resolver.setDefaultLocale(ENGLISH);
        return resolver;
    }
}
