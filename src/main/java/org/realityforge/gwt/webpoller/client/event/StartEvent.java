package org.realityforge.gwt.webpoller.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import javax.annotation.Nonnull;
import org.realityforge.gwt.webpoller.client.WebPoller;
import org.realityforge.gwt.webpoller.client.event.StartEvent.Handler;

/**
 * Event fired when the web poller starts polling.
 */
public class StartEvent
  extends WebPollerEvent<Handler>
{
  public interface Handler
    extends EventHandler
  {
    void onStartEvent( @Nonnull StartEvent event );
  }

  private static final GwtEvent.Type<Handler> TYPE = new Type<Handler>();

  public static GwtEvent.Type<Handler> getType()
  {
    return TYPE;
  }

  public StartEvent( @Nonnull final WebPoller webPoller )
  {
    super( webPoller );
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType()
  {
    return StartEvent.getType();
  }

  @Override
  protected void dispatch( @Nonnull final Handler handler )
  {
    handler.onStartEvent( this );
  }
}
