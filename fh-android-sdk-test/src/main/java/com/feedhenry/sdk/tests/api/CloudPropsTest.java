package com.feedhenry.sdk.tests.api;

import android.test.AndroidTestCase;
import com.feedhenry.sdk.AppProps;
import com.feedhenry.sdk.CloudProps;

public class CloudPropsTest extends AndroidTestCase {

  public void testCloudProps() throws Exception {
    AppProps.load(getContext());
    CloudProps cloudProps = CloudProps.initDev();
    String cloudHost = cloudProps.getCloudHost();
    assertEquals("http://localhost:9000", cloudHost);
  }
}
