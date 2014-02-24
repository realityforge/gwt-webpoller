package org.realityforge.gwt.webpoller.client;

import javax.annotation.Nonnull;

public final class TestWebPoller
  extends WebPoller
{
  public static class TestRequest
    implements Request
  {
    @Override
    public void cancel()
    {
    }
  }

  public static class TestRequestFactory
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

  public static class Factory
    implements WebPoller.Factory
  {
    @Nonnull
    @Override
    public WebPoller newWebPoller()
    {
      return new TestWebPoller();
    }
  }

  int _pollCount;

  @Override
  protected void doPoll()
  {
    super.doPoll();
    _pollCount++;
  }
}
