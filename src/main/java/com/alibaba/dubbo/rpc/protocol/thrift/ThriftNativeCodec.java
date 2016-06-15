package com.alibaba.dubbo.rpc.protocol.thrift;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.utils.ClassHelper;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.buffer.ChannelBufferInputStream;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.remoting.telnet.codec.TelnetCodec;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.protocol.thrift.io.RandomAccessByteArrayOutputStream;

/**
 * 
 * @author <a href="mailto:xpbasic@126.com">echo.zhao</a>
 * @date 2016年6月14日 下午2:08:26
 */
public class ThriftNativeCodec implements Codec2 {
	
	//thrift原生id并发有问题，只能自己做了
	private static final AtomicLong SEQ_ID = new AtomicLong( 0 );

	private static final ConcurrentMap<String, Class<?>> cachedTBaseClass = new ConcurrentHashMap<String, Class<?>>();

	static final ConcurrentMap<Long, RequestData> cachedRequest = new ConcurrentHashMap<Long, RequestData>();

	public static final int MESSAGE_SHORTEST_LENGTH = 10;

	public static final String NAME = "thriftx";

	private final TelnetCodec TELNET_CODEC = new TelnetCodec();

	public void encode(Channel channel, ChannelBuffer buffer, Object message) throws IOException {

		if (message instanceof Response) {
			encodeResponse(channel, buffer, (Response) message);
		}
		// 新增telnet解码
		else if (message instanceof String) {
			TELNET_CODEC.encode(channel, buffer, message);
		} else {
			throw new UnsupportedOperationException(new StringBuilder(32).append("Thrift codec only support encode ")
					.append(Request.class.getName()).append(" and ").append(Response.class.getName()).toString());
		}

	}

	public Object decode(Channel channel, ChannelBuffer buffer) throws IOException {

		//目前只支持配置一个thrift 接口，配置多个，默认选择最后一个配置
		Collection<String> serviceInterfaces = ThriftNativeProtocol.getInstance().getExporterKeys();
		if (serviceInterfaces.size()==0)
		{
			throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, "Can't find any thrift interface.");
		}
		String serviceInterfaceA[] = serviceInterfaces.toArray(new String[serviceInterfaces.size()]);
		String serviceInterface = serviceInterfaceA[serviceInterfaceA.length-1].split(":")[0];

		int available = buffer.readableBytes();

