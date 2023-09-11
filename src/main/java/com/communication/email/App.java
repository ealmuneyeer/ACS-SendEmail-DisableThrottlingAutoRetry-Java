package com.communication.email;
import java.time.Duration;

import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.EmailClient;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.communication.email.models.EmailSendStatus;
import com.azure.communication.email.models.EmailMessage;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;

public class App 
{
    public static final Duration POLLER_WAIT_TIME = Duration.ofSeconds(10);



    public static void main( String[] args )
    {
        // System.setProperty("http.proxyHost", "127.0.0.1");
        // System.setProperty("https.proxyHost", "127.0.0.1");
        // System.setProperty("http.proxyPort", "8888");
        // System.setProperty("https.proxyPort", "8888");

        //System.setProperty("javax.net.ssl.trustStoreType","Windows-ROOT");

        String connectionString = "endpoint=https://ebraheemacsus.communication.azure.com/;accesskey=dPHQy6c8EkgqOocLMZUlGJjq0knigExHhakHyoqlZDcOfIy1/mCFmhWG6OuBztxFv68LNRdKjjYpS3CybojWXQ==";
        String endpoint = "https://ebraheemacsus.communication.azure.com";
        String senderAddress = "DoNotReply@notification.ealmuneyeeraz.net";
        String recipientAddress = "ealmuneyeer@microsoft.com";

        DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

        ClientSecretCredential secretCredential = new ClientSecretCredentialBuilder()
                .tenantId("72f988bf-86f1-41af-91ab-2d7cd011db47")
                .clientId("83918f85-56ba-4108-aa47-57a231fc694e")
                .clientSecret("b-Y8Q~FTiKSFcs~FL9PYmN~kPdWta5CmUI2TvaD.")
                .build();

        EmailClient client = new EmailClientBuilder()
            .credential(defaultCredential)
            .endpoint(endpoint)
            //.connectionString(connectionString)
            .retryPolicy(new RetryPolicy(new CustomStrategy()))
            .buildClient();

        EmailMessage message = new EmailMessage()
            .setSenderAddress(senderAddress)
            .setToRecipients(recipientAddress)
            .setSubject("Test email from Java Sample")
            .setBodyPlainText("This is plaintext body of test email.")
            .setBodyHtml("<html><h1>This is the html body of test email.</h1></html>");

        try
        {
            SyncPoller<EmailSendResult, EmailSendResult> poller = null;
            
            try{
                for(int i =0; i < 20; i++){
                    poller = client.beginSend(message, null);
                    System.out.println("Message sent" + i);
                }
            }catch(Exception ex){
                    System.out.println("Exception occurred");
            }

            //SyncPoller<EmailSendResult, EmailSendResult> poller = client.beginSend(message, null);

            PollResponse<EmailSendResult> pollResponse = null;

            Duration timeElapsed = Duration.ofSeconds(0);

             while (pollResponse == null
                     || pollResponse.getStatus() == LongRunningOperationStatus.NOT_STARTED
                     || pollResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS)
             {
                 pollResponse = poller.poll();
                 System.out.println("Email send poller status: " + pollResponse.getStatus());

                 Thread.sleep(POLLER_WAIT_TIME.toMillis());
                 timeElapsed = timeElapsed.plus(POLLER_WAIT_TIME);

                 if (timeElapsed.compareTo(POLLER_WAIT_TIME.multipliedBy(18)) >= 0)
                 {
                     throw new RuntimeException("Polling timed out.");
                 }
             }

             if (poller.getFinalResult().getStatus() == EmailSendStatus.SUCCEEDED)
             {
                 System.out.printf("Successfully sent the email (operation id: %s)", poller.getFinalResult().getId());
             }
             else
             {
                 throw new RuntimeException(poller.getFinalResult().getError().getMessage());
             }
        }
        catch (Exception exception)
        {
            System.out.println(exception.getMessage());
        }
    }
}
