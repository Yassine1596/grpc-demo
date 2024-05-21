package io.grpc.examples.helloworld;

/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple client that requests a greeting from the {@link HelloWorldServer}.
 */
public class HelloWorldClient {
    private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());

    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    /** Construct client for accessing HelloWorld server using the existing channel. */
    public HelloWorldClient(Channel channel) {
        // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
        // shut it down.

        // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    /** Say hello to server. */
public void greet(String name, String firstName, String cin) {
    logger.info("Will try to greet " + name + " " + firstName + " with CIN: " + cin + " ...");
    HelloRequest request = HelloRequest.newBuilder()
            .setName(name)
            .setFirstName(firstName) // Assuming you have these fields in your HelloRequest message
            .setCin(cin) // Assuming you have this field in your HelloRequest message
            .build();
    HelloReply response;
    try {
        response = blockingStub.sayHello(request);
        logger.info("Greeting: " + response.getMessage());
    } catch (StatusRuntimeException e) {
        logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        return; // Early return on failure
    }

    try {
        response = blockingStub.sayHelloAgain(request);
        logger.info("Greeting: " + response.getMessage());
    } catch (StatusRuntimeException e) {
        logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
    }
}

/**
 * Greet server. If provided, the first element of {@code args} is the name, the second is the first name,
 * and the third is the CIN. The last argument is the target server.
 */
public static void main(String[] args) throws Exception {
    String user = "GHOUMA";
    String firstName = "MAYEZ"; // le nom par defaut
    String cin = "99999999"; // le cin par defaut
    String target = "localhost:50051";
    
    if (args.length > 0) {
        if ("--help".equals(args[0])) {
            System.err.println("Usage: [name] [firstName] [cin] [target]");
            System.err.println("");
            System.err.println("  name      The name you wish to be greeted by. Defaults to " + user);
            System.err.println("  firstName The first name for the greeting. Defaults to " + firstName);
            System.err.println("  cin       The CIN for the greeting. Defaults to " + cin);
            System.err.println("  target    The server to connect to. Defaults to " + target);
            System.exit(1);
        }
        user = args[0];
    }
    if (args.length > 1) {
        firstName = args[1];
    }
    if (args.length > 2) {
        cin = args[2];
    }
    if (args.length > 3) {
        target = args[3];
    }

    ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
            .build();
    try {
        HelloWorldClient client = new HelloWorldClient(channel);
        client.greet(user, firstName, cin);
    } finally {
        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }
}
}
