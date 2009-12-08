package au.edu.usq.fascinator;

import org.apache.activemq.broker.BrokerService;

public class QueueService {

    public static void main(String[] args) {
        BrokerService broker = new BrokerService();

        // configure the broker
        try {
            broker.addConnector("tcp://localhost:61616");
            broker.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
