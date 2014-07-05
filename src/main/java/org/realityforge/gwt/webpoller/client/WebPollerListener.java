package org.realityforge.gwt.webpoller.client;

import java.util.Map;
import javax.annotation.Nonnull;

public interface WebPollerListener
{
  /**
   * Fire a Start event.
   */
  void onStart( @Nonnull WebPoller webPoller );

  /**
   * Fire a Stop event.
   */
  void onStop( @Nonnull WebPoller webPoller );

  /**
   * Fire a Message event.
   */
  void onMessage( @Nonnull WebPoller webPoller, @Nonnull Map<String, String> context, @Nonnull String data );

  /**
   * Fire an Error event.
   * If the number of successive errors reaches a threshold then shut-down the poller.
   */
  void onError( @Nonnull WebPoller webPoller, @Nonnull Throwable exception );
}
