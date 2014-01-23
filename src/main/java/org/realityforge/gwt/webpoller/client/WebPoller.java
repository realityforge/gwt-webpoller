package org.realityforge.gwt.webpoller.client;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import javax.annotation.Nonnull;
import org.realityforge.gwt.webpoller.client.event.CloseEvent;
import org.realityforge.gwt.webpoller.client.event.ErrorEvent;
import org.realityforge.gwt.webpoller.client.event.MessageEvent;
import org.realityforge.gwt.webpoller.client.event.OpenEvent;
import org.realityforge.gwt.webpoller.client.html5.Html5WebPoller;

public abstract class WebPoller
{
  public interface Factory
  {
    WebPoller newWebPoller( boolean longPoll );
  }

  private static Factory g_factory;
  private final EventBus _eventBus;

  public static WebPoller newWebPoller( final boolean longPoll )
  {
    if ( null == g_factory )
    {
      register( new Html5WebPoller.Factory() );
    }
    return g_factory.newWebPoller( longPoll );
  }

  public static void register( @Nonnull final Factory factory )
  {
    g_factory = factory;
  }

  public static boolean deregister( @Nonnull final Factory factory )
  {
    if ( g_factory != factory )
    {
      return false;
    }
    else
    {
      g_factory = null;
      return true;
    }
  }

  public WebPoller( final EventBus eventBus )
  {
    _eventBus = eventBus;
  }

  public abstract void connect( @Nonnull String url );

  public abstract void close();

  @Nonnull
  public final HandlerRegistration addOpenHandler( @Nonnull OpenEvent.Handler handler )
  {
    return _eventBus.addHandler( OpenEvent.getType(), handler );
  }

  @Nonnull
  public final HandlerRegistration addCloseHandler( @Nonnull CloseEvent.Handler handler )
  {
    return _eventBus.addHandler( CloseEvent.getType(), handler );
  }

  @Nonnull
  public final HandlerRegistration addMessageHandler( @Nonnull MessageEvent.Handler handler )
  {
    return _eventBus.addHandler( MessageEvent.getType(), handler );
  }

  @Nonnull
  public final HandlerRegistration addErrorHandler( @Nonnull ErrorEvent.Handler handler )
  {
    return _eventBus.addHandler( ErrorEvent.getType(), handler );
  }

  /**
   * Fire a Connected event.
   */
  protected final void onOpen()
  {
    _eventBus.fireEventFromSource( new OpenEvent( this ), this );
  }

  /**
   * Fire a Close event.
   */
  protected final void onClose()
  {
    _eventBus.fireEventFromSource( new CloseEvent( this ), this );
  }

  /**
   * Fire a Message event.
   */
  protected final void onMessage( final String data )
  {
    _eventBus.fireEventFromSource( new MessageEvent( this, data ), this );
  }

  /**
   * Fire an Error event.
   */
  protected final void onError()
  {
    _eventBus.fireEventFromSource( new ErrorEvent( this ), this );
  }
}