		if (available < MESSAGE_SHORTEST_LENGTH) {

			// return DecodeResult.NEED_MORE_INPUT;
			// 新增telnet解码
			return TELNET_CODEC.decode(channel, buffer);

		} else {

			TIOStreamTransport transport = new TIOStreamTransport(new ChannelBufferInputStream(buffer));
			TBinaryProtocol protocol = new TBinaryProtocol(transport);
			return decode(protocol, serviceInterface);

		}

	}

	private Object decode(TProtocol protocol, String serviceName) throws IOException {

		long seqId = this.nextSeqId();

		TMessage message;

		try {
			message = protocol.readMessageBegin();
		} catch (TException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage(), e);
		}

		if (message.type == TMessageType.CALL) {

			RpcInvocation result = new RpcInvocation();
			result.setAttachment(Constants.INTERFACE_KEY, serviceName);
			result.setMethodName(message.name);

			String argsClassName = ExtensionLoader.getExtensionLoader(ClassNameGenerator.class)
					.getExtension(ThriftClassNameGenerator.NAME).generateArgsClassName(serviceName, message.name);

			if (StringUtils.isEmpty(argsClassName)) {
				throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, "The specified interface name incorrect.");
			}

			Class<?> clazz = cachedTBaseClass.get(argsClassName);

			if (clazz == null) {
				try {

					clazz = ClassHelper.forNameWithThreadContextClassLoader(argsClassName);

					cachedTBaseClass.putIfAbsent(argsClassName, clazz);

				} catch (ClassNotFoundException e) {
					throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
//					RpcResult exResult = new RpcResult();
//					exResult.setException(new RpcException("哈哈"+e.getMessage()));
//
//					Response response = new Response();
//					response.setResult(result);
//					response.setId(seqId);
//					
//					return response;
				}
			}

			TBase<?, ?> args;

			try {
				args = (TBase<?, ?>) clazz.newInstance();
			} catch (InstantiationException e) {
				throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
			} catch (IllegalAccessException e) {
				throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
			}

			try {
				args.read(protocol);
				protocol.readMessageEnd();
			} catch (TException e) {
				throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
			}

			List<Object> parameters = new ArrayList<Object>();
			List<Class<?>> parameterTypes = new ArrayList<Class<?>>();
			int index = 1;

			while (true) {

				TFieldIdEnum fieldIdEnum = args.fieldForId(index++);

				if (fieldIdEnum == null) {
					break;
				}

				String fieldName = fieldIdEnum.getFieldName();

				String getMethodName = ThriftUtils.generateGetMethodName(fieldName);

				Method getMethod;

				try {
					getMethod = clazz.getMethod(getMethodName);
				} catch (NoSuchMethodException e) {
					throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
				}

				parameterTypes.add(getMethod.getReturnType());
				try {
					parameters.add(getMethod.invoke(args));
				} catch (IllegalAccessException e) {
					throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
				} catch (InvocationTargetException e) {
					throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
				}

			}

			result.setArguments(parameters.toArray());
			result.setParameterTypes(parameterTypes.toArray(new Class[parameterTypes.size()]));

			Request request = new Request(seqId);
			request.setData(result);

			cachedRequest.putIfAbsent(seqId, RequestData.create(seqId, serviceName, message.name,message.seqid));

			return request;

		} else if (message.type == TMessageType.EXCEPTION) {

			TApplicationException exception;

			try {
				exception = TApplicationException.read(protocol);
				protocol.readMessageEnd();
			} catch (TException e) {
				throw new IOException(e.getMessage(), e);
			}

			RpcResult result = new RpcResult();

			result.setException(new RpcException(exception.getMessage()));

			Response response = new Response();

			response.setResult(result);

			response.setId(seqId);

			return response;

		} else if (message.type == TMessageType.REPLY) {

			String resultClassName = ExtensionLoader.getExtensionLoader(ClassNameGenerator.class)
					.getExtension(ThriftClassNameGenerator.NAME).generateResultClassName(serviceName, message.name);

			if (StringUtils.isEmpty(resultClassName)) {
				throw new IllegalArgumentException(new StringBuilder(32)
						.append("Could not infer service result class name from service name ").append(serviceName)
						.append(", the service name you specified may not generated by thrift idl compiler")
						.toString());
			}

			Class<?> clazz = cachedTBaseClass.get(resultClassName);

			if (clazz == null) {

				try {

					clazz = ClassHelper.forNameWithThreadContextClassLoader(resultClassName);

					cachedTBaseClass.putIfAbsent(resultClassName, clazz);

				} catch (ClassNotFoundException e) {
					throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
				}

			}

			TBase<?, ? extends TFieldIdEnum> result;
			try {
				result = (TBase<?, ?>) clazz.newInstance();
			} catch (InstantiationException e) {
				throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
			} catch (IllegalAccessException e) {
				throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
			}

			try {
				result.read(protocol);
				protocol.readMessageEnd();
			} catch (TException e) {
				throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
			}

			Object realResult = null;

			int index = 0;

			while (true) {

				TFieldIdEnum fieldIdEnum = result.fieldForId(index++);

				if (fieldIdEnum == null) {
					break;
				}

				Field field;

				try {
					field = clazz.getDeclaredField(fieldIdEnum.getFieldName());
					field.setAccessible(true);
				} catch (NoSuchFieldException e) {
					throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
				}

				try {
					realResult = field.get(result);
				} catch (IllegalAccessException e) {
					throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
				}

				if (realResult != null) {
					break;
				}

			}

			Response response = new Response();

			response.setId(seqId);

			RpcResult rpcResult = new RpcResult();

			if (realResult instanceof Throwable) {
				rpcResult.setException((Throwable) realResult);
			} else {
				rpcResult.setValue(realResult);
			}

			response.setResult(rpcResult);

			return response;

		} else {
			// Impossible
			throw new IOException();
		}

	}


	private void encodeResponse(Channel channel, ChannelBuffer buffer, Response response) throws IOException {

		RpcResult result = (RpcResult) response.getResult();
		RequestData rd = cachedRequest.remove(response.getId());
		
		// 获得thrift生成client的代码中的类名
		String resultClassName = ExtensionLoader
				.getExtensionLoader(ClassNameGenerator.class).getExtension(channel.getUrl()
						.getParameter(ThriftConstants.CLASS_NAME_GENERATOR_KEY, ThriftClassNameGenerator.NAME))
				.generateResultClassName(rd.serviceName, rd.methodName);

		if (StringUtils.isEmpty(resultClassName)) {
			throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, new StringBuilder(32)
					.append("Could not encode response, the specified interface may be incorrect.").toString());
		}

		Class<?> clazz = cachedTBaseClass.get(resultClassName);

		if (clazz == null) {

			try {
				clazz = ClassHelper.forNameWithThreadContextClassLoader(resultClassName);
				cachedTBaseClass.putIfAbsent(resultClassName, clazz);
			} catch (ClassNotFoundException e) {
				throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
			}

		}

		TBase<?, ?> resultObj;

		try {
			resultObj = (TBase<?, ?>) clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
		}

		TApplicationException applicationException = null;
		TMessage message;

		if (result.hasException()) {
			Throwable throwable = result.getException();
			int index = 1;
			boolean found = false;
			while (true) {
				TFieldIdEnum fieldIdEnum = resultObj.fieldForId(index++);
				if (fieldIdEnum == null) {
					break;
				}
				String fieldName = fieldIdEnum.getFieldName();
				String getMethodName = ThriftUtils.generateGetMethodName(fieldName);
				String setMethodName = ThriftUtils.generateSetMethodName(fieldName);
				Method getMethod;
				Method setMethod;
				try {
					getMethod = clazz.getMethod(getMethodName);
					if (getMethod.getReturnType().equals(throwable.getClass())) {
						found = true;
						setMethod = clazz.getMethod(setMethodName, throwable.getClass());
						setMethod.invoke(resultObj, throwable);
					}
				} catch (NoSuchMethodException e) {
					throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
				} catch (InvocationTargetException e) {
					throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
				} catch (IllegalAccessException e) {
					throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
				}
			}

			if (!found) {
				applicationException = new TApplicationException(throwable.getMessage());
			}

		} else {// thrift规定动作
			Object realResult = result.getValue();
			// result field id is 0
			String fieldName = resultObj.fieldForId(0).getFieldName();
			String setMethodName = ThriftUtils.generateSetMethodName(fieldName);
			String getMethodName = ThriftUtils.generateGetMethodName(fieldName);
			Method getMethod;
			Method setMethod;
			try {
				getMethod = clazz.getMethod(getMethodName);
				setMethod = clazz.getMethod(setMethodName, getMethod.getReturnType());
				setMethod.invoke(resultObj, realResult);
			} catch (NoSuchMethodException e) {
				throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
			} catch (InvocationTargetException e) {
				throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
			} catch (IllegalAccessException e) {
				throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
			}

		}

		if (applicationException != null|| ((RpcResult)response.getResult()).hasException() ) {
			message = new TMessage(rd.methodName, TMessageType.EXCEPTION, rd.thrift_seq_id);
		} else {
			message = new TMessage(rd.methodName, TMessageType.REPLY, rd.thrift_seq_id);
		}

		RandomAccessByteArrayOutputStream bos = new RandomAccessByteArrayOutputStream(1024);

		TIOStreamTransport transport = new TIOStreamTransport(bos);

		TBinaryProtocol protocol = new TBinaryProtocol(transport);

		try {
			// message
			protocol.writeMessageBegin(message);
			switch (message.type) {
			case TMessageType.EXCEPTION:
				applicationException.write(protocol);
				break;
			case TMessageType.REPLY:
				resultObj.write(protocol);
				break;
			}
			protocol.writeMessageEnd();
			protocol.getTransport().flush();
			int oldIndex = bos.size();
			bos.setWriteIndex(oldIndex);

		} catch (TException e) {
			throw new RpcException(RpcException.SERIALIZATION_EXCEPTION, e.getMessage(), e);
		}
		buffer.writeBytes(bos.toByteArray());
	}

	private long nextSeqId()
	{
		return SEQ_ID.getAndIncrement();
	}
	
	
	static class RequestData {
		long id;
		String serviceName;
		String methodName;
		int thrift_seq_id;

		static RequestData create(long id, String sn, String mn,int thrift_seq_id) {
			RequestData result = new RequestData();
			result.id = id;
			result.serviceName = sn;
			result.methodName = mn;
			result.thrift_seq_id = thrift_seq_id;
			
			return result;
		}

	}

}
