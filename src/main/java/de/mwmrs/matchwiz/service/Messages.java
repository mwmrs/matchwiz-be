package de.mwmrs.service;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.jboss.logging.Logger;

@ApplicationScoped
public class Messages {

    private static final Logger LOG = Logger.getLogger(Messages.class);

    private Properties en;
    private final Map<String, Properties> locales = new HashMap<>();

    @PostConstruct
    void init() {
        en = load("messages.properties");
        locales.put("de", load("messages_de.properties"));
        locales.put("es", load("messages_es.properties"));
    }

    public String get(String key, String lang, Object... args) {
        Properties props = (lang != null && !lang.isBlank())
                ? locales.getOrDefault(lang.toLowerCase(), en)
                : en;
        String pattern = props.getProperty(key, en.getProperty(key, key));
        return args.length > 0 ? MessageFormat.format(pattern, args) : pattern;
    }

    private static Properties load(String name) {
        Properties p = new Properties();
        try (InputStream in = Messages.class.getClassLoader().getResourceAsStream(name)) {
            if (in == null) {
                LOG.warnf("Message bundle not found: %s", name);
                return p;
            }
            p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOG.errorf(e, "Failed to load message bundle: %s", name);
        }
        return p;
    }
}
