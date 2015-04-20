package com.feedhenry.sdk.tests.api;

import android.test.AndroidTestCase;
import com.feedhenry.sdk.AppProps;

public class AppPropsTest extends AndroidTestCase {
  public void testAppProps() throws Exception {
    AppProps appProps = AppProps.load(getContext());
    assertNotNull(appProps.getHost());
    assertEquals("http://localhost:9000", appProps.getHost());
    assertNull(appProps.getAppApiKey());
    assertNull(appProps.getAppId());
    assertNull(appProps.getProjectId());
    assertNull(appProps.getConnectionTag());
    assertTrue(appProps.isLocalDevelopment());
  }
}
