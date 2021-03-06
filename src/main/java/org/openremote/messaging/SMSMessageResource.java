/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2016, OpenRemote Inc.
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.messaging;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.openremote.messaging.domain.SMSMessage;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;

@Path("accounts/{accountId}/SMSMessages")
public class SMSMessageResource
{
  @Inject
  ServiceConfiguration configuration;

  @PathParam(value = "accountId")
  String accountId;

  /*
   * Exception in thread "main" java.lang.IllegalArgumentException: AuthToken ''
   * is not valid. at
   * com.twilio.sdk.TwilioRestClient.validateAuthToken(TwilioRestClient
   * .java:175) at
   * com.twilio.sdk.TwilioRestClient.<init>(TwilioRestClient.java:130) at
   * com.twilio.sdk.TwilioRestClient.<init>(TwilioRestClient.java:110) at
   * TwilioTest.main(TwilioTest.java:19)
   */

  @POST
  @Consumes("application/json")
  @Produces("application/json")
  public Response sendSMSMessage(@Context SecurityContext sc, SMSMessage message)
  {
    TwilioRestClient client = new TwilioRestClient(configuration.getTwilioAccountSid(), configuration.getTwilioAuthToken());

    // Build a filter for the MessageList
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("Body", message.getMessage()));

    if (message.getRecipients().size() <= 0)
    {
      return Response.status(Status.BAD_REQUEST).entity("TODO - appropriate error").build();
    }
    
    for (String recipient : message.getRecipients())
    {
      // TODO: validate recipient is valid SMS number and potentially reformat
      
      // Sending each message individually (no batch API on Twilio)
      params.add(new BasicNameValuePair("To", recipient));      
    }
    
    params.add(new BasicNameValuePair("From", configuration.getTwilioSmsFromNumber()));

    /*
     * Exception in thread "main" com.twilio.sdk.TwilioRestException: The From
     * phone number +13853558104 is not a valid, SMS-capable inbound phone number
     * or short code for your account. at
     * com.twilio.sdk.TwilioRestException.parseResponse
     * (TwilioRestException.java:74) at
     * com.twilio.sdk.TwilioRestClient.safeRequest(TwilioRestClient.java:583) at
     * com.twilio.sdk.resource.list.MessageList.create(MessageList.java:70) at
     * TwilioTest.main(TwilioTest.java:36)
     */

    MessageFactory messageFactory = client.getAccount().getMessageFactory();
    Message twilioMessage;
    try
    {
      twilioMessage = messageFactory.create(params);
      System.out.println(twilioMessage.getSid());
    } catch (TwilioRestException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    return Response.ok().build();
  }

}