package org.realityforge.gwt.webpoller.client;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import javax.annotation.Nonnull;

final class TestWebPoller
  extends WebPoller
{
  static class TestRequest
    implements Request
  {
    @Override
    public void cancel()
    {
    }
  }

  static class TestRequestFactory
    implements RequestFactory
  {
    @Nonnull
    @Override
    public Request newRequest( @Nonnull final RequestContext context )
      throws Exception
    {
      return new TestRequest();
    }
  }

  static class Factory
    implements WebPoller.Factory
  {
    @Nonnull
    @Override
    public WebPoller newWebPoller()
    {
      return new TestWebPoller( new SimpleEventBus() );
    }
  }

  int _pollCount;

  TestWebPoller( final EventBus eventBus )
  {
    super( eventBus );
  }

  @Override
  protected void doPoll()
  {
    super.doPoll();
    _pollCount++;
  }
}
