package ttaomae.connectn.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * A utility for obtaining {@link ResourceBundle}s.
 *
 * @author Todd Taomae
 */
public final class ResourceBundleUtil
{
    private ResourceBundleUtil()
    {
        // prevent instantiation
    }

    /**
     * Returns a {@link ResourceBundle} with the specified base name and locale
     * defined in the specified locale file. The locale file should be the name
     * of a {@code .properties} file {@linkplain ClassLoader#getResource(String)
     * resource} containing a {@code language} and {@code country} property.
     *
     * @param resourceBaseName the {@code ResourceBundle} base name
     * @param localeFileName the name of the locale properties file resource
     * @return a {@link ResourceBundle} with the specified base name and locale
     *         defined in the specified locale file.
     */
    public static ResourceBundle getResourceBundle(String resourceBaseName, String localeFileName)
    {
        try (InputStream propertyFile = ResourceBundleUtil.class.getClassLoader()
                .getResourceAsStream(localeFileName)) {
            Properties localeProperties = new Properties();
            try {
                localeProperties.load(propertyFile);
            } catch (IOException e) {
                throw new RuntimeException("Could not load localePropertyFile", e);
            }
            Locale locale = new Locale(
                    localeProperties.getProperty("language"),
                    localeProperties.getProperty("country"));
            return ResourceBundle.getBundle(resourceBaseName, locale);
        }
        catch (IOException e) {
            throw new RuntimeException("An error occurred while closing locale property file.", e);
        }
    }
}
