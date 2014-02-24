package org.realityforge.gwt.webpoller.client;

import java.util.Map;
import javax.annotation.Nonnull;

/**
 * Adapter to make listener implementation easier.
 */
public abstract class WebPollerListenerAdapter
  implements WebPollerListener
{
  /**
   * {@inheritDoc}
   */
  @Override
  public void onStart( @Nonnull final WebPoller webPoller )
  {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onStop( @Nonnull final WebPoller webPoller )
  {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onMessage( @Nonnull final WebPoller webPoller,
                         @Nonnull final Map<String, String> context,
                         @Nonnull final String data )
  {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onEmptyMessage( @Nonnull final WebPoller webPoller )
  {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onError( @Nonnull final WebPoller webPoller, @Nonnull final Throwable exception )
  {
  }
}
