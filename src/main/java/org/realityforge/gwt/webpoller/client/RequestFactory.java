package org.realityforge.gwt.webpoller.client;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The interface by which the poller created requests.
 */
public interface RequestFactory
{
  /**
   * Create a request within specified context.
   * The context is used by the factory to interact with the poller.
   *
   * @param context the request context.
   * @return the new request.
   * @throws Exception if there is an error creating request.
   */
  @Nullable
  Request newRequest( @Nonnull RequestContext context )
    throws Exception;
}
