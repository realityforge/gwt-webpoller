package org.realityforge.gwt.webpoller.client.html5;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import javax.annotation.Nonnull;
import org.realityforge.gwt.webpoller.client.WebPoller;

public class Html5WebPoller
  extends WebPoller
{
  private static final int POLL_DURATION = 2000;

  private Timer _timer;
  private Request _request;

  public static class Factory
    implements WebPoller.Factory
  {
    @Nonnull
    @Override
    public WebPoller newWebPoller( @Nonnull final RequestFactory requestFactory )
    {
      return new Html5WebPoller( new SimpleEventBus(), requestFactory );
    }
  }

  public Html5WebPoller( @Nonnull final EventBus eventBus,
                         @Nonnull final RequestFactory requestFactory )
  {
    super( eventBus, requestFactory );
  }

  @Override
  protected void doStop()
  {
    stopTimer();
    if ( null != _request && _request.isPending() )
    {
      _request.cancel();
      _request = null;
    }
    super.doStop();
  }

  @Override
  protected void doStart()
  {
    if ( isLongPoll() )
    {
      poll();
    }
    else
    {
      startTimer();
    }
    super.doStart();
  }

  private void startTimer()
  {
    stopTimer();
    _timer = new Timer()
    {
      @Override
      public void run()
      {
        poll();
      }
    };

    _timer.scheduleRepeating( POLL_DURATION );
  }

  private void stopTimer()
  {
    if ( null != _timer )
    {
      _timer.cancel();
      _timer = null;
    }
  }

  @Override
  protected void doPoll()
  {
    final RequestBuilder requestBuilder =
      getRequestFactory().getRequestBuilder( new RequestCallback()
      {
        @Override
        public void onResponseReceived( final Request request, final Response response )
        {
          final String data = response.getText();
          if ( 0 == data.length() )
          {
            onEmptyPollResult();
          }
          else
          {
            onMessage( response.getText() );
          }
          pollReturned();
        }

        @Override
        public void onError( final Request request, final Throwable exception )
        {
          Html5WebPoller.this.onError();
          pollReturned();
        }
      } );
    try
    {
      _request = requestBuilder.send();
    }
    catch ( final RequestException e )
    {
      onError();
      pollReturned();
    }
  }

  protected void pollReturned()
  {
    _request = null;
    super.pollReturned();
  }
}
