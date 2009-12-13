/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package au.edu.usq.fascinator.portal;

import org.purl.sword.client.Client;
import org.purl.sword.client.PostMessage;
import org.purl.sword.client.PostDestination;
import org.purl.sword.base.ServiceDocument;
import org.purl.sword.base.ServiceDocumentRequest;
import org.purl.sword.base.Deposit;
//import org.purl.sword.base.*;
//import org.purl.sword.server.*;  // DepositServlet, DummyServer, ServiceDocumentServlet
//import org.w3.atom.*;
//   Author Content Contributor Entry Generator Link Source Summary Title
//        ContentType TextConstruct Rights InvalidMediaTypeException
//
//import org.purl.sword.test.*;

//import org.purl.sword.server.SWORDServer;       // interface
// 	public ServiceDocument doServiceDocument(ServiceDocumentRequest sdr)
//		throws SWORDAuthenticationException, SWORDException;
//	public DepositResponse doDeposit(Deposit deposit)
//		throws SWORDAuthenticationException, SWORDContentTypeException, SWORDException;


/**
 *
 * @author ward
 */
public class Sword {
    public Sword(){
    }

    public Client getClient(){
        return new Client();
    }

    public PostMessage getPostMessage() {
        return new PostMessage();
    }

    public PostDestination getPostDestination() {
        return new PostDestination();
    }

    public ServiceDocument getServiceDocument() {
        return new ServiceDocument();
    }

    public ServiceDocumentRequest getServiceDocumentRequest() {
       // username, password, onBehalfOf, IPAddress
        return new ServiceDocumentRequest();
    }

    public Deposit getDeposit() {
        return new Deposit();
    }
}
