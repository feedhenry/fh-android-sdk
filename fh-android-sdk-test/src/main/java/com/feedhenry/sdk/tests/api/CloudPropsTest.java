package com.feedhenry.sdk.tests.api;

import android.test.AndroidTestCase;
import com.feedhenry.sdk.AppProps;
import com.feedhenry.sdk.CloudProps;

/**
 * Created by weili on 15/04/15.
 */
public class CloudPropsTest extends AndroidTestCase {

  public void testCloudProps() throws Exception {
    AppProps.load(getContext());
    CloudProps cloudProps = CloudProps.init();
    String cloudHost = cloudProps.getCloudHost();
    assertEquals("http://localhost:9000", cloudHost);
  }
}
