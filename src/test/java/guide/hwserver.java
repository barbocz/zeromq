package guide;

//
//  Hello World server in Java
//  Binds REP socket to tcp://*:5555
//  Expects "Hello" from client, replies with "World"
//

import org.zeromq.ZMQ;

public class hwserver
{

    public static void main(String[] args) throws Exception
    {
        ZMQ.Context context = ZMQ.context(1);

        //  Socket to talk to clients
        ZMQ.Socket socket = context.socket(ZMQ.REP);

        socket.bind("tcp://localhost:5555");

        while (!Thread.currentThread().isInterrupted()) {

            byte[] reply = socket.recv(0);
            System.out.println("Received " + ": [" + new String(reply, ZMQ.CHARSET) + "]");

            //  Create a "Hello" message.
            String request = "krapulax";
            // Send the message
            socket.send(request.getBytes(ZMQ.CHARSET), 0);

            Thread.sleep(1000); //  Do some 'work'
        }

        socket.close();
        context.term();
    }
}
