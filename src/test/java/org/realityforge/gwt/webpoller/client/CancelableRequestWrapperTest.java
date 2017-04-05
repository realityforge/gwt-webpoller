package org.realityforge.gwt.webpoller.client;

import org.testng.annotations.Test;
import static org.mockito.Mockito.*;

public class CancelableRequestWrapperTest
{
  @Test
  public void setThenCancel()
  {
    final CancelableRequestWrapper wrapper = new CancelableRequestWrapper();
    final Request request = mock( Request.class );
    wrapper.setRequest( request );

    verify( request, never() ).cancel();

    wrapper.cancel();

    verify( request ).cancel();
  }

  @Test
  public void cancelThenSet()
  {
    final CancelableRequestWrapper wrapper = new CancelableRequestWrapper();
    wrapper.cancel();

    final Request request = mock( Request.class );
    wrapper.setRequest( request );

    verify( request ).cancel();
  }
}
