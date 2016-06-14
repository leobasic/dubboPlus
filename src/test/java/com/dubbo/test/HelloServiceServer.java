package com.dubbo.test;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import com.dubbo.apps.thrift.Hello;
import com.dubbo.apps.thrift.Hello.Processor;
import com.dubbo.apps.thrift.HelloServiceImpl;

public class HelloServiceServer {

	public void startServer() {
		try {

			TServerSocket serverTransport = new TServerSocket(20002);

			Hello.Processor process = new Processor(new HelloServiceImpl());

			Factory portFactory = new TBinaryProtocol.Factory(true, true);
			Args args = new Args(serverTransport);
			args.processor(process);
			args.protocolFactory(portFactory);

			TServer server = new TThreadPoolServer(args);
			server.serve();
		} catch (TTransportException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		HelloServiceServer server = new HelloServiceServer();
		server.startServer();
	}

}
