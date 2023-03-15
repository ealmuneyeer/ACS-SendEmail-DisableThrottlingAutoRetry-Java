package com.communication.email;

import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.ExponentialBackoff;


public class CustomStrategy extends ExponentialBackoff{
    @Override
    public boolean shouldRetry (final HttpResponse httpResponse) {
        int code = httpResponse.getStatusCode();

        //If the return error code is 429 for throttling stop the retrial, otherwsie, keep the default behavior
        if(code == HTTP_STATUS_TOO_MANY_REQUESTS){
            return false;
        }else{
            return super.shouldRetry(httpResponse);
        }
    }
}
