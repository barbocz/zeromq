package artificial.intelligence;
//
//  Hello World server in Java
//  Binds REP socket to tcp://*:5555
//  Expects "Hello" from client, replies with "World"
//

import org.zeromq.ZMQ;

import java.io.File;
import java.io.FilenameFilter;

public class server {

    public static void main(String[] args) throws Exception {

        String request = "";
        Classification classification = new Classification("", "", "");

        //System.out.println(dirList);

        ZMQ.Context context = ZMQ.context(1);

        //  Socket to talk to clients
        ZMQ.Socket socket = context.socket(ZMQ.REP);

        socket.bind("tcp://localhost:5555");

        while (!Thread.currentThread().isInterrupted()) {

            byte[] reply = socket.recv(0);
            String receivedMessage = new String(reply, ZMQ.CHARSET);
            System.out.println("MT4 message " + ": [" + new String(reply, ZMQ.CHARSET) + "]");

            String messageParts[] = receivedMessage.split("\\|");
            String action = messageParts[0];
            //System.out.println("messageParts length: "+messageParts.length);
            //if (messageParts.length==3) System.out.println(messageParts[0]+","+messageParts[1]+","+messageParts[2]);

            request = "Problem occured during Server processing";
            try {

                switch (action) {
                    case "init":
                        request = "ok";
                        classification = new Classification(messageParts[1], messageParts[2], messageParts[3]);
                        //System.out.println(action+" command received");
                        break;
                    case "getIndicatorName":
                        //System.out.println(messageParts[0] + "," + messageParts[1]);
                        //System.out.println(action+" command received");
                        request = classification.getIndicatorName(messageParts[1]);
//                    System.out.println(request);
                        break;
                    case "setStrategy":
                        classification.setStrategy(messageParts[1]);
                        break;
                    case "copyStrategyToTester":
                        classification.copyStrategyToTester(messageParts[1]);
                        break;
                    case "setModel":
                        classification.setModel(messageParts[1]);
                        classification.setClassifier();
                        break;
                    case "train":
                        //System.out.println(action+" command received");
                        request = classification.train();
                        //System.out.println(request);

                        break;

                    case "classify":
                        double[] feautures = new double[messageParts.length - 1];
                        for (int i = 0; i < feautures.length; i++) {
                            feautures[i] = Double.parseDouble(messageParts[i + 1]);
                        }
                        request = classification.classify(feautures);
                        break;
                    case "testClassify":
                        request = classification.testClassify();
                        break;
                }

            } catch (Exception e) {
                System.out.println("Exception: " + e.toString());
            }


            //  Create a "Hello" message.
            //String request = MT4FileAccess.getIndicatorName("C:\\Program Files\\Global Prime\\MQL4\\Indicators",new String(reply, ZMQ.CHARSET));;
            // Send the message
            socket.send(request.getBytes(ZMQ.CHARSET), 0);

            Thread.sleep(1000); //  Do some 'work'
        }

        socket.close();
        context.term();
    }
}
