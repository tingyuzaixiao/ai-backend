package com.train.platform;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class test {

	@Test
	public void test() {
//		String str = "[\"43\",\"44\",\"45\"]";
//		List<Long> list = JSON.parseArray(str, Long.class);
//		list.forEach(System.out::println);

		List<Integer> list = new ArrayList<>(Arrays.asList(1,2,3));
		List<Integer> list2 = new ArrayList<>(Arrays.asList(3,2,1));

		String test = list.toString();
		System.out.println(test);


	}
}
