package com.debayan.continuousdatacollect.Utils;

/**
 * Created by debayan on 10/17/17.
 */

public class Constants {
    /*
     * You should replace these values with your own. See the README for details
     * on what to fill in.
     */
    public static final String COGNITO_POOL_ID = "us-east-2:76bfd60b-6b5f-4921-a77a-ef68c14ca111";

    /*
     * Region of your Cognito identity pool ID.
     */
    public static final String COGNITO_POOL_REGION = "us-east-2";

    /*
     * Note, you must first create a bucket using the S3 console before running
     * the sample (https://console.aws.amazon.com/s3/). After creating a bucket,
     * put it's name in the field below.
     */
    public static final String BUCKET_NAME = "continuous-authentication";

    /*
     * Region of your bucket.
     */
    public static final String BUCKET_REGION = "us-east-2";
}
