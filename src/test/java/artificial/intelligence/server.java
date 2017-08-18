package artificial.intelligence;
//
//  Hello World server in Java
//  Binds REP socket to tcp://*:5555
//  Expects "Hello" from client, replies with "World"
//

import org.zeromq.ZMQ;

import java.io.File;
import java.io.FilenameFilter;

public class server
{

    public static void main(String[] args) throws Exception
    {

        String request="";

        //System.out.println(dirList);

        ZMQ.Context context = ZMQ.context(1);

        //  Socket to talk to clients
        ZMQ.Socket socket = context.socket(ZMQ.REP);

        socket.bind("tcp://localhost:5555");

        while (!Thread.currentThread().isInterrupted()) {

            byte[] reply = socket.recv(0);
            String receivedMessage=new String(reply, ZMQ.CHARSET);
            //System.out.println("Received " + ": [" + new String(reply, ZMQ.CHARSET) + "]");

            String messageParts[]=receivedMessage.split("\\|");
            String action=messageParts[0];
            //System.out.println(messageParts[0]+","+messageParts[1]);

            switch (action) {
                case "getIndicatorName":
                    //System.out.println(messageParts[0] + "," + messageParts[1]);
                    System.out.println(action+" command received");
                    request = MT4FileAccess.getIndicatorName("C:\\Program Files\\Global Prime\\MQL4\\Indicators",messageParts[1]);
                    //System.out.println(request);
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
