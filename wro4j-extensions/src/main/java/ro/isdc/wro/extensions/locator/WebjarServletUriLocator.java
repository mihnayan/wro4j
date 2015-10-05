package ro.isdc.wro.extensions.locator;

import static java.lang.String.format;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webjars.WebJarAssetLocator;

import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.model.resource.locator.wildcard.DefaultWildcardStreamLocator;

/**
 * Similar to {@link WebjarUriLocator}, but uses "/webjar/" prefix to identify a webjar resource.
 *
 * @author Alex Objelean
 * @created 5 Oct 2015
 * @since 1.7.10
 */
public class WebjarServletUriLocator
    implements UriLocator {
  private static final Logger LOG = LoggerFactory.getLogger(WebjarServletUriLocator.class);
  /**
   * Alias used to register this locator with {@link LocatorProvider}.
   */
  public static final String ALIAS = "webjarServlet";
  /**
   * Prefix of the resource uri used to check if the resource can be read by this {@link UriLocator} implementation.
   */
  public static final String PREFIX = format("/%s/", ALIAS);
  private final UriLocator classpathLocator = new ClasspathUriLocator();
  private final WebJarAssetLocator webjarAssetLocator = newWebJarAssetLocator();


  /**
   * @return an instance of {@link WebJarAssetLocator} to be used for identifying the fully qualified name of resources
   *         based on provided partial path.
   */
  private WebJarAssetLocator newWebJarAssetLocator() {
    return new WebJarAssetLocator(WebJarAssetLocator.getFullPathIndex(
        Pattern.compile(".*"), Thread.currentThread().getContextClassLoader()));
  }

  /**
   * @return the uri which is acceptable by this locator.
   */
  public static String createUri(final String path) {
    notNull(path);
    return PREFIX + path;
  }

  @Override
  public InputStream locate(final String uri)
      throws IOException {
    LOG.debug("locating: {}", uri);
    try {
      final String fullpath = webjarAssetLocator.getFullPath(extractPath(uri));
      return classpathLocator.locate(ClasspathUriLocator.createUri(fullpath));
    } catch (final Exception e) {
      throw new IOException("No webjar with uri: " + uri + " available.", e);
    }
  }

  /**
   * Replaces the protocol specific prefix and removes the query path if it exist, since it should not be accepted.
   */
  private String extractPath(final String uri) {
    return DefaultWildcardStreamLocator.stripQueryPath(uri.replace(PREFIX, StringUtils.EMPTY));
  }

  @Override
  public boolean accept(final String uri) {
    return uri.trim().startsWith(PREFIX);
  }
}
