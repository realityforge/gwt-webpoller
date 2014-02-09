package org.realityforge.gwt.webpoller.client.event;

import com.google.gwt.event.shared.EventHandler;
import javax.annotation.Nonnull;
import org.realityforge.gwt.webpoller.client.WebPoller;
import org.realityforge.gwt.webpoller.client.event.StopEvent.Handler;

/**
 * Event fired when the web poller is stopped.
 */
public class StopEvent
  extends WebPollerEvent<Handler>
{
  public interface Handler
    extends EventHandler
  {
    void onStopEvent( @Nonnull StopEvent event );
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public static Type<Handler> getType()
  {
    return TYPE;
  }

  public StopEvent( @Nonnull final WebPoller webPoller )
  {
    super( webPoller );
  }

  @Override
  public Type<Handler> getAssociatedType()
  {
    return StopEvent.getType();
  }

  @Override
  protected void dispatch( @Nonnull final Handler handler )
  {
    handler.onStopEvent( this );
  }
}
