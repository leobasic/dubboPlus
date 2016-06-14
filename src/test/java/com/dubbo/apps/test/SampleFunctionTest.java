package com.dubbo.apps.test;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

//@ContextConfiguration(locations = { "classpath:dubbo-services2.xml" })
//@RunWith(SpringJUnit4ClassRunner.class)
public class SampleFunctionTest {

	@Resource
	private SampleFunction sampleFunction;

	//@Test
	public void test() {
		for (int i = 0; i < 1; i++) {
			try {
				ConcurrentHashMap<String, String> ccm1 = new ConcurrentHashMap<String, String>();
				ccm1.put("ccm1Key1", "ccm1Val1");
				ccm1.put("ccm1Key2", "ccm1Val2");

				ConcurrentHashMap<String, String> ccm2 = new ConcurrentHashMap<String, String>();
				ccm2.put("ccm2Key1", "ccm2Val1");
				ccm2.put("ccm2Key2", "ccm2Val2");

				DataBean dataBean = new DataBean();
				dataBean.setName("中文名字");
				dataBean.addMapSet(ccm1);
				dataBean.addMapSet(ccm2);
				dataBean.put("主键1", "111");
				dataBean.put("主键2", "222");

				System.err.println("返回结果：=====================");
				DataBean result = sampleFunction.processObj(dataBean);
				System.err.println("name：" + result.getName());
				System.err.println("主键1：" + result.get("主键1"));
				System.err.println("主键2：" + result.get("主键2"));

				for (Iterator<ConcurrentHashMap<String, String>> iter = dataBean.getMapSet(); iter.hasNext();) {
					ConcurrentHashMap<String, String> chm = iter.next();
					
					for (String k:chm.keySet())
					{
						System.err.println(k+"="+chm.get(k));
					}
					
				}
				

			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

}
