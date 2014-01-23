package org.realityforge.gwt.webpoller.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import javax.annotation.Nonnull;
import org.realityforge.gwt.webpoller.client.WebPoller;

/**
 * Base class of all events originating from WebPoller.
 */
public abstract class WebPollerEvent<H extends EventHandler>
  extends GwtEvent<H>
{
  private final WebPoller _webPoller;

  protected WebPollerEvent( @Nonnull final WebPoller webPoller )
  {
    _webPoller = webPoller;
  }

  @Nonnull
  public final WebPoller getWebPoller()
  {
    return _webPoller;
  }
}
